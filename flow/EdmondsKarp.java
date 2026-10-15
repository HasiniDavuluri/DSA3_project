package mediamine.flow;

import java.util.*;

/**
 * Edmonds-Karp algorithm (BFS-based Ford-Fulkerson, O(V*E^2)).
 * MediaMine use case: content-delivery networks where guaranteed polynomial
 * running time matters (BFS guarantees shortest augmenting paths first).
 */
public class EdmondsKarp {

    private final int n;
    private final int[][] capacity;

    public EdmondsKarp(int n) {
        this.n = n;
        this.capacity = new int[n][n];
    }

    public void addEdge(int u, int v, int cap) {
        capacity[u][v] += cap;
    }

    private int bfs(int source, int sink, int[] parent) {
        Arrays.fill(parent, -1);
        parent[source] = source;
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{source, Integer.MAX_VALUE});

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int u = cur[0], flow = cur[1];
            for (int v = 0; v < n; v++) {
                if (parent[v] == -1 && capacity[u][v] > 0) {
                    parent[v] = u;
                    int newFlow = Math.min(flow, capacity[u][v]);
                    if (v == sink) return newFlow;
                    queue.add(new int[]{v, newFlow});
                }
            }
        }
        return 0;
    }

    public int maxFlow(int source, int sink) {
        int total = 0;
        int[] parent = new int[n];
        int newFlow;
        while ((newFlow = bfs(source, sink, parent)) > 0) {
            total += newFlow;
            int cur = sink;
            while (cur != source) {
                int prev = parent[cur];
                capacity[prev][cur] -= newFlow;
                capacity[cur][prev] += newFlow;
                cur = prev;
            }
        }
        return total;
    }

    public static void main(String[] args) {
        EdmondsKarp ek = new EdmondsKarp(6);
        ek.addEdge(0, 1, 16);
        ek.addEdge(0, 2, 13);
        ek.addEdge(1, 2, 10);
        ek.addEdge(2, 1, 4);
        ek.addEdge(1, 3, 12);
        ek.addEdge(3, 2, 9);
        ek.addEdge(2, 4, 14);
        ek.addEdge(4, 3, 7);
        ek.addEdge(3, 5, 20);
        ek.addEdge(4, 5, 4);

        System.out.println("Max CDN throughput (Edmonds-Karp): " + ek.maxFlow(0, 5));
    }
}
