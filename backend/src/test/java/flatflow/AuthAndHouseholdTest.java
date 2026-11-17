package flatflow;

import flatflow.model.*;
import flatflow.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Sign-in and households working together, using in-memory stores (no database file needed). */
class AuthAndHouseholdTest {
    /** A fast fake instead of bcrypt, so tests don't wait on real hashing. */
    static class FakeHasher implements PasswordHasher {
        public String hash(String password) { return "hashed:" + password; }
        public boolean matches(String password, String hash) { return hash.equals("hashed:" + password); }
    }

    private InMemoryStores stores;
    private AuthService auth;
    private HouseholdService households;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        stores = new InMemoryStores();
        clock = new MutableClock(Instant.parse("2026-11-15T12:00:00Z"));
        auth = new AuthService(stores.accounts, new FakeHasher(), 168, clock);
        households = new HouseholdService(stores.households, stores.accounts, 6);
    }

    private UserAccount account(int id) { return stores.accounts.findById(id).orElseThrow(); }

    @Test
    void registerLoginAndLogout() {
        int id = auth.register("Alex", "alex_1", "password123");
        AuthService.LoginResult r = auth.login("alex_1", "password123");
        assertNull(r.householdId());
        assertEquals(id, auth.userForToken(r.token()).orElseThrow().id());

        auth.logout(r.token());
        assertTrue(auth.userForToken(r.token()).isEmpty());
    }

    @Test
    void wrongPasswordAndDuplicateLoginAreRejected() {
        auth.register("Alex", "alex_1", "password123");
        assertEquals(401, assertThrows(ApiException.class, () -> auth.login("alex_1", "nope-nope")).status());
        assertEquals(409, assertThrows(ApiException.class, () -> auth.register("Al", "alex_1", "password123")).status());
        assertEquals(400, assertThrows(ApiException.class, () -> auth.register("Bo", "bo", "password123")).status());
        assertEquals(400, assertThrows(ApiException.class, () -> auth.register("Bo", "bobby", "short")).status());
    }

    @Test
    void tokenExpiresAfterLifetime() {
        auth.register("Alex", "alex_1", "password123");
        String token = auth.login("alex_1", "password123").token();
        clock.advance(Duration.ofHours(169));
        assertTrue(auth.userForToken(token).isEmpty());
    }

    @Test
    void creatorIsOrganizerAndJoinerIsMember() {
        int alex = auth.register("Alex", "alex_1", "password123");
        int sam = auth.register("Sam", "sam_1", "password123");
        Household h = households.create(account(alex), "Maple St House");
        assertEquals(6, h.inviteCode().length());

        households.join(account(sam), h.inviteCode().toLowerCase());   // codes are not case-sensitive
        assertInstanceOf(Organizer.class, households.householdUser(account(alex)));
        assertInstanceOf(Member.class, households.householdUser(account(sam)));
        assertEquals(List.of("Organizer", "Member"),
                households.view(households.householdUser(account(alex))).members().stream()
                        .map(HouseholdService.MemberView::role).toList());

        assertEquals(409, assertThrows(ApiException.class, () -> households.join(account(sam), h.inviteCode())).status());
        assertEquals(404, assertThrows(ApiException.class,
                () -> households.householdUser(account(auth.register("Pat", "pat_1", "password123")))).status());
    }

    /** A clock the test can move forward, to check token expiry. */
    static class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant start) { now = start; }
        void advance(Duration d) { now = now.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}