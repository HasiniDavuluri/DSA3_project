package mediamine.strings;

import java.util.*;

/**
 * Aho-Corasick automaton.
 * MediaMine use case: detect many topics/keywords in a single pass over a
 * news article or transcript (e.g. tagging content with multiple categories
 * at once).
 */
public class AhoCorasick {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        List<String> output = new ArrayList<>();
    }

    private final Node root = new Node();

    /** Adds a keyword to the trie/automaton. */
    public void addKeyword(String keyword) {
        Node cur = root;
        for (char c : keyword.toCharArray()) {
            cur = cur.children.computeIfAbsent(c, k -> new Node());
        }
        cur.output.add(keyword);
    }

    /** Builds failure links via BFS (must be called after adding all keywords). */
    public void build() {
        Deque<Node> queue = new ArrayDeque<>();
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (Map.Entry<Character, Node> entry : cur.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();
                Node f = cur.fail;
                while (f != null && !f.children.containsKey(c)) f = f.fail;
                child.fail = (f == null) ? root : f.children.get(c);
                child.output.addAll(child.fail.output);
                queue.add(child);
            }
        }
    }

    /** Returns a map: matched keyword -> list of end indices in the text. */
    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> results = new LinkedHashMap<>();
        Node cur = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (cur != root && !cur.children.containsKey(c)) cur = cur.fail;
            cur = cur.children.getOrDefault(c, root);
            for (String kw : cur.output) {
                results.computeIfAbsent(kw, k -> new ArrayList<>()).add(i - kw.length() + 1);
            }
        }
        return results;
    }

    public static void main(String[] args) {
        AhoCorasick ac = new AhoCorasick();
        for (String topic : new String[]{"election", "climate", "economy", "clima"}) {
            ac.addKeyword(topic);
        }
        ac.build();
        String article = "the election results affected the economy while climate policy dominated debate";
        System.out.println("Aho-Corasick topic detection: " + ac.search(article));
    }
}
