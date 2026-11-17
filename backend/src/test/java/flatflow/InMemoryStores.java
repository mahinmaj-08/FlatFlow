package flatflow;

import flatflow.data.AccountStore;
import flatflow.data.HouseholdStore;
import flatflow.model.*;

import java.time.Instant;
import java.util.*;

/**
 * Test double: the account and household stores kept in memory, so service tests need no database file.
 * Possible because the services depend on store interfaces, not on SQLite (DIP / ISP).
 */
class InMemoryStores {
    private int nextId = 1;
    private final Map<Integer, UserAccount> users = new LinkedHashMap<>();
    private final Map<String, Session> sessions = new HashMap<>();
    private final Map<Integer, Household> householdRows = new LinkedHashMap<>();

    final AccountStore accounts = new AccountStore() {
        public int createUser(String displayName, String loginName, String passwordHash) {
            int id = nextId++;
            users.put(id, new UserAccount(id, displayName, loginName, passwordHash, null));
            return id;
        }
        public Optional<UserAccount> findByLogin(String login) {
            return users.values().stream().filter(u -> u.loginName().equals(login)).findFirst();
        }
        public Optional<UserAccount> findById(int id) { return Optional.ofNullable(users.get(id)); }
        public void setHousehold(int userId, int householdId) {
            UserAccount u = users.get(userId);
            users.put(userId, new UserAccount(u.id(), u.displayName(), u.loginName(), u.passwordHash(), householdId));
        }
        public void createSession(String token, int userId, Instant at) { sessions.put(token, new Session(token, userId, at)); }
        public Optional<Session> findSession(String token) { return Optional.ofNullable(sessions.get(token)); }
        public void deleteSession(String token) { sessions.remove(token); }
    };

    final HouseholdStore households = new HouseholdStore() {
        public Household create(String name, int organizerUserId, String code) {
            Household h = new Household(nextId++, name, organizerUserId, code);
            householdRows.put(h.id(), h);
            accounts.setHousehold(organizerUserId, h.id());
            return h;
        }
        public Optional<Household> findById(int id) { return Optional.ofNullable(householdRows.get(id)); }
        public Optional<Household> findByInviteCode(String code) {
            return householdRows.values().stream().filter(h -> h.inviteCode().equals(code)).findFirst();
        }
        public List<UserAccount> members(int householdId) {
            return users.values().stream().filter(u -> Objects.equals(u.householdId(), householdId)).toList();
        }
    };
}