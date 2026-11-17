package flatflow.data;

import flatflow.model.Household;
import flatflow.model.UserAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** HouseholdStore backed by SQLite. */
public class SqliteHouseholdStore implements HouseholdStore {
    private final Database db;

    public SqliteHouseholdStore(Database db) { this.db = db; }

    @Override
    public Household create(String name, int organizerUserId, String inviteCode) {
        try (Connection con = db.connect()) {
            con.setAutoCommit(false);   // both steps happen, or neither does
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO household(name, organizer_user_id, invite_code) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setInt(2, organizerUserId);
                    ps.setString(3, inviteCode);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); id = keys.getInt(1); }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE user SET household_id = ? WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, organizerUserId);
                    ps.executeUpdate();
                }
                con.commit();
                return new Household(id, name, organizerUserId, inviteCode);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new DataException(e); }
    }

    @Override
    public Optional<Household> findById(int householdId) {
        return findOne("SELECT * FROM household WHERE id = ?", householdId);
    }

    @Override
    public Optional<Household> findByInviteCode(String inviteCode) {
        return findOne("SELECT * FROM household WHERE invite_code = ?", inviteCode);
    }

    private Optional<Household> findOne(String sql, Object param) {
        try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Household(rs.getInt("id"), rs.getString("name"),
                        rs.getInt("organizer_user_id"), rs.getString("invite_code")));
            }
        } catch (SQLException e) { throw new DataException(e); }
    }

    @Override
    public List<UserAccount> members(int householdId) {
        List<UserAccount> out = new ArrayList<>();
        try (Connection con = db.connect();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM user WHERE household_id = ? ORDER BY id")) {
            ps.setInt(1, householdId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(SqliteAccountStore.read(rs));
            }
        } catch (SQLException e) { throw new DataException(e); }
        return out;
    }
}
