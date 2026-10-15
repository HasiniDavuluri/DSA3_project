package mediamine.dp;

import java.util.ArrayList;
import java.util.List;

/**
 * Bitmask Dynamic Programming.
 * MediaMine use case: selecting the optimal combination of content
 * categories to tag a piece of media with, subject to a "processing budget"
 * (a knapsack-style constraint over 2^k category subsets), maximizing
 * relevance score.
 */
public class BitmaskDP {

    /**
     * Given categories with (cost, relevanceScore), find the subset with
     * maximum total relevance whose total cost fits within budget.
     * Uses bitmask DP over all 2^n subsets -- practical for n up to ~20.
     */
    public static Result bestCategorySubset(int[] cost, int[] relevance, int budget) {
        int n = cost.length;
        int full = 1 << n;
        int bestMask = 0;
        int bestScore = -1;

        for (int mask = 0; mask < full; mask++) {
            int totalCost = 0, totalScore = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    totalCost += cost[i];
                    totalScore += relevance[i];
                }
            }
            if (totalCost <= budget && totalScore > bestScore) {
                bestScore = totalScore;
                bestMask = mask;
            }
        }

        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) chosen.add(i);
        }
        return new Result(chosen, bestScore);
    }

    public static class Result {
        public final List<Integer> selectedCategories;
        public final int totalRelevance;
        Result(List<Integer> selected, int score) {
            this.selectedCategories = selected;
            this.totalRelevance = score;
        }
    }

    public static void main(String[] args) {
        String[] categoryNames = {"Politics", "Sports", "Tech", "Health", "Finance"};
        int[] cost = {3, 2, 4, 2, 3};
        int[] relevance = {8, 5, 9, 6, 7};
        int budget = 7;

        Result r = bestCategorySubset(cost, relevance, budget);
        System.out.print("Selected categories: ");
        for (int idx : r.selectedCategories) System.out.print(categoryNames[idx] + " ");
        System.out.println("\nTotal relevance score: " + r.totalRelevance);
    }
}
