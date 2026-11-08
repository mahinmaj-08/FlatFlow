import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Plain-Java tests: no libraries needed. Run with: java EqualSplitterTest.java */
public class EqualSplitterTest {
    static int passed = 0;

    static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("ok   " + name);
    }

    static long sum(List<Share> shares) {
        return shares.stream().mapToLong(Share::cents).sum();
    }

    static boolean throwsIllegalArgument(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) {
        // 1. The case from the proposal: $10.00 between 3 people
        List<Share> s = EqualSplitter.split(1000, List.of(1, 2, 3));
        check(s.equals(List.of(new Share(1, 334), new Share(2, 333), new Share(3, 333))),
              "$10.00 between 3 gives 3.34 / 3.33 / 3.33");

        // 2. Exact division has no leftover
        s = EqualSplitter.split(900, List.of(1, 2, 3));
        check(s.stream().allMatch(x -> x.cents() == 300), "$9.00 between 3 gives 3.00 each");

        // 3. Fewer cents than members
        s = EqualSplitter.split(1, List.of(1, 2, 3));
        check(s.equals(List.of(new Share(1, 1), new Share(2, 0), new Share(3, 0))),
              "1 cent between 3 gives 1 / 0 / 0");

        // 4. Zero total
        check(sum(EqualSplitter.split(0, List.of(4, 5))) == 0, "zero total gives zero shares");

        // 5. One member pays for themselves
        check(EqualSplitter.split(1234, List.of(7)).equals(List.of(new Share(7, 1234))),
              "single member gets the whole amount");

        // 6. Input order does not change who gets the leftover cent
        s = EqualSplitter.split(1000, List.of(3, 1, 2));
        check(s.get(0).userId() == 1 && s.get(0).cents() == 334,
              "lowest user id gets the leftover cent, whatever the input order");

        // 7. Property test: shares always add up to the total and differ by at most 1 cent
        Random rnd = new Random(42);
        boolean allOk = true;
        for (int i = 0; i < 20000; i++) {
            long total = rnd.nextInt(2_000_000);
            int n = 1 + rnd.nextInt(12);
            List<Integer> ids = new ArrayList<>();
            for (int k = 1; k <= n; k++) ids.add(k * 10);
            Collections.shuffle(ids, rnd);
            List<Share> r = EqualSplitter.split(total, ids);
            long min = r.stream().mapToLong(Share::cents).min().getAsLong();
            long max = r.stream().mapToLong(Share::cents).max().getAsLong();
            if (sum(r) != total || max - min > 1) { allOk = false; break; }
        }
        check(allOk, "20,000 random splits: sum is exact and shares differ by at most 1 cent");

        // 8. Bad input is rejected
        check(throwsIllegalArgument(() -> EqualSplitter.split(100, List.of())), "no members rejected");
        check(throwsIllegalArgument(() -> EqualSplitter.split(-1, List.of(1))), "negative total rejected");
        check(throwsIllegalArgument(() -> EqualSplitter.split(100, Arrays.asList(1, 1))), "duplicate ids rejected");

        System.out.println("\nAll " + passed + " checks passed.");
    }
}
