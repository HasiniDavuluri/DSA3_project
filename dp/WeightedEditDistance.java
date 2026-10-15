package mediamine.dp;

/**
 * Weighted edit distance where insert/delete/substitute costs can differ
 * per operation (and substitution cost can depend on which characters are
 * involved, e.g. commonly confused phonemes in speech-to-text transcripts).
 * MediaMine use case: transcript-error correction where some corrections
 * (e.g. "b" <-> "p") are cheaper/more likely than others.
 */
public class WeightedEditDistance {

    public interface SubstitutionCost {
        double cost(char a, char b);
    }

    public static double compute(String a, String b,
                                  double insertCost, double deleteCost,
                                  SubstitutionCost subCost) {
        int n = a.length(), m = b.length();
        double[][] dp = new double[n + 1][m + 1];
        for (int i = 1; i <= n; i++) dp[i][0] = dp[i - 1][0] + deleteCost;
        for (int j = 1; j <= m; j++) dp[0][j] = dp[0][j - 1] + insertCost;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double sub = dp[i - 1][j - 1] + subCost.cost(a.charAt(i - 1), b.charAt(j - 1));
                double del = dp[i - 1][j] + deleteCost;
                double ins = dp[i][j - 1] + insertCost;
                dp[i][j] = Math.min(sub, Math.min(del, ins));
            }
        }
        return dp[n][m];
    }

    /** Example cost function: phonetically similar letters are cheap to swap. */
    private static double phoneticCost(char a, char b) {
        if (a == b) return 0.0;
        String[] confusable = {"bp", "vf", "sz", "td", "mn"};
        for (String pair : confusable) {
            if (pair.indexOf(a) >= 0 && pair.indexOf(b) >= 0) return 0.3;
        }
        return 1.0;
    }

    public static void main(String[] args) {
        String transcript = "the bata was recorded";
        String correct = "the data was recorded";
        double dist = compute(transcript, correct, 1.0, 1.0, WeightedEditDistance::phoneticCost);
        System.out.println("Weighted edit distance (phonetic-aware): " + dist);
    }
}
