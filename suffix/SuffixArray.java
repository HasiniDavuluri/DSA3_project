package mediamine.suffix;

import java.util.Arrays;

/**
 * Suffix Array construction (O(n log^2 n) doubling method).
 * MediaMine use case: index transcripts and news articles for fast
 * substring / content retrieval queries.
 */
public class SuffixArray {

    private final String text;
    private final int[] suffixArray;

    public SuffixArray(String text) {
        this.text = text + "\u0000"; // sentinel, smaller than any real char
        this.suffixArray = build(this.text);
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    public String getText() {
        return text;
    }

    private static int[] build(String s) {
        int n = s.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] tmp = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = s.charAt(i);
        }

        for (int k = 1; k < n; k <<= 1) {
            final int kk = k;
            final int[] r = rank;
            java.util.Comparator<Integer> cmp = (a, b) -> {
                if (r[a] != r[b]) return Integer.compare(r[a], r[b]);
                int ra = (a + kk < n) ? r[a + kk] : -1;
                int rb = (b + kk < n) ? r[b + kk] : -1;
                return Integer.compare(ra, rb);
            };
            Arrays.sort(sa, cmp);

            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp.compare(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            rank = tmp.clone();
            if (rank[sa[n - 1]] == n - 1) break;
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = sa[i];
        return result;
    }

    /** Binary search for a pattern in the suffix array. Returns starting index or -1. */
    public int search(String pattern) {
        int lo = 0, hi = suffixArray.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            String suffix = text.substring(suffixArray[mid]);
            int cmp = suffix.length() >= pattern.length()
                    ? suffix.substring(0, pattern.length()).compareTo(pattern)
                    : suffix.compareTo(pattern);
            if (cmp == 0) return suffixArray[mid];
            else if (cmp < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        SuffixArray sa = new SuffixArray("banana media banana report");
        System.out.println("Suffix array: " + Arrays.toString(sa.getSuffixArray()));
        System.out.println("Search 'banana' at index: " + sa.search("banana"));
    }
}
