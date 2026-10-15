package mediamine.flow;

import java.util.*;

/**
 * Maximum Bipartite Matching (Kuhn's algorithm, augmenting-path based).
 * MediaMine use case: assign media content items to eligible distribution
 * channels, where each content item can only go to channels that meet its
 * licensing/format requirements, maximizing the number of content items
 * successfully placed.
 */
public class BipartiteMatching {

    private final int numContent, numChannels;
    private final List<List<Integer>> eligibleChannels; // adjacency list: content -> channels
    private int[] channelAssignment; // channel -> content matched to it (-1 if none)

    public BipartiteMatching(int numContent, int numChannels) {
        this.numContent = numContent;
        this.numChannels = numChannels;
        this.eligibleChannels = new ArrayList<>();
        for (int i = 0; i < numContent; i++) eligibleChannels.add(new ArrayList<>());
    }

    public void addEligibility(int content, int channel) {
        eligibleChannels.get(content).add(channel);
    }

    private boolean tryAssign(int content, boolean[] visited) {
        for (int channel : eligibleChannels.get(content)) {
            if (!visited[channel]) {
                visited[channel] = true;
                if (channelAssignment[channel] == -1 || tryAssign(channelAssignment[channel], visited)) {
                    channelAssignment[channel] = content;
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the maximum matching size and fills channelAssignment[]. */
    public int maxMatching() {
        channelAssignment = new int[numChannels];
        Arrays.fill(channelAssignment, -1);
        int matches = 0;
        for (int content = 0; content < numContent; content++) {
            boolean[] visited = new boolean[numChannels];
            if (tryAssign(content, visited)) matches++;
        }
        return matches;
    }

    public int[] getChannelAssignment() {
        return channelAssignment;
    }

    public static void main(String[] args) {
        String[] contentNames = {"Doc-A", "Doc-B", "Show-C"};
        String[] channelNames = {"YouTube", "Netflix", "Hulu"};

        BipartiteMatching bm = new BipartiteMatching(3, 3);
        bm.addEligibility(0, 0); // Doc-A -> YouTube
        bm.addEligibility(0, 1); // Doc-A -> Netflix
        bm.addEligibility(1, 1); // Doc-B -> Netflix
        bm.addEligibility(2, 1); // Show-C -> Netflix
        bm.addEligibility(2, 2); // Show-C -> Hulu

        int matches = bm.maxMatching();
        System.out.println("Max content-to-channel assignments: " + matches);
        int[] assign = bm.getChannelAssignment();
        for (int c = 0; c < assign.length; c++) {
            if (assign[c] != -1) {
                System.out.println(contentNames[assign[c]] + " -> " + channelNames[c]);
            }
        }
    }
}
