package flatflow.data;

import flatflow.model.Session;
import flatflow.model.UserAccount;

import java.time.Instant;
import java.util.Optional;

/** Everything sign-in needs from the database: users and sessions (poster: AccountStore). */
public interface AccountStore {
    int createUser(String displayName, String loginName, String passwordHash);
    Optional<UserAccount> findByLogin(String loginName);
    Optional<UserAccount> findById(int userId);
    void setHousehold(int userId, int householdId);

    void createSession(String token, int userId, Instant createdAt);
    Optional<Session> findSession(String token);
    void deleteSession(String token);
}
