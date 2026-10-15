package mediamine.suffix;

import java.util.HashMap;
import java.util.Map;

/**
 * Suffix Automaton (built online in O(n)).
 * MediaMine use case: substring-based content retrieval -- quickly answer
 * "does this content contain substring X?" and count distinct substrings,
 * without building a full suffix tree.
 */
public class SuffixAutomaton {

    private static class State {
        int len;
        int link;
        Map<Character, Integer> transitions = new HashMap<>();
    }

    private State[] states;
    private int size, last;

    public SuffixAutomaton(String s) {
        states = new State[2 * s.length() + 5];
        for (int i = 0; i < states.length; i++) states[i] = new State();
        states[0].len = 0;
        states[0].link = -1;
        size = 1;
        last = 0;
        for (char c : s.toCharArray()) extend(c);
    }

    private void extend(char c) {
        int cur = size++;
        states[cur].len = states[last].len + 1;
        int p = last;
        while (p != -1 && !states[p].transitions.containsKey(c)) {
            states[p].transitions.put(c, cur);
            p = states[p].link;
        }
        if (p == -1) {
            states[cur].link = 0;
        } else {
            int q = states[p].transitions.get(c);
            if (states[p].len + 1 == states[q].len) {
                states[cur].link = q;
            } else {
                int clone = size++;
                states[clone].len = states[p].len + 1;
                states[clone].link = states[q].link;
                states[clone].transitions = new HashMap<>(states[q].transitions);
                while (p != -1 && states[p].transitions.get(c) != null && states[p].transitions.get(c) == q) {
                    states[p].transitions.put(c, clone);
                    p = states[p].link;
                }
                states[q].link = clone;
                states[cur].link = clone;
            }
        }
        last = cur;
    }

    /** Checks whether the automaton's source string contains {@code pattern} as a substring. */
    public boolean contains(String pattern) {
        int cur = 0;
        for (char c : pattern.toCharArray()) {
            Integer next = states[cur].transitions.get(c);
            if (next == null) return false;
            cur = next;
        }
        return true;
    }

    /** Counts the number of distinct non-empty substrings of the source string. */
    public long countDistinctSubstrings() {
        long total = 0;
        for (int i = 1; i < size; i++) {
            total += states[i].len - states[states[i].link].len;
        }
        return total;
    }

    public static void main(String[] args) {
        SuffixAutomaton sam = new SuffixAutomaton("mediamine content");
        System.out.println("Contains 'media': " + sam.contains("media"));
        System.out.println("Contains 'xyz': " + sam.contains("xyz"));
        System.out.println("Distinct substrings: " + sam.countDistinctSubstrings());
    }
}
