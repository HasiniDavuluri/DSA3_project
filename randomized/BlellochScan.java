package mediamine.randomized;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Blelloch work-efficient parallel prefix sum (exclusive scan): up-sweep
 * (reduce) phase followed by down-sweep phase, O(n) work, O(log n) depth.
 * MediaMine use case: cumulative media statistics -- e.g. running totals of
 * views/engagement over time buckets -- computed in parallel across cores.
 */
public class BlellochScan {

    /** Computes the exclusive prefix sum of `input` in place (array length must be a power of 2). */
    public static void exclusiveScan(long[] input) {
        int n = input.length;
        if ((n & (n - 1)) != 0) throw new IllegalArgumentException("Length must be a power of 2");

        // Up-sweep (reduce) phase
        for (int d = 0; d < log2(n); d++) {
            int stride = 1 << (d + 1);
            for (int i = 0; i + stride - 1 < n; i += stride) {
                input[i + stride - 1] += input[i + (stride / 2) - 1];
            }
        }

        input[n - 1] = 0; // set identity at the root

        // Down-sweep phase
        for (int d = log2(n) - 1; d >= 0; d--) {
            int stride = 1 << (d + 1);
            for (int i = 0; i + stride - 1 < n; i += stride) {
                long left = input[i + (stride / 2) - 1];
                input[i + (stride / 2) - 1] = input[i + stride - 1];
                input[i + stride - 1] += left;
            }
        }
    }

    private static int log2(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    /** Pads an arbitrary-length array with zeros up to the next power of 2, for convenience. */
    public static long[] padToPowerOfTwo(long[] data) {
        int n = 1;
        while (n < data.length) n <<= 1;
        return Arrays.copyOf(data, n);
    }

    public static void main(String[] args) {
        long[] hourlyViews = {5, 3, 8, 2, 9, 1, 4, 7}; // already length 8 (power of 2)
        long[] scan = hourlyViews.clone();
        exclusiveScan(scan);
        System.out.println("Hourly views:            " + Arrays.toString(hourlyViews));
        System.out.println("Cumulative views (excl): " + Arrays.toString(scan));
    }
}
