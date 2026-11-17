package flatflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import flatflow.config.AppConfig;
import flatflow.data.*;
import flatflow.service.*;
import flatflow.web.*;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

/**
 * Starts FlatFlow. This is the only class that picks the real implementations
 * (SQLite stores, bcrypt) and passes them into the services (SOLID report: DIP).
 *
 * Run: mvn compile exec:java      (reads config.json in the backend folder)
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.load(Path.of(args.length > 0 ? args[0] : "config.json"));
        start(config);
    }

    public static Javalin start(AppConfig config) {
        // Data block
        Database db = new Database(config.databasePath());
        db.createTables();
        AccountStore accounts = new SqliteAccountStore(db);
        HouseholdStore householdStore = new SqliteHouseholdStore(db);

        // Processing block: services
        AuthService auth = new AuthService(accounts, new BcryptHasher(config.bcryptCost()),
                config.tokenLifetimeHours(), Clock.systemUTC());
        HouseholdService households = new HouseholdService(householdStore, accounts, config.inviteCodeLength());

        // Web server
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Path frontend = config.frontendDir() == null ? null : Path.of(config.frontendDir());
        Javalin app = Javalin.create(cfg -> {
            cfg.jsonMapper(new JavalinJackson(mapper, false));
            cfg.showJavalinBanner = false;
            if (frontend != null && Files.isDirectory(frontend)) {
                cfg.staticFiles.add(frontend.toAbsolutePath().toString(), Location.EXTERNAL);   // serves the HTML pages too
            }
        });

        app.before("/api/*", new SessionChecker(auth));
        new AuthController(auth).register(app);
        new HouseholdController(households).register(app);

        // Errors always come back as {"error": "..."}, which the frontend's api.js shows to the user
        app.exception(ApiException.class, (e, ctx) -> ctx.status(e.status()).json(Map.of("error", e.getMessage())));
        app.exception(IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unexpected error on {} {}", ctx.method(), ctx.path(), e);
            ctx.status(500).json(Map.of("error", "Something went wrong on the server"));
        });
        app.start(config.port());
        log.info("FlatFlow API running on http://localhost:{}", config.port());
        if (frontend == null || !Files.isDirectory(frontend)) {
            log.info("No frontend folder at {}: serving the API only", config.frontendDir());
        }
        return app;
    }
}