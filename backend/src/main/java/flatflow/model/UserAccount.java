package flatflow.model;

/** A row of the user table. Includes the password hash, so it is never sent to the browser. */
public record UserAccount(int id, String displayName, String loginName, String passwordHash, Integer householdId) {
    public boolean hasHousehold() { return householdId != null; }
}
