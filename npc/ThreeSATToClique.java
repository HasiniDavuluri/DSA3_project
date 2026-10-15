package mediamine.npc;

import java.util.*;

/**
 * Classic polynomial-time reduction: 3-SAT --> CLIQUE.
 * MediaMine use case: topic-compatibility analysis -- given constraints on
 * which topic-tags can co-occur, build a "compatibility graph" and find a
 * clique of size k representing a mutually-compatible set of topic choices.
 *
 * For each clause (3 literals) we create 3 nodes (one per literal). We
 * connect two nodes (from different clauses) with an edge if their literals
 * are NOT negations of each other. A satisfying assignment for the 3-SAT
 * instance exists iff the resulting graph has a clique of size = number of
 * clauses (picking exactly one consistent literal per clause).
 */
public class ThreeSATToClique {

    public static class Node {
        final int clauseIndex;
        final int literal; // signed variable
        Node(int clauseIndex, int literal) {
            this.clauseIndex = clauseIndex;
            this.literal = literal;
        }
        @Override public String toString() {
            return "clause" + clauseIndex + ":" + (literal > 0 ? "x" + literal : "NOT x" + (-literal));
        }
    }

    private final List<int[]> clauses;
    private List<Node> nodes;
    private boolean[][] adjacency;

    public ThreeSATToClique(List<int[]> clauses) {
        this.clauses = clauses;
    }

    /** Builds the compatibility graph. Returns the target clique size (= number of clauses). */
    public int buildGraph() {
        nodes = new ArrayList<>();
        for (int c = 0; c < clauses.size(); c++) {
            for (int lit : clauses.get(c)) {
                nodes.add(new Node(c, lit));
            }
        }
        int n = nodes.size();
        adjacency = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Node a = nodes.get(i), b = nodes.get(j);
                if (a.clauseIndex == b.clauseIndex) continue; // never connect within same clause
                if (a.literal == -b.literal) continue;        // negated literals conflict
                adjacency[i][j] = adjacency[j][i] = true;
            }
        }
        return clauses.size();
    }

    /** Brute-force search for a clique of the given size (fine for small instances). */
    public List<Node> findClique(int targetSize) {
        int n = nodes.size();
        int[] chosen = new int[targetSize];
        List<Node> result = new ArrayList<>();
        if (search(0, 0, chosen, targetSize, n)) {
            for (int idx : chosen) result.add(nodes.get(idx));
        }
        return result;
    }

    private boolean search(int depth, int start, int[] chosen, int targetSize, int n) {
        if (depth == targetSize) return true;
        for (int i = start; i < n; i++) {
            boolean connectsToAll = true;
            for (int d = 0; d < depth; d++) {
                if (!adjacency[chosen[d]][i]) { connectsToAll = false; break; }
            }
            if (connectsToAll) {
                chosen[depth] = i;
                if (search(depth + 1, i + 1, chosen, targetSize, n)) return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // 3-SAT: (x1 OR x2 OR x3) AND (NOT x1 OR x2 OR NOT x3)
        List<int[]> clauses = new ArrayList<>();
        clauses.add(new int[]{1, 2, 3});
        clauses.add(new int[]{-1, 2, -3});

        ThreeSATToClique reduction = new ThreeSATToClique(clauses);
        int k = reduction.buildGraph();
        List<Node> clique = reduction.findClique(k);
        System.out.println("Compatible topic-tag clique of size " + k + ": " + clique);
    }
}
