package flatflow.service;

import flatflow.data.AccountStore;
import flatflow.model.Session;
import flatflow.model.UserAccount;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Register, sign in, sign out, and "who is this token?" (poster 2: AuthService; SOLID DIP example).
 * Depends only on interfaces (AccountStore, PasswordHasher), which App passes in.
 */
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountStore accounts;
    private final PasswordHasher hasher;
    private final Duration tokenLifetime;
    private final Clock clock;

    public AuthService(AccountStore accounts, PasswordHasher hasher, int tokenLifetimeHours, Clock clock) {
        this.accounts = accounts;
        this.hasher = hasher;
        this.tokenLifetime = Duration.ofHours(tokenLifetimeHours);
        this.clock = clock;
    }

    /** Creates an account and returns its id. */
    public int register(String displayName, String loginName, String password) {
        String name = Check.text(displayName, "Display name", 1, 40);
        String login = Check.text(loginName, "Login name", 3, 20);
        if (!login.matches("[A-Za-z0-9_]+")) {
            throw ApiException.badRequest("Login name can only use letters, digits and underscores");
        }
        if (password == null || password.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters");
        }
        if (accounts.findByLogin(login).isPresent()) {
            throw ApiException.conflict("That login name is already taken");
        }
        return accounts.createUser(name, login, hasher.hash(password));
    }

    public record LoginResult(String token, Integer householdId) {}

    /** Checks the password and starts a session. Same message for wrong name or wrong password. */
    public LoginResult login(String loginName, String password) {
        String login = loginName == null ? "" : loginName.trim();
        UserAccount user = accounts.findByLogin(login)
                .filter(u -> password != null && hasher.matches(password, u.passwordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Wrong login name or password"));

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        accounts.createSession(token, user.id(), Instant.now(clock));
        return new LoginResult(token, user.householdId());
    }

    public void logout(String token) {
        if (token != null) accounts.deleteSession(token);
    }

    /** The signed-in user for a token, or empty if the token is unknown or expired. */
    public Optional<UserAccount> userForToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        Optional<Session> session = accounts.findSession(token);
        if (session.isEmpty()) return Optional.empty();
        if (session.get().createdAt().plus(tokenLifetime).isBefore(Instant.now(clock))) {
            accounts.deleteSession(token);   // expired: remove it
            return Optional.empty();
        }
        return accounts.findById(session.get().userId());
    }
}
