package mediamine.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt algorithm.
 * MediaMine use case: search a keyword inside a news article / transcript in O(n + m).
 */
public class KMP {

    /** Builds the failure/partial-match table for the pattern. */
    private static int[] buildFailureTable(String pattern) {
        int m = pattern.length();
        int[] fail = new int[m];
        int k = 0;
        for (int i = 1; i < m; i++) {
            while (k > 0 && pattern.charAt(i) != pattern.charAt(k)) {
                k = fail[k - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            fail[i] = k;
        }
        return fail;
    }

    /** Returns all starting indices in {@code text} where {@code pattern} occurs. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty() || text.length() < pattern.length()) return matches;

        int[] fail = buildFailureTable(pattern);
        int n = text.length(), m = pattern.length();
        int k = 0;
        for (int i = 0; i < n; i++) {
            while (k > 0 && text.charAt(i) != pattern.charAt(k)) {
                k = fail[k - 1];
            }
            if (text.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            if (k == m) {
                matches.add(i - m + 1);
                k = fail[k - 1];
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        String transcript = "the media platform indexes media content across media channels";
        String keyword = "media";
        List<Integer> hits = search(transcript, keyword);
        System.out.println("KMP: keyword \"" + keyword + "\" found at indices: " + hits);
    }
}
