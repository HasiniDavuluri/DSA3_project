package mediamine.npc;

import java.util.*;

/**
 * 2-approximation algorithm for Minimum Vertex Cover.
 * MediaMine use case: exact vertex cover (see CliqueToIndependentSetToVertexCover)
 * is NP-hard and only tractable for small graphs; for large content-relationship
 * graphs, this greedy edge-picking approximation guarantees a cover at most
 * 2x the optimal size, in O(V+E) time.
 */
public class VertexCoverApprox {

    /** edges: list of [u, v] pairs representing "important content relationships" to cover. */
    public static List<Integer> approximate(int numVertices, List<int[]> edges) {
        Set<Integer> cover = new HashSet<>();
        List<int[]> remaining = new ArrayList<>(edges);

        while (!remaining.isEmpty()) {
            int[] edge = remaining.remove(remaining.size() - 1);
            int u = edge[0], v = edge[1];
            if (cover.contains(u) || cover.contains(v)) continue; // already covered

            cover.add(u);
            cover.add(v);

            // remove all edges now covered by u or v
            remaining.removeIf(e -> e[0] == u || e[1] == u || e[0] == v || e[1] == v);
        }
        return new ArrayList<>(cover);
    }

    public static void main(String[] args) {
        List<int[]> relationships = new ArrayList<>();
        relationships.add(new int[]{0, 1});
        relationships.add(new int[]{1, 2});
        relationships.add(new int[]{2, 3});
        relationships.add(new int[]{3, 4});
        relationships.add(new int[]{4, 0});
        relationships.add(new int[]{1, 3});

        List<Integer> cover = approximate(5, relationships);
        System.out.println("Approximate vertex cover (<= 2x optimal): " + cover);
        System.out.println("Cover size: " + cover.size());
    }
}
