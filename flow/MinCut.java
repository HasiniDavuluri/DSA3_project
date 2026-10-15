package mediamine.flow;

import java.util.*;

/**
 * Min-Cut via Max-Flow (Max-Flow Min-Cut Theorem).
 * MediaMine use case: identify the critical (bottleneck) distribution
 * connections whose failure would most reduce content reach -- i.e. the
 * minimum set of edges whose removal disconnects source from sink.
 */
public class MinCut {

    private final int n;
    private final int[][] originalCapacity;
    private int[][] residual;

    public MinCut(int n) {
        this.n = n;
        this.originalCapacity = new int[n][n];
    }

    public void addEdge(int u, int v, int cap) {
        originalCapacity[u][v] += cap;
    }

    private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    visited[v] = true;
                    parent[v] = u;
                    queue.add(v);
                }
            }
        }
        return visited[sink];
    }

    public static class CutResult {
        public final int minCutValue;
        public final List<int[]> criticalEdges; // [u, v] pairs
        CutResult(int value, List<int[]> edges) {
            this.minCutValue = value;
            this.criticalEdges = edges;
        }
    }

    public CutResult computeMinCut(int source, int sink) {
        residual = new int[n][n];
        for (int i = 0; i < n; i++) residual[i] = originalCapacity[i].clone();

        int[] parent = new int[n];
        int maxFlow = 0;
        while (bfs(source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                pathFlow = Math.min(pathFlow, residual[parent[v]][v]);
            }
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
            }
            maxFlow += pathFlow;
        }

        // Find reachable set from source in final residual graph
        boolean[] visited = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(source);
        visited[source] = true;
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    visited[v] = true;
                    stack.push(v);
                }
            }
        }

        List<int[]> critical = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            if (visited[u]) {
                for (int v = 0; v < n; v++) {
                    if (!visited[v] && originalCapacity[u][v] > 0) {
                        critical.add(new int[]{u, v});
                    }
                }
            }
        }
        return new CutResult(maxFlow, critical);
    }

    public static void main(String[] args) {
        MinCut mc = new MinCut(6);
        mc.addEdge(0, 1, 16);
        mc.addEdge(0, 2, 13);
        mc.addEdge(1, 2, 10);
        mc.addEdge(2, 1, 4);
        mc.addEdge(1, 3, 12);
        mc.addEdge(3, 2, 9);
        mc.addEdge(2, 4, 14);
        mc.addEdge(4, 3, 7);
        mc.addEdge(3, 5, 20);
        mc.addEdge(4, 5, 4);

        CutResult result = mc.computeMinCut(0, 5);
        System.out.println("Min-cut capacity: " + result.minCutValue);
        System.out.println("Critical distribution connections:");
        for (int[] edge : result.criticalEdges) {
            System.out.println("  Node " + edge[0] + " -> Node " + edge[1]);
        }
    }
}
