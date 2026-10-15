package mediamine.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Z-function algorithm.
 * MediaMine use case: identify repeated phrases within a transcript, and also
 * pattern matching via the "pattern + separator + text" trick.
 */
public class ZFunction {

    /** z[i] = length of the longest substring starting at i that matches a prefix of s. */
    public static int[] buildZArray(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i < r) {
                z[i] = Math.min(r - i, z[i - l]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
        }
        return z;
    }

    /** Finds all repeated phrases of a given length in the transcript using the Z-array. */
    public static List<String> findRepeatedPhrases(String transcript, int phraseLen) {
        List<String> repeats = new ArrayList<>();
        int n = transcript.length();
        for (int start = 0; start + phraseLen <= n; start++) {
            String suffix = transcript.substring(start);
            int[] z = buildZArray(suffix);
            for (int i = 1; i < z.length; i++) {
                if (z[i] >= phraseLen) {
                    String phrase = suffix.substring(0, phraseLen);
                    if (!repeats.contains(phrase)) repeats.add(phrase);
                    break;
                }
            }
        }
        return repeats;
    }

    /** Pattern search using Z-function: pattern + '#' + text. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        String combined = pattern + "\u0001" + text;
        int[] z = buildZArray(combined);
        int m = pattern.length();
        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] >= m) {
                matches.add(i - m - 1);
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        String transcript = "breaking news breaking news update on the story";
        System.out.println("Z-function search for 'breaking news': " + search(transcript, "breaking news"));
        System.out.println("Repeated 6-char phrases: " + findRepeatedPhrases(transcript, 6));
    }
}
