package mediamine.randomized;

/**
 * Brent's Theorem: relates the work (W, total operations) and span (S,
 * length of the critical path / depth of the computation DAG) of a parallel
 * algorithm to its runtime on P processors:
 *
 *      T_P  <=  W / P  +  S
 *
 * MediaMine use case: estimate/bound the runtime of parallel transcript
 * processing (e.g. the parallel scan/reduce jobs above) on a given number
 * of worker threads, and decide how many workers are worth allocating.
 */
public class BrentTheorem {

    public static class Estimate {
        public final long work;
        public final long span;
        public final int processors;
        public final double estimatedTime;

        Estimate(long work, long span, int processors, double estimatedTime) {
            this.work = work;
            this.span = span;
            this.processors = processors;
            this.estimatedTime = estimatedTime;
        }

        @Override
        public String toString() {
            return String.format(
                "Work=%d, Span=%d, Processors=%d -> Brent's bound T_P <= %.2f (work/P + span)",
                work, span, processors, estimatedTime);
        }
    }

    /** Computes Brent's upper bound on runtime given work, span, and processor count. */
    public static Estimate estimateRuntime(long work, long span, int processors) {
        double bound = ((double) work / processors) + span;
        return new Estimate(work, span, processors, bound);
    }

    /** Suggests the number of processors beyond which adding more gives negligible speedup
     *  (the point where work/P approaches span, i.e. P ~= work/span). */
    public static int suggestOptimalProcessors(long work, long span) {
        if (span <= 0) return 1;
        return (int) Math.max(1, Math.round((double) work / span));
    }

    public static void main(String[] args) {
        // Example: processing a transcript involves 10,000,000 total character
        // operations (work), with a critical path (span) of ~24 steps
        // (e.g. log2(n) depth of a parallel scan over ~16M elements).
        long work = 10_000_000L;
        long span = 24L;

        for (int p : new int[]{1, 2, 4, 8, 16, 32}) {
            System.out.println(estimateRuntime(work, span, p));
        }
        System.out.println("Suggested processor count where returns diminish: "
                + suggestOptimalProcessors(work, span));
    }
}
