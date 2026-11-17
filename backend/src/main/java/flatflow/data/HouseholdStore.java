package flatflow.data;

import flatflow.model.Household;
import flatflow.model.UserAccount;

import java.util.List;
import java.util.Optional;

/** Households and who lives in them. */
public interface HouseholdStore {
    /** Creates the household and puts the organizer in it, in one transaction. */
    Household create(String name, int organizerUserId, String inviteCode);
    Optional<Household> findById(int householdId);
    Optional<Household> findByInviteCode(String inviteCode);
    List<UserAccount> members(int householdId);
}
