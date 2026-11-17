package flatflow.model;

/**
 * Someone signed in who belongs to a household (poster 2B, SOLID report section 3 / LSP).
 * Member and Organizer both extend this, so any page that only needs "a household user"
 * works with either one. Organizer-only actions ask for an Organizer instead.
 */
public abstract class HouseholdUser {
    private final int id;
    private final String displayName;
    private final int householdId;

    protected HouseholdUser(int id, String displayName, int householdId) {
        this.id = id;
        this.displayName = displayName;
        this.householdId = householdId;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getHouseholdId() { return householdId; }

    /** "Organizer" or "Member", as shown on the household page. */
    public abstract String role();

    /** Builds the right subclass: the household's creator is the Organizer, everyone else a Member. */
    public static HouseholdUser of(UserAccount account, Household household) {
        if (account.id() == household.organizerUserId()) {
            return new Organizer(account.id(), account.displayName(), household.id());
        }
        return new Member(account.id(), account.displayName(), household.id());
    }
}
