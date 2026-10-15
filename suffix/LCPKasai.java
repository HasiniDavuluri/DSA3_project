package mediamine.suffix;

/**
 * Kasai's algorithm: builds the LCP (Longest Common Prefix) array in O(n)
 * from a text and its suffix array.
 * MediaMine use case: identify common phrases between reports (the longest
 * common substring between any two documents relates directly to LCP values).
 */
public class LCPKasai {

    /** Returns lcp[] where lcp[i] = LCP(suffixArray[i-1], suffixArray[i]); lcp[0] = 0. */
    public static int[] buildLCPArray(String text, int[] suffixArray) {
        int n = text.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[suffixArray[i]] = i;

        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = suffixArray[rank[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    /** Finds the longest common substring between two reports, joined with a separator. */
    public static String longestCommonPhrase(String reportA, String reportB) {
        String combined = reportA + "\u0001" + reportB;
        SuffixArray sa = new SuffixArray(combined);
        int[] suffixArray = sa.getSuffixArray();
        // SuffixArray appends a trailing sentinel ('\u0000', the smallest possible
        // character) internally. Its suffix therefore sorts FIRST in suffixArray,
        // not last -- so build the LCP array over the sentinel-inclusive text and
        // full-length suffixArray together, rather than truncating either one.
        String textWithSentinel = sa.getText();
        int[] lcp = buildLCPArray(textWithSentinel, suffixArray);

        int splitIndex = reportA.length();
        int best = 0, bestPos = -1;
        for (int i = 1; i < lcp.length; i++) {
            int posA = suffixArray[i - 1];
            int posB = suffixArray[i];
            boolean oneFromEach = (posA < splitIndex) != (posB < splitIndex);
            if (oneFromEach && lcp[i] > best) {
                best = lcp[i];
                bestPos = posA < splitIndex ? posA : posB;
            }
        }
        // best/bestPos are drawn only from real content on either side of the
        // separator, so this always lands within `combined` (never touches the
        // separator or sentinel characters).
        return bestPos == -1 ? "" : combined.substring(bestPos, bestPos + best);
    }

    public static void main(String[] args) {
        String reportA = "the quarterly media engagement report shows growth";
        String reportB = "our media engagement report indicates a decline";
        System.out.println("Longest common phrase: \"" + longestCommonPhrase(reportA, reportB) + "\"");
    }
}
