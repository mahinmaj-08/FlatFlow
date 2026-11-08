import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** SQLite access shared by both server versions, so only the web layer differs. */
public class ChoreRepository {
    private final String url;

    public ChoreRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        try (Connection con = open(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS chore ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, "
                + "assignee_user_id INTEGER NOT NULL, due_date TEXT NOT NULL, status TEXT NOT NULL)");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Connection open() throws SQLException { return DriverManager.getConnection(url); }

    public Chore save(Chore c) {
        String status = c.status() == null ? "PENDING" : c.status();
        if (c.title() == null || c.title().isBlank()) throw new IllegalArgumentException("Title is required");
        if (c.assigneeUserId() <= 0) throw new IllegalArgumentException("Assignee is required");
        if (!status.equals("PENDING") && !status.equals("DONE")) throw new IllegalArgumentException("Status must be PENDING or DONE");
        try { LocalDate.parse(c.dueDate()); }
        catch (Exception e) { throw new IllegalArgumentException("Due date must look like 2026-11-10"); }

        String sql = "INSERT INTO chore(title, assignee_user_id, due_date, status) VALUES (?, ?, ?, ?)";  // parameterized: no SQL injection
        try (Connection con = open(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.title().trim());
            ps.setInt(2, c.assigneeUserId());
            ps.setString(3, c.dueDate());
            ps.setString(4, status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Chore(keys.getInt(1), c.title().trim(), c.assigneeUserId(), c.dueDate(), status);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Chore> findAll() {
        List<Chore> out = new ArrayList<>();
        try (Connection con = open(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, title, assignee_user_id, due_date, status FROM chore ORDER BY due_date, id")) {
            while (rs.next()) out.add(new Chore(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5)));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
