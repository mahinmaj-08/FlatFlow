import io.javalin.Javalin;
import java.util.Map;

/** Version A: Javalin. JSON in and out is handled by the framework (Jackson). Port 7070. */
public class JavalinApp {
    public static void main(String[] args) {
        ChoreRepository repo = new ChoreRepository("flatflow-javalin.db");
        Javalin app = Javalin.create().start(7070);

        app.get("/api/chores", ctx -> ctx.json(repo.findAll()));
        app.post("/api/chores", ctx -> ctx.status(201).json(repo.save(ctx.bodyAsClass(Chore.class))));
        app.exception(IllegalArgumentException.class,
            (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
    }
}
