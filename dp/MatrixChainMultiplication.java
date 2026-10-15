package mediamine.dp;

/**
 * Matrix Chain Multiplication DP.
 * MediaMine use case: media transformations (resize, transcode, filter,
 * feature-extraction) can be modeled as matrix operations chained together;
 * this finds the optimal parenthesization/order to minimize total
 * multiplication (processing) cost.
 */
public class MatrixChainMultiplication {

    public static class Result {
        public final int minCost;
        public final String order;
        Result(int cost, String order) {
            this.minCost = cost;
            this.order = order;
        }
    }

    /** dims has length n+1: dims[i-1] x dims[i] is the shape of matrix i (1-indexed). */
    public static Result solve(int[] dims) {
        int n = dims.length - 1;
        int[][] dp = new int[n + 1][n + 1];
        int[][] split = new int[n + 1][n + 1];

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + dims[i - 1] * dims[k] * dims[j];
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                        split[i][j] = k;
                    }
                }
            }
        }
        String order = buildOrder(split, 1, n);
        return new Result(dp[1][n], order);
    }

    private static String buildOrder(int[][] split, int i, int j) {
        if (i == j) return "M" + i;
        int k = split[i][j];
        return "(" + buildOrder(split, i, k) + " x " + buildOrder(split, k + 1, j) + ")";
    }

    public static void main(String[] args) {
        // 4 processing stages with these matrix (operation) dimensions
        int[] dims = {40, 20, 30, 10, 30};
        Result result = solve(dims);
        System.out.println("Minimum processing cost: " + result.minCost);
        System.out.println("Optimal pipeline order: " + result.order);
    }
}
