package flatflow.web;

import flatflow.service.AuthService;
import io.javalin.Javalin;

import java.util.Map;

/** POST /api/register, /api/login, /api/logout (frontend: register.js, login.js). */
public class AuthController {
    record RegisterRequest(String displayName, String loginName, String password) {}
    record LoginRequest(String loginName, String password) {}

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    public void register(Javalin app) {
        app.post("/api/register", ctx -> {
            RegisterRequest r = Bodies.read(ctx, RegisterRequest.class);
            int id = auth.register(r.displayName(), r.loginName(), r.password());
            ctx.status(201).json(Map.of("id", id));
        });
        app.post("/api/login", ctx -> {
            LoginRequest r = Bodies.read(ctx, LoginRequest.class);
            ctx.json(auth.login(r.loginName(), r.password()));   // {token, householdId}
        });
        app.post("/api/logout", ctx -> {
            auth.logout(SessionChecker.token(ctx));
            ctx.json(Map.of("ok", true));
        });
    }
}
