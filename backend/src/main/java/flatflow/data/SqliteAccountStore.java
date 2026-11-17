package flatflow.data;

import flatflow.model.Session;
import flatflow.model.UserAccount;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/** AccountStore backed by SQLite. Every query uses ? placeholders (parameterized = no SQL injection). */
public class SqliteAccountStore implements AccountStore {
    private final Database db;

    public SqliteAccountStore(Database db) { this.db = db; }

    @Override
    public int createUser(String displayName, String loginName, String passwordHash) {
        String sql = "INSERT INTO user(display_name, login_name, password_hash) VALUES (?, ?, ?)";
        try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, displayName);
            ps.setString(2, loginName);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getInt(1); }
        } catch (SQLException e) { throw new DataException(e); }
    }

    @Override
    public Optional<UserAccount> findByLogin(String loginName) {
        return findOne("SELECT * FROM user WHERE login_name = ?", loginName);
    }

    @Override
    public Optional<UserAccount> findById(int userId) {
        return findOne("SELECT * FROM user WHERE id = ?", userId);
    }

    private Optional<UserAccount> findOne(String sql, Object param) {
        try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(read(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new DataException(e); }
    }

    static UserAccount read(ResultSet rs) throws SQLException {
        int hh = rs.getInt("household_id");
        Integer householdId = rs.wasNull() ? null : hh;
        return new UserAccount(rs.getInt("id"), rs.getString("display_name"), rs.getString("login_name"),
                rs.getString("password_hash"), householdId);
    }

    @Override
    public void setHousehold(int userId, int householdId) {
        update("UPDATE user SET household_id = ? WHERE id = ?", householdId, userId);
    }

    @Override
    public void createSession(String token, int userId, Instant createdAt) {
        update("INSERT INTO session(token, user_id, created_at) VALUES (?, ?, ?)", token, userId, createdAt.toString());
    }

    @Override
    public Optional<Session> findSession(String token) {
        String sql = "SELECT token, user_id, created_at FROM session WHERE token = ?";
        try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Session(rs.getString(1), rs.getInt(2), Instant.parse(rs.getString(3))));
            }
        } catch (SQLException e) { throw new DataException(e); }
    }

    @Override
    public void deleteSession(String token) {
        update("DELETE FROM session WHERE token = ?", token);
    }

    private void update(String sql, Object... params) {
        try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DataException(e); }
    }
}
