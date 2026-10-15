package mediamine.flow;

import java.util.*;

/**
 * Ford-Fulkerson method (DFS-based augmenting paths).
 * MediaMine use case: model the maximum media-distribution capacity from a
 * source (studio/publisher) to a sink (end viewers) through intermediate
 * distribution nodes (CDNs, regional servers).
 */
public class FordFulkerson {

    private final int n;
    private final int[][] capacity;

    public FordFulkerson(int n) {
        this.n = n;
        this.capacity = new int[n][n];
    }

    public void addEdge(int u, int v, int cap) {
        capacity[u][v] += cap;
    }

    private boolean dfs(int u, int sink, boolean[] visited, int[] parent) {
        if (u == sink) return true;
        visited[u] = true;
        for (int v = 0; v < n; v++) {
            if (!visited[v] && capacity[u][v] > 0) {
                parent[v] = u;
                if (dfs(v, sink, visited, parent)) return true;
            }
        }
        return false;
    }

    public int maxFlow(int source, int sink) {
        int totalFlow = 0;
        int[] parent = new int[n];

        while (true) {
            Arrays.fill(parent, -1);
            boolean[] visited = new boolean[n];
            if (!dfs(source, sink, visited, parent)) break;

            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v]);
            }
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                capacity[u][v] -= pathFlow;
                capacity[v][u] += pathFlow;
            }
            totalFlow += pathFlow;
        }
        return totalFlow;
    }

    public static void main(String[] args) {
        // 0 = source (studio), 5 = sink (viewers), 1-4 = CDN nodes
        FordFulkerson ff = new FordFulkerson(6);
        ff.addEdge(0, 1, 16);
        ff.addEdge(0, 2, 13);
        ff.addEdge(1, 2, 10);
        ff.addEdge(2, 1, 4);
        ff.addEdge(1, 3, 12);
        ff.addEdge(3, 2, 9);
        ff.addEdge(2, 4, 14);
        ff.addEdge(4, 3, 7);
        ff.addEdge(3, 5, 20);
        ff.addEdge(4, 5, 4);

        System.out.println("Max media-distribution capacity (Ford-Fulkerson): " + ff.maxFlow(0, 5));
    }
}
