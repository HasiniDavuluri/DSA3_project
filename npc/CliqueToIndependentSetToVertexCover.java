package mediamine.npc;

import java.util.*;

/**
 * Reduction chain: CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER.
 * MediaMine use case: model "content conflict" relationships (e.g. two
 * pieces of content cannot both be scheduled together due to rights or
 * category overlap) and relate finding compatible content sets (clique in
 * the complement graph / independent set) to finding a minimal set of
 * content items to REMOVE so no conflicts remain (vertex cover).
 *
 * Key facts used:
 *  - G has a clique of size k  <=>  complement(G) has an independent set of size k.
 *  - G has an independent set of size k  <=>  G has a vertex cover of size (n - k).
 */
public class CliqueToIndependentSetToVertexCover {

    private final int n;
    private final boolean[][] adjacency; // conflict graph: true = conflict edge

    public CliqueToIndependentSetToVertexCover(int n) {
        this.n = n;
        this.adjacency = new boolean[n][n];
    }

    public void addConflict(int u, int v) {
        adjacency[u][v] = adjacency[v][u] = true;
    }

    /** Complement graph: edge exists where the original graph has NO conflict. */
    private boolean[][] complement() {
        boolean[][] comp = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) comp[i][j] = !adjacency[i][j];
            }
        }
        return comp;
    }

    /** Finds the maximum clique in the complement graph == max independent set in original. */
    public List<Integer> maxIndependentSetViaClique() {
        boolean[][] comp = complement();
        List<Integer> best = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) candidates.add(i);
        bronKerbosch(comp, current, candidates, new ArrayList<>(), best);
        return best;
    }

    private void bronKerbosch(boolean[][] g, List<Integer> r, List<Integer> p, List<Integer> x, List<Integer> best) {
        if (p.isEmpty() && x.isEmpty()) {
            if (r.size() > best.size()) {
                best.clear();
                best.addAll(r);
            }
            return;
        }
        List<Integer> pCopy = new ArrayList<>(p);
        for (int v : pCopy) {
            List<Integer> newR = new ArrayList<>(r);
            newR.add(v);
            List<Integer> newP = new ArrayList<>();
            List<Integer> newX = new ArrayList<>();
            for (int u : p) if (g[v][u]) newP.add(u);
            for (int u : x) if (g[v][u]) newX.add(u);
            bronKerbosch(g, newR, newP, newX, best);
            p.remove(Integer.valueOf(v));
            x.add(v);
        }
    }

    /** Minimum vertex cover = all nodes NOT in the maximum independent set. */
    public List<Integer> minVertexCover() {
        List<Integer> independentSet = maxIndependentSetViaClique();
        List<Integer> cover = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!independentSet.contains(i)) cover.add(i);
        }
        return cover;
    }

    public static void main(String[] args) {
        // content items 0..4; conflicts among them
        CliqueToIndependentSetToVertexCover model = new CliqueToIndependentSetToVertexCover(5);
        model.addConflict(0, 1);
        model.addConflict(1, 2);
        model.addConflict(2, 3);
        model.addConflict(3, 4);
        model.addConflict(4, 0);

        System.out.println("Max compatible content set (independent set): " + model.maxIndependentSetViaClique());
        System.out.println("Min content items to drop to resolve all conflicts (vertex cover): " + model.minVertexCover());
    }
}
