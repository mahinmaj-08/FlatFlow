package flatflow.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The one place that knows where flatflow.db is. Every SQLite store shares this object
 * (the aggregation "store <>-- Database" on the poster).
 */
public class Database {
    private final String url;

    public Database(String path) {
        try {
            Path parent = Path.of(path).toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create folder for database: " + path, e);
        }
        this.url = "jdbc:sqlite:" + path;
    }

    /** Opens a connection. Callers close it with try-with-resources. */
    public Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");   // SQLite only checks foreign keys when asked
        }
        return con;
    }

    /** Creates the tables from schema.sql if they do not exist yet. Safe to run on every start. */
    public void createTables() {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql is missing from resources");
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read schema.sql", e);
        }
        try (Connection con = connect(); Statement st = con.createStatement()) {
            for (String part : sql.replaceAll("--[^\n]*", "").split(";")) {
                if (!part.isBlank()) st.execute(part);
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}
