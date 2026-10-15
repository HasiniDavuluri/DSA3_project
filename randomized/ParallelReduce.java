package mediamine.randomized;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Parallel Reduce using the fork/join framework (divide-and-conquer,
 * work-stealing scheduler).
 * MediaMine use case: large-scale media analytics, e.g. summing total watch
 * time or engagement across millions of records, parallelized across CPU
 * cores.
 */
public class ParallelReduce {

    private static final int THRESHOLD = 1000;

    private static class SumTask extends RecursiveTask<Long> {
        private final long[] data;
        private final int lo, hi;

        SumTask(long[] data, int lo, int hi) {
            this.data = data; this.lo = lo; this.hi = hi;
        }

        @Override
        protected Long compute() {
            if (hi - lo <= THRESHOLD) {
                long sum = 0;
                for (int i = lo; i < hi; i++) sum += data[i];
                return sum;
            }
            int mid = (lo + hi) / 2;
            SumTask left = new SumTask(data, lo, mid);
            SumTask right = new SumTask(data, mid, hi);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    public static long parallelSum(long[] data) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.invoke(new SumTask(data, 0, data.length));
    }

    public static void main(String[] args) {
        long[] watchTimesSeconds = new long[5_000_000];
        for (int i = 0; i < watchTimesSeconds.length; i++) watchTimesSeconds[i] = (i % 300) + 1;

        long start = System.nanoTime();
        long total = parallelSum(watchTimesSeconds);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Total watch time across " + watchTimesSeconds.length
                + " records: " + total + " seconds (computed in " + elapsedMs + " ms using "
                + Runtime.getRuntime().availableProcessors() + " cores)");
    }
}
