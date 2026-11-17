package flatflow.model;

/** A roommate who can add chores and expenses. Has no extra rights. */
public class Member extends HouseholdUser {
    public Member(int id, String displayName, int householdId) { super(id, displayName, householdId); }
    @Override public String role() { return "Member"; }
}
