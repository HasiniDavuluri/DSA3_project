package mediamine.suffix;

import java.util.Arrays;

/**
 * SA-IS: linear-time (O(n)) suffix array construction via Induced Sorting.
 * MediaMine use case: large-scale transcript indexing where O(n log n)
 * comparison-sort suffix arrays are too slow.
 *
 * Classic implementation of Nong, Zhang and Chen's SA-IS algorithm.
 */
public class SAIS {

    /** Builds a suffix array for the given text (converted to an int array with a sentinel 0). */
    public static int[] build(String text) {
        int n = text.length();
        int[] s = new int[n + 1];
        int maxChar = 0;
        for (int i = 0; i < n; i++) {
            s[i] = text.charAt(i) + 1; // shift so 0 is reserved for sentinel
            maxChar = Math.max(maxChar, s[i]);
        }
        s[n] = 0; // sentinel, strictly smallest
        int[] sa = new int[n + 1];
        saisRecursive(s, sa, maxChar + 1);
        // Drop the sentinel's own position (sa[0] is always the sentinel suffix)
        return Arrays.copyOfRange(sa, 1, n + 1);
    }

    private static void saisRecursive(int[] s, int[] sa, int alphabetSize) {
        int n = s.length;
        boolean[] isS = new boolean[n]; // true = S-type, false = L-type
        isS[n - 1] = true;
        for (int i = n - 2; i >= 0; i--) {
            isS[i] = (s[i] < s[i + 1]) || (s[i] == s[i + 1] && isS[i + 1]);
        }

        boolean[] isLMS = new boolean[n];
        for (int i = 1; i < n; i++) {
            isLMS[i] = isS[i] && !isS[i - 1];
        }

        int[] bucketSizes = new int[alphabetSize];
        for (int c : s) bucketSizes[c]++;

        Arrays.fill(sa, -1);
        induceSort(s, sa, isS, isLMS, bucketSizes, alphabetSize);

        // Collect sorted LMS substrings, name them
        int[] lmsIndices = new int[n];
        int numLms = 0;
        for (int i = 0; i < n; i++) {
            if (sa[i] > 0 && isLMS[sa[i]]) lmsIndices[numLms++] = sa[i];
        }

        int[] names = new int[n];
        Arrays.fill(names, -1);
        int name = 0;
        names[lmsIndices[0]] = name;
        for (int i = 1; i < numLms; i++) {
            int prev = lmsIndices[i - 1], cur = lmsIndices[i];
            boolean diff = false;
            int d = 0;
            while (true) {
                boolean atLmsPrev = (d > 0) && isLMS[prev + d];
                boolean atLmsCur = (d > 0) && isLMS[cur + d];
                if (atLmsPrev && atLmsCur) break;
                if (s[prev + d] != s[cur + d] || isS[prev + d] != isS[cur + d] || atLmsPrev != atLmsCur) {
                    diff = true;
                    break;
                }
                d++;
            }
            if (diff) name++;
            names[cur] = name;
        }

        int[] summary = new int[numLms];
        for (int i = 0, j = 0; i < n; i++) {
            if (names[i] != -1) summary[j++] = names[i];
        }

        int[] summarySa = new int[numLms];
        if (name + 1 < numLms) {
            saisRecursive(summary, summarySa, name + 1);
        } else {
            for (int i = 0; i < numLms; i++) summarySa[summary[i]] = i;
        }

        int[] sortedLms = new int[numLms];
        for (int i = 0; i < numLms; i++) sortedLms[i] = lmsIndices[summarySa[i]];

        Arrays.fill(sa, -1);
        induceSortFinal(s, sa, isS, isLMS, bucketSizes, alphabetSize, sortedLms);
    }

    private static int[] bucketHeads(int[] bucketSizes, int alphabetSize) {
        int[] heads = new int[alphabetSize];
        int sum = 0;
        for (int c = 0; c < alphabetSize; c++) {
            heads[c] = sum;
            sum += bucketSizes[c];
        }
        return heads;
    }

    private static int[] bucketTails(int[] bucketSizes, int alphabetSize) {
        int[] tails = new int[alphabetSize];
        int sum = 0;
        for (int c = 0; c < alphabetSize; c++) {
            sum += bucketSizes[c];
            tails[c] = sum - 1;
        }
        return tails;
    }

    private static void induceSort(int[] s, int[] sa, boolean[] isS, boolean[] isLMS,
                                    int[] bucketSizes, int alphabetSize) {
        int n = s.length;
        int[] tails = bucketTails(bucketSizes, alphabetSize);
        for (int i = n - 1; i >= 1; i--) {
            if (isLMS[i]) sa[tails[s[i]]--] = i;
        }
        placeLAndS(s, sa, isS, bucketSizes, alphabetSize);
    }

    private static void induceSortFinal(int[] s, int[] sa, boolean[] isS, boolean[] isLMS,
                                         int[] bucketSizes, int alphabetSize, int[] sortedLms) {
        int n = s.length;
        int[] tails = bucketTails(bucketSizes, alphabetSize);
        for (int i = sortedLms.length - 1; i >= 0; i--) {
            int idx = sortedLms[i];
            sa[tails[s[idx]]--] = idx;
        }
        placeLAndS(s, sa, isS, bucketSizes, alphabetSize);
    }

    private static void placeLAndS(int[] s, int[] sa, boolean[] isS, int[] bucketSizes, int alphabetSize) {
        int n = s.length;
        int[] heads = bucketHeads(bucketSizes, alphabetSize);
        for (int i = 0; i < n; i++) {
            int j = sa[i] - 1;
            if (sa[i] > 0 && !isS[j]) {
                sa[heads[s[j]]++] = j;
            }
        }
        int[] tails = bucketTails(bucketSizes, alphabetSize);
        for (int i = n - 1; i >= 0; i--) {
            int j = sa[i] - 1;
            if (sa[i] > 0 && isS[j]) {
                sa[tails[s[j]]--] = j;
            }
        }
    }

    public static void main(String[] args) {
        String text = "media analytics media reports media";
        int[] sa = build(text);
        System.out.println("SA-IS suffix array for \"" + text + "\": " + Arrays.toString(sa));
    }
}
