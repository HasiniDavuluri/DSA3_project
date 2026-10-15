package mediamine.flow;

import java.util.*;

/**
 * Dinic's algorithm (O(V^2 * E), much faster in practice via blocking flows
 * and level graphs).
 * MediaMine use case: larger-scale media-distribution networks where
 * Edmonds-Karp's plain BFS-per-augmenting-path becomes too slow.
 */
public class Dinic {

    public static class Edge {
        int to, cap, flow;
        Edge(int to, int cap) { this.to = to; this.cap = cap; this.flow = 0; }
    }

    private final int n;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Integer>> graph;
    private int[] level, it;

    public Dinic(int n) {
        this.n = n;
        graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int cap) {
        graph.get(u).add(edges.size());
        edges.add(new Edge(v, cap));
        graph.get(v).add(edges.size());
        edges.add(new Edge(u, 0)); // reverse edge
    }

    private boolean bfs(int s, int t) {
        level = new int[n];
        Arrays.fill(level, -1);
        level[s] = 0;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int id : graph.get(u)) {
                Edge e = edges.get(id);
                if (level[e.to] < 0 && e.cap - e.flow > 0) {
                    level[e.to] = level[u] + 1;
                    queue.add(e.to);
                }
            }
        }
        return level[t] >= 0;
    }

    private int dfs(int u, int t, int pushed) {
        if (u == t || pushed == 0) return pushed;
        for (; it[u] < graph.get(u).size(); it[u]++) {
            int id = graph.get(u).get(it[u]);
            Edge e = edges.get(id);
            if (level[e.to] != level[u] + 1 || e.cap - e.flow <= 0) continue;
            int d = dfs(e.to, t, Math.min(pushed, e.cap - e.flow));
            if (d > 0) {
                e.flow += d;
                edges.get(id ^ 1).flow -= d;
                return d;
            }
        }
        return 0;
    }

    public int maxFlow(int s, int t) {
        int flow = 0;
        while (bfs(s, t)) {
            it = new int[n];
            int pushed;
            while ((pushed = dfs(s, t, Integer.MAX_VALUE)) > 0) {
                flow += pushed;
            }
        }
        return flow;
    }

    public static void main(String[] args) {
        Dinic d = new Dinic(6);
        d.addEdge(0, 1, 16);
        d.addEdge(0, 2, 13);
        d.addEdge(1, 2, 10);
        d.addEdge(2, 1, 4);
        d.addEdge(1, 3, 12);
        d.addEdge(3, 2, 9);
        d.addEdge(2, 4, 14);
        d.addEdge(4, 3, 7);
        d.addEdge(3, 5, 20);
        d.addEdge(4, 5, 4);

        System.out.println("Max flow for large distribution network (Dinic): " + d.maxFlow(0, 5));
    }
}
