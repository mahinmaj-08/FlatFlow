package flatflow.web;

import flatflow.model.UserAccount;
import flatflow.service.ApiException;
import flatflow.service.AuthService;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Set;

/**
 * Runs before every /api request (architecture step 2). Reads "Authorization: Bearer <token>",
 * finds the signed-in user, and stores them on the request. Register and login don't need a token.
 */
public class SessionChecker implements Handler {
    private static final Set<String> PUBLIC_PATHS = Set.of("/api/register", "/api/login");
    private static final String USER_KEY = "flatflow.user";

    private final AuthService auth;

    public SessionChecker(AuthService auth) { this.auth = auth; }

    @Override
    public void handle(Context ctx) {
        if (PUBLIC_PATHS.contains(ctx.path())) return;
        UserAccount user = auth.userForToken(token(ctx))
                .orElseThrow(() -> ApiException.unauthorized("Please log in again"));
        ctx.attribute(USER_KEY, user);
    }

    /** The token sent by the browser, or null. */
    public static String token(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring("Bearer ".length()).trim();
    }

    /** The signed-in user for this request (set by handle above). */
    public static UserAccount account(Context ctx) {
        return ctx.attribute(USER_KEY);
    }
}
