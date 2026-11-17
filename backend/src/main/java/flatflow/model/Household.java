package flatflow.model;

/** A group of roommates. The organizer created it; others join with the invite code. */
public record Household(int id, String name, int organizerUserId, String inviteCode) {}
