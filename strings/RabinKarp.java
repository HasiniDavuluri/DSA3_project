package mediamine.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp rolling-hash string search.
 * MediaMine use case: quickly search for media identifiers (e.g. content IDs,
 * hashtags) inside large text corpora using rolling hashes.
 */
public class RabinKarp {

    private static final long BASE = 256L;
    private static final long MOD = 1_000_000_007L;

    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return matches;

        long highOrder = 1;
        for (int i = 0; i < m - 1; i++) highOrder = (highOrder * BASE) % MOD;

        long patternHash = 0, windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {
            if (windowHash == patternHash && text.regionMatches(i, pattern, 0, m)) {
                matches.add(i);
            }
            if (i < n - m) {
                windowHash = (windowHash - text.charAt(i) * highOrder % MOD + MOD) % MOD;
                windowHash = (windowHash * BASE + text.charAt(i + m)) % MOD;
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        String corpus = "video:MID-4021 caption:MID-4099 video:MID-4021 again";
        String id = "MID-4021";
        System.out.println("Rabin-Karp: identifier \"" + id + "\" found at: " + search(corpus, id));
    }
}
