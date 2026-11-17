package flatflow.web;

import flatflow.model.Household;
import flatflow.service.HouseholdService;
import io.javalin.Javalin;

import java.util.Map;

/** POST /api/households, POST /api/households/join, GET /api/household (frontend: household.js). */
public class HouseholdController {
    record CreateRequest(String name) {}
    record JoinRequest(String inviteCode) {}

    private final HouseholdService households;

    public HouseholdController(HouseholdService households) { this.households = households; }

    public void register(Javalin app) {
        app.post("/api/households", ctx -> {
            CreateRequest r = Bodies.read(ctx, CreateRequest.class);
            Household h = households.create(SessionChecker.account(ctx), r.name());
            ctx.status(201).json(Map.of("id", h.id(), "inviteCode", h.inviteCode()));
        });
        app.post("/api/households/join", ctx -> {
            JoinRequest r = Bodies.read(ctx, JoinRequest.class);
            Household h = households.join(SessionChecker.account(ctx), r.inviteCode());
            ctx.json(Map.of("householdId", h.id()));
        });
        app.get("/api/household", ctx ->
            ctx.json(households.view(households.householdUser(SessionChecker.account(ctx)))));
    }
}
