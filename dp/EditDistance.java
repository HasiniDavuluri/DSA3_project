package mediamine.dp;

/**
 * Levenshtein and Damerau-Levenshtein edit distance.
 * MediaMine use case: correcting misspelled media-search queries
 * ("did you mean...?" suggestions).
 */
public class EditDistance {

    /** Standard Levenshtein distance: insert, delete, substitute. */
    public static int levenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[n][m];
    }

    /** Damerau-Levenshtein: adds adjacent transposition as a fourth operation. */
    public static int damerauLevenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                           Math.min(dp[i][j - 1] + 1,
                                    dp[i - 1][j - 1] + cost));
                if (i > 1 && j > 1
                        && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost);
                }
            }
        }
        return dp[n][m];
    }

    /** Finds the closest query in a dictionary of known/valid search terms. */
    public static String correctQuery(String query, String[] dictionary) {
        String best = query;
        int bestDist = Integer.MAX_VALUE;
        for (String candidate : dictionary) {
            int dist = damerauLevenshtein(query, candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        String[] knownTerms = {"election", "economy", "climate", "sports", "technology"};
        String typo = "electoin";
        System.out.println("Levenshtein('kitten','sitting') = " + levenshtein("kitten", "sitting"));
        System.out.println("Query \"" + typo + "\" corrected to: " + correctQuery(typo, knownTerms));
    }
}
