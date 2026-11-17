package flatflow.service;

/**
 * Turns a password into a hash and checks a password against a hash.
 * AuthService depends on this interface, not on a bcrypt library (DIP), so tests can use a fake.
 */
public interface PasswordHasher {
    String hash(String password);
    boolean matches(String password, String hash);
}
