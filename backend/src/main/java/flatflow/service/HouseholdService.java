package flatflow.service;

import flatflow.data.AccountStore;
import flatflow.data.HouseholdStore;
import flatflow.model.Household;
import flatflow.model.HouseholdUser;
import flatflow.model.UserAccount;

import java.security.SecureRandom;
import java.util.List;

/** Create a household, join one with an invite code, and see who is in it. */
public class HouseholdService {
    // No 0/O or 1/I, so codes are easy to read out loud
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HouseholdStore households;
    private final AccountStore accounts;
    private final int inviteCodeLength;

    public HouseholdService(HouseholdStore households, AccountStore accounts, int inviteCodeLength) {
        this.households = households;
        this.accounts = accounts;
        this.inviteCodeLength = inviteCodeLength;
    }

    /** The signed-in user creates a household and becomes its Organizer. */
    public Household create(UserAccount me, String name) {
        if (me.hasHousehold()) throw ApiException.conflict("You are already in a household");
        String clean = Check.text(name, "Household name", 1, 50);
        String code;
        do { code = newCode(); } while (households.findByInviteCode(code).isPresent());
        return households.create(clean, me.id(), code);
    }

    /** The signed-in user joins an existing household as a Member. */
    public Household join(UserAccount me, String inviteCode) {
        if (me.hasHousehold()) throw ApiException.conflict("You are already in a household");
        String code = inviteCode == null ? "" : inviteCode.trim().toUpperCase();
        Household h = households.findByInviteCode(code)
                .orElseThrow(() -> ApiException.notFound("No household has that invite code"));
        accounts.setHousehold(me.id(), h.id());
        return h;
    }

    /** Turns the signed-in account into a Member or Organizer. Fails if they have no household yet. */
    public HouseholdUser householdUser(UserAccount me) {
        if (!me.hasHousehold()) throw ApiException.notFound("You are not in a household yet");
        Household h = households.findById(me.householdId())
                .orElseThrow(() -> ApiException.notFound("You are not in a household yet"));
        return HouseholdUser.of(me, h);
    }

    public record MemberView(int id, String displayName, String role) {}
    public record HouseholdView(int id, String name, String inviteCode, List<MemberView> members) {}

    /** What the household page shows: name, invite code and members with their roles. */
    public HouseholdView view(HouseholdUser me) {
        Household h = households.findById(me.getHouseholdId()).orElseThrow();
        List<MemberView> members = households.members(h.id()).stream()
                .map(u -> HouseholdUser.of(u, h))
                .map(u -> new MemberView(u.getId(), u.getDisplayName(), u.role()))
                .toList();
        return new HouseholdView(h.id(), h.name(), h.inviteCode(), members);
    }

    public List<UserAccount> members(int householdId) {
        return households.members(householdId);
    }

    private String newCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inviteCodeLength; i++) sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        return sb.toString();
    }
}
