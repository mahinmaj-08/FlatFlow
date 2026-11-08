import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splits an amount in integer cents equally between members.
 *
 * Rule being tested in this spike: the base share is totalCents / n (rounded
 * down). The leftover cents (totalCents % n) are given one each to the members
 * with the lowest user ids. The shares always add up exactly to the total.
 */
public class EqualSplitter {

    public static List<Share> split(long totalCents, List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new IllegalArgumentException("At least one member is required");
        }
        if (totalCents < 0) {
            throw new IllegalArgumentException("Total must not be negative");
        }
        Set<Integer> unique = new HashSet<>(memberIds);
        if (unique.size() != memberIds.size()) {
            throw new IllegalArgumentException("Duplicate member ids");
        }

        List<Integer> sorted = new ArrayList<>(memberIds);
        sorted.sort(Integer::compare);

        long base = totalCents / sorted.size();
        long leftover = totalCents % sorted.size();

        List<Share> shares = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            long cents = base + (i < leftover ? 1 : 0);
            shares.add(new Share(sorted.get(i), cents));
        }
        return shares;
    }
}
