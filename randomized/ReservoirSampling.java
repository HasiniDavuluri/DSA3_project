package mediamine.randomized;

import java.util.*;

/**
 * Reservoir Sampling (Algorithm R).
 * MediaMine use case: continuous viewing-event streams are unbounded --
 * this maintains a uniformly-random sample of k events seen so far without
 * storing the entire stream, useful for real-time analytics/sampling audits.
 */
public class ReservoirSampling<T> {

    private final int k;
    private final List<T> reservoir;
    private long count = 0;
    private final Random random = new Random();

    public ReservoirSampling(int k) {
        this.k = k;
        this.reservoir = new ArrayList<>(k);
    }

    /** Call once per incoming stream event. */
    public void add(T event) {
        count++;
        if (reservoir.size() < k) {
            reservoir.add(event);
        } else {
            int j = random.nextInt((int) count); // uniform in [0, count-1]
            if (j < k) {
                reservoir.set(j, event);
            }
        }
    }

    public List<T> getSample() {
        return new ArrayList<>(reservoir);
    }

    public static void main(String[] args) {
        ReservoirSampling<String> sampler = new ReservoirSampling<>(3);
        String[] stream = {"view:v1", "view:v2", "view:v3", "view:v4", "view:v5",
                            "view:v6", "view:v7", "view:v8", "view:v9", "view:v10"};
        for (String event : stream) sampler.add(event);
        System.out.println("Random sample of 3 viewing events from a stream of " + stream.length + ": "
                + sampler.getSample());
    }
}
