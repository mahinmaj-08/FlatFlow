package flatflow.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;

/**
 * Settings read once from config.json when the server starts (see D1 I/O spec, "Configuration file").
 * Keeping settings here means the same code runs on a laptop or on a hosted server.
 */
public record AppConfig(int port, String databasePath, int tokenLifetimeHours, int bcryptCost,
                        int inviteCodeLength, String timeZone, String frontendDir) {

    public static AppConfig load(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalStateException("config.json not found at " + file.toAbsolutePath());
        }
        AppConfig c;
        try {
            c = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(file.toFile(), AppConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("config.json is not valid JSON: " + e.getMessage());
        }
        c.check();
        return c;
    }

    /** Stop at startup with a clear message instead of running with a bad setting. */
    private void check() {
        if (port < 1 || port > 65535) bad("port", "must be between 1 and 65535");
        if (databasePath == null || databasePath.isBlank()) bad("databasePath", "is missing");
        if (tokenLifetimeHours < 1) bad("tokenLifetimeHours", "must be at least 1");
        if (bcryptCost < 4 || bcryptCost > 31) bad("bcryptCost", "must be between 4 and 31");
        if (inviteCodeLength < 4 || inviteCodeLength > 12) bad("inviteCodeLength", "must be between 4 and 12");
        try { zone(); } catch (Exception e) { bad("timeZone", "is not a valid time zone, e.g. America/Halifax"); }
    }

    private static void bad(String setting, String problem) {
        throw new IllegalStateException("Bad setting in config.json: \"" + setting + "\" " + problem);
    }

    /** Time zone used to decide what "today" is (for overdue chores). */
    public ZoneId zone() {
        return ZoneId.of(timeZone == null || timeZone.isBlank() ? "America/Halifax" : timeZone);
    }
}
