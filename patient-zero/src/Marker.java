import java.util.*;

/**
 * Missions 3 and 4. Find a word in a long text.
 */
public class Marker {

    /**
     * Mission 3a. The prefix function of p. pi[i] = the length of the longest proper prefix of p[0..i] that is
     * also a suffix of p[0..i] ("proper" means shorter than p[0..i] itself).
     * Example: p = "ACGAC" gives [0, 0, 0, 1, 2]. The last number is the "Border length" line.
     */
    public static int[] prefixFunction(String p) {
        int[] pi = new int[p.length()];
        for (int i = 1; i < p.length(); i++) {
            int matched = pi[i - 1];
            while (matched > 0 && p.charAt(i) != p.charAt(matched)) {
                matched = pi[matched - 1];
            }
            if (p.charAt(i) == p.charAt(matched)) matched++;
            pi[i] = matched;
        }
        return pi;
    }

    /**
     * Mission 3b. KMP. All start positions (counting from 0, in increasing order) where p occurs in text.
     * Overlapping occurrences count: "ACG" occurs in "ACGACG" at 0 and 3, and "AA" in "AAA" at 0 and 1.
     * Use the prefix function: the text pointer never goes back. The number of positions is "Marker hits",
     * the first one is "First hit" (-1 when there is none).
     */
    public static int[] findAll(String text, String p) {
        if (p.isEmpty()) return new int[0];

        int[] pi = prefixFunction(p);
        int[] hits = new int[text.length()];
        int count = 0;
        int matched = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            while (matched > 0 && letter != p.charAt(matched)) {
                matched = pi[matched - 1];
            }
            if (letter == p.charAt(matched)) matched++;
            if (matched == p.length()) {
                hits[count++] = i - p.length() + 1;
                matched = pi[matched - 1];
            }
        }
        return Arrays.copyOf(hits, count);
    }

    /**
     * Mission 4. Rabin-Karp. The number of positions i in a such that the word of k letters starting at i
     * (a[i..i+k-1]) also occurs somewhere in b. Count positions, not different words. This is the "Shared words" line.
     * The idea: give every window of k letters a number (a rolling hash, updated in O(1) when the window moves by one),
     * look the number up among the windows of b, and when it is found COMPARE THE LETTERS, because two different words
     * can have the same hash.
     * Example: a = "ACGTAC", b = "TTACGT", k = 4: the words of a are ACGT, CGTA, GTAC; only ACGT is in b, so the answer is 1.
     */
    public static int sharedWords(String a, String b, int k) {
        if (k <= 0 || k > a.length() || k > b.length()) return 0;

        long leadingPower = 1;
        for (int i = 1; i < k; i++) leadingPower = leadingPower * BASE % MOD;

        Map<Long, List<Integer>> positions = new HashMap<>();
        long hash = firstHash(b, k);
        for (int start = 0; start <= b.length() - k; start++) {
            if (start > 0) hash = slide(hash, b, start, k, leadingPower);
            positions.computeIfAbsent(hash, key -> new ArrayList<>()).add(start);
        }

        int shared = 0;
        hash = firstHash(a, k);
        for (int start = 0; start <= a.length() - k; start++) {
            if (start > 0) hash = slide(hash, a, start, k, leadingPower);
            for (int otherStart : positions.getOrDefault(hash, Collections.emptyList())) {
                if (sameWord(a, start, b, otherStart, k)) {
                    shared++;
                    break;
                }
            }
        }
        return shared;
    }

    private static final long BASE = 257;
    private static final long MOD = 1_000_000_007;

    private static long firstHash(String text, int k) {
        long hash = 0;
        for (int i = 0; i < k; i++) hash = (hash * BASE + text.charAt(i)) % MOD;
        return hash;
    }

    private static long slide(long hash, String text, int start, int k, long leadingPower) {
        long outgoing = text.charAt(start - 1) * leadingPower % MOD;
        long rest = (hash - outgoing + MOD) % MOD;
        return (rest * BASE + text.charAt(start + k - 1)) % MOD;
    }

    private static boolean sameWord(String a, int startA, String b, int startB, int k) {
        for (int offset = 0; offset < k; offset++) {
            if (a.charAt(startA + offset) != b.charAt(startB + offset)) return false;
        }
        return true;
    }
}
