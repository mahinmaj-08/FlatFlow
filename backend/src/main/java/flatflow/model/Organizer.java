package flatflow.model;

/**
 * The roommate who created the household. Service methods that only an organizer may call
 * (e.g. ChoreService.deleteChore) take an Organizer parameter, so a Member can't be passed in.
 */
public class Organizer extends HouseholdUser {
    public Organizer(int id, String displayName, int householdId) { super(id, displayName, householdId); }
    @Override public String role() { return "Organizer"; }
}
