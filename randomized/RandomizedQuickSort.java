package mediamine.randomized;

import java.util.Random;

/**
 * Randomized QuickSort (random pivot selection to avoid worst-case O(n^2)
 * on adversarial/sorted input).
 * MediaMine use case: rank media content items (e.g. by engagement score)
 * with good expected performance regardless of input order.
 */
public class RandomizedQuickSort {

    private static final Random RANDOM = new Random();

    public static void sort(int[] scores) {
        quickSort(scores, 0, scores.length - 1);
    }

    private static void quickSort(int[] a, int lo, int hi) {
        if (lo < hi) {
            int p = randomizedPartition(a, lo, hi);
            quickSort(a, lo, p - 1);
            quickSort(a, p + 1, hi);
        }
    }

    private static int randomizedPartition(int[] a, int lo, int hi) {
        int randomIndex = lo + RANDOM.nextInt(hi - lo + 1);
        swap(a, randomIndex, hi);
        return partition(a, lo, hi);
    }

    private static int partition(int[] a, int lo, int hi) {
        int pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (a[j] <= pivot) {
                i++;
                swap(a, i, j);
            }
        }
        swap(a, i + 1, hi);
        return i + 1;
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    public static void main(String[] args) {
        int[] engagementScores = {87, 12, 56, 99, 34, 71, 5, 63};
        sort(engagementScores);
        System.out.println("Ranked media content (by engagement score): " + java.util.Arrays.toString(engagementScores));
    }
}
