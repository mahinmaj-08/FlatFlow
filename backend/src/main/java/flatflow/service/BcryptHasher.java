package flatflow.service;

import org.mindrot.jbcrypt.BCrypt;

/** The real PasswordHasher. Cost comes from config.json (bcryptCost). */
public class BcryptHasher implements PasswordHasher {
    private final int cost;

    public BcryptHasher(int cost) { this.cost = cost; }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(cost));
    }

    @Override
    public boolean matches(String password, String hash) {
        return hash != null && BCrypt.checkpw(password, hash);
    }
}
