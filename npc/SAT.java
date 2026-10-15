package mediamine.npc;

import java.util.*;

/**
 * SAT / 3-SAT solver (DPLL-style with unit propagation).
 * MediaMine use case: content-selection constraints -- e.g. "if we feature
 * article A, we must NOT feature article B" or "at least one of C, D must be
 * shown" -- expressed as boolean clauses over which content items to select.
 *
 * A clause is a list of ints; positive i means variable i must be TRUE,
 * negative i means variable (-i) must be FALSE. Variables are 1-indexed.
 */
public class SAT {

    private final int numVars;
    private final List<int[]> clauses;

    public SAT(int numVars) {
        this.numVars = numVars;
        this.clauses = new ArrayList<>();
    }

    public void addClause(int... literals) {
        clauses.add(literals);
    }

    /** Returns a satisfying assignment (1-indexed, index 0 unused) or null if UNSAT. */
    public int[] solve() {
        int[] assignment = new int[numVars + 1]; // 0 = unassigned, 1 = true, -1 = false
        if (dpll(assignment)) return assignment;
        return null;
    }

    private boolean dpll(int[] assignment) {
        // Unit propagation
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int[] clause : clauses) {
                int unassignedLit = 0, unassignedCount = 0;
                boolean satisfied = false;
                for (int lit : clause) {
                    int var = Math.abs(lit);
                    int val = assignment[var];
                    if (val == 0) { unassignedCount++; unassignedLit = lit; }
                    else if ((lit > 0 && val == 1) || (lit < 0 && val == -1)) { satisfied = true; }
                }
                if (satisfied) continue;
                if (unassignedCount == 0) return false; // clause falsified, dead end
                if (unassignedCount == 1) {
                    assignment[Math.abs(unassignedLit)] = unassignedLit > 0 ? 1 : -1;
                    changed = true;
                }
            }
        }

        int nextVar = -1;
        for (int v = 1; v <= numVars; v++) {
            if (assignment[v] == 0) { nextVar = v; break; }
        }
        if (nextVar == -1) return isFullySatisfied(assignment);

        for (int guess : new int[]{1, -1}) {
            int[] copy = assignment.clone();
            copy[nextVar] = guess;
            if (dpll(copy)) {
                System.arraycopy(copy, 0, assignment, 0, copy.length);
                return true;
            }
        }
        return false;
    }

    private boolean isFullySatisfied(int[] assignment) {
        for (int[] clause : clauses) {
            boolean satisfied = false;
            for (int lit : clause) {
                int var = Math.abs(lit);
                if ((lit > 0 && assignment[var] == 1) || (lit < 0 && assignment[var] == -1)) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // Variables: 1=FeatureArticleA, 2=FeatureArticleB, 3=FeatureArticleC
        // Constraint: (A OR B) AND (NOT A OR NOT B) AND (B OR C)
        SAT sat = new SAT(3);
        sat.addClause(1, 2);
        sat.addClause(-1, -2);
        sat.addClause(2, 3);

        int[] result = sat.solve();
        if (result == null) {
            System.out.println("No valid content-selection satisfies constraints (UNSAT).");
        } else {
            System.out.println("Satisfying content selection: A=" + result[1] + " B=" + result[2] + " C=" + result[3]);
        }
    }
}
