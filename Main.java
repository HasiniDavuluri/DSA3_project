package mediamine;

import mediamine.dp.*;
import mediamine.flow.*;
import mediamine.model.MediaItem;
import mediamine.model.MediaLibrary;
import mediamine.npc.*;
import mediamine.randomized.*;
import mediamine.strings.*;
import mediamine.suffix.*;

import java.util.*;

/**
 * MediaMine - Intelligent Media Analytics Platform.
 *
 * An interactive, console-only, menu-driven application. Every menu action
 * is backed by a real DSA module (M1-M6 from the project spec) operating on
 * an actual in-memory media library, rather than a one-shot printed demo.
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);
    private static final MediaLibrary LIBRARY = new MediaLibrary();

    public static void main(String[] args) {
        seedLibrary();
        System.out.println("======================================================");
        System.out.println(" Welcome to MediaMine - Intelligent Media Analytics Platform");
        System.out.println("======================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addMediaItem();
                case 2 -> viewAllMedia();
                case 3 -> searchContentMenu();
                case 4 -> compareOrCorrectMenu();
                case 5 -> indexAndRetrieveMenu();
                case 6 -> selectCategoriesMenu();
                case 7 -> pipelineOrderMenu();
                case 8 -> distributionNetworkMenu();
                case 9 -> constraintsAndConflictsMenu();
                case 10 -> recordViewingEvent();
                case 11 -> analyticsDashboardMenu();
                case 0 -> { running = false; System.out.println("Shutting down MediaMine. Goodbye."); }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ============================================================
    // Seed data
    // ============================================================

    private static void seedLibrary() {
        MediaItem a1 = LIBRARY.addMedia("Election Results Roundup", "Article",
                "the election results affected the economy while climate policy dominated debate", "Politics");
        a1.addCaption("Live coverage of results night");
        a1.putMetadata("author", "News Desk");

        MediaItem a2 = LIBRARY.addMedia("Climate Summit Report", "Article",
                "our media engagement report indicates a decline in climate coverage this quarter", "Environment");
        a2.putMetadata("author", "Environment Desk");

        MediaItem v1 = LIBRARY.addMedia("Quarterly Engagement Review", "Video",
                "the quarterly media engagement report shows growth across all channels", "Business");
        v1.addCaption("Chart: engagement up 12% QoQ");

        MediaItem v2 = LIBRARY.addMedia("Championship Highlights", "Video",
                "the championship game ended with a dramatic final play in the last minute", "Sports");
        v2.addCaption("Final score 3-2");

        MediaItem t1 = LIBRARY.addMedia("Tech Keynote Transcript", "Transcript",
                "the bata was recorded during the keynote about media analytics platforms", "Technology");
        t1.putMetadata("speaker", "Keynote Speaker");
    }

    // ============================================================
    // Menu printing / input helpers
    // ============================================================

    private static void printMainMenu() {
        System.out.println();
        System.out.println("---------------- MAIN MENU ----------------");
        System.out.println(" 1. Add Media Item");
        System.out.println(" 2. View All Media");
        System.out.println(" 3. Search Content (M1: String Algorithms)");
        System.out.println(" 4. Compare Transcripts / Correct Search Query (M3: DP)");
        System.out.println(" 5. Index a Document & Retrieve Substrings (M2: Suffix Structures)");
        System.out.println(" 6. Select Optimal Content Categories (M3: Bitmask DP)");
        System.out.println(" 7. Optimize Media-Processing Pipeline Order (M3: Matrix Chain)");
        System.out.println(" 8. Distribution Network Analysis (M4: Network Flow)");
        System.out.println(" 9. Content-Selection Constraints & Conflicts (M5: NP-Completeness)");
        System.out.println("10. Record a Viewing Event");
        System.out.println("11. Real-Time Analytics Dashboard (M6: Randomized/Parallel)");
        System.out.println(" 0. Exit");
        System.out.println("--------------------------------------------");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    private static MediaItem promptForItem(String label) {
        String id = readLine(label + " (e.g. M1): ");
        Optional<MediaItem> item = LIBRARY.findById(id);
        if (item.isEmpty()) {
            System.out.println("No media item found with id " + id);
            return null;
        }
        return item.get();
    }

    // ============================================================
    // 1. Add Media Item
    // ============================================================

    private static void addMediaItem() {
        System.out.println("\n-- Add Media Item --");
        String title = readLine("Title: ");
        String type = readLine("Type (Article/Video/Transcript): ");
        String content = readLine("Content / transcript text: ");
        String category = readLine("Category: ");
        MediaItem item = LIBRARY.addMedia(title, type, content, category);

        String addCaption = readLine("Add a caption? (y/n): ");
        if (addCaption.equalsIgnoreCase("y")) {
            item.addCaption(readLine("Caption text: "));
        }
        System.out.println("Added media item with id " + item.getId());
    }

    // ============================================================
    // 2. View All Media
    // ============================================================

    private static void viewAllMedia() {
        System.out.println("\n-- Media Library (" + LIBRARY.size() + " items) --");
        if (LIBRARY.getAllItems().isEmpty()) {
            System.out.println("No media items yet.");
            return;
        }
        for (MediaItem item : LIBRARY.getAllItems()) {
            System.out.println(item.toDetailedString());
        }
    }

    // ============================================================
    // 3. Search Content
    // ============================================================

    private static void searchContentMenu() {
        System.out.println("\n-- Search Content --");
        System.out.println("1. Keyword search across all items (KMP)");
        System.out.println("2. Search by ID/identifier substring (Rabin-Karp)");
        System.out.println("3. Multi-topic detection across all items (Aho-Corasick)");
        System.out.println("4. Repeated-phrase detection within one item (Z-function)");
        System.out.println("0. Back");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1 -> {
                String keyword = readLine("Keyword: ");
                LIBRARY.recordSearch(keyword);
                boolean any = false;
                for (MediaItem item : LIBRARY.getAllItems()) {
                    List<Integer> matches = KMP.search(item.getContent().toLowerCase(), keyword.toLowerCase());
                    if (!matches.isEmpty()) {
                        any = true;
                        System.out.println(item.getId() + " \"" + item.getTitle() + "\" -> matches at " + matches);
                    }
                }
                if (!any) System.out.println("No matches found for \"" + keyword + "\".");
            }
            case 2 -> {
                StringBuilder corpus = new StringBuilder();
                for (MediaItem item : LIBRARY.getAllItems()) {
                    corpus.append(item.getId()).append(":").append(item.getTitle()).append("\n");
                }
                String idFragment = readLine("ID/identifier fragment: ");
                List<Integer> positions = RabinKarp.search(corpus.toString(), idFragment);
                System.out.println(positions.isEmpty()
                        ? "No identifier matches."
                        : "Found identifier fragment at corpus positions: " + positions);
            }
            case 3 -> {
                String topicsLine = readLine("Topics (comma-separated): ");
                String[] topics = topicsLine.split(",");
                AhoCorasick ac = new AhoCorasick();
                for (String t : topics) {
                    String trimmed = t.trim().toLowerCase();
                    if (!trimmed.isEmpty()) ac.addKeyword(trimmed);
                }
                ac.build();
                for (MediaItem item : LIBRARY.getAllItems()) {
                    Map<String, List<Integer>> hits = ac.search(item.getContent().toLowerCase());
                    if (!hits.isEmpty()) {
                        System.out.println(item.getId() + " \"" + item.getTitle() + "\" -> topics found: " + hits.keySet());
                    }
                }
            }
            case 4 -> {
                MediaItem item = promptForItem("Item id to scan");
                if (item == null) return;
                int len = readInt("Phrase length to look for: ");
                List<String> repeats = ZFunction.findRepeatedPhrases(item.getContent(), len);
                System.out.println(repeats.isEmpty()
                        ? "No repeated phrases of length " + len + " found."
                        : "Repeated phrases: " + repeats);
            }
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    // ============================================================
    // 4. Compare Transcripts / Correct Query
    // ============================================================

    private static void compareOrCorrectMenu() {
        System.out.println("\n-- Compare / Correct --");
        System.out.println("1. Compare two items (edit distance + common phrase)");
        System.out.println("2. Correct a misspelled search query");
        System.out.println("0. Back");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1 -> {
                MediaItem a = promptForItem("First item id");
                if (a == null) return;
                MediaItem b = promptForItem("Second item id");
                if (b == null) return;
                int lev = EditDistance.levenshtein(a.getContent(), b.getContent());
                int dam = EditDistance.damerauLevenshtein(a.getContent(), b.getContent());
                String common = LCPKasai.longestCommonPhrase(a.getContent(), b.getContent());
                System.out.println("Levenshtein distance: " + lev);
                System.out.println("Damerau-Levenshtein distance: " + dam);
                System.out.println("Longest common phrase: \"" + common + "\"");
            }
            case 2 -> {
                String query = readLine("Search query: ");
                String[] vocab = LIBRARY.buildVocabulary();
                if (vocab.length == 0) {
                    System.out.println("Vocabulary is empty -- add some media items first.");
                    return;
                }
                String corrected = EditDistance.correctQuery(query, vocab);
                LIBRARY.recordSearch(query);
                System.out.println("Did you mean: \"" + corrected + "\"?");
            }
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    // ============================================================
    // 5. Index & Retrieve (Suffix Automaton / Suffix Array)
    // ============================================================

    private static void indexAndRetrieveMenu() {
        System.out.println("\n-- Document Indexing & Substring Retrieval --");
        MediaItem item = promptForItem("Item id to index");
        if (item == null) return;

        SuffixAutomaton automaton = new SuffixAutomaton(item.getContent());
        System.out.println("Index built. Distinct substrings: " + automaton.countDistinctSubstrings());

        boolean querying = true;
        while (querying) {
            String pattern = readLine("Enter a substring to check (blank to stop): ");
            if (pattern.isEmpty()) {
                querying = false;
            } else {
                System.out.println(automaton.contains(pattern)
                        ? "YES - \"" + pattern + "\" occurs in this document."
                        : "NO - \"" + pattern + "\" does not occur in this document.");
            }
        }
    }

 // ============================================================
 // 6. Select Optimal Content Categories (Bitmask DP)
 // ============================================================

 private static void selectCategoriesMenu() {

     System.out.println("\n-- Select Optimal Content Categories --");

     Set<String> distinctCategories = new LinkedHashSet<>();

     for (MediaItem item : LIBRARY.getAllItems()) {
         distinctCategories.add(item.getCategory());
     }

     if (distinctCategories.isEmpty()) {
         System.out.println("No categories available yet.");
         return;
     }

     List<String> categories = new ArrayList<>(distinctCategories);

     int n = categories.size();

     int[] cost = new int[n];
     int[] relevance = new int[n];

     System.out.println(
             "Enter a production cost for each category "
             + "(relevance is auto-computed from item count):"
     );

     for (int i = 0; i < n; i++) {

         // Store category in a separate variable
         // so it can safely be used inside the lambda
         String category = categories.get(i);

         long itemCount = LIBRARY.getAllItems()
                 .stream()
                 .filter(m -> m.getCategory().equals(category))
                 .count();

         relevance[i] = (int) itemCount * 5 + 1;

         cost[i] = readInt(
                 "  Cost for \"" + category
                 + "\" (relevance=" + relevance[i] + "): "
         );
     }

     int budget = readInt("Total production budget: ");

     BitmaskDP.Result result =
             BitmaskDP.bestCategorySubset(cost, relevance, budget);

     System.out.print("Selected categories: ");

     for (int idx : result.selectedCategories) {
         System.out.print(categories.get(idx) + " ");
     }

     System.out.println(
             "\nTotal relevance score: " + result.totalRelevance
     );
 } 

    // ============================================================
    // 7. Optimize Pipeline Order (Matrix Chain Multiplication)
    // ============================================================

    private static void pipelineOrderMenu() {
        System.out.println("\n-- Optimize Media-Processing Pipeline Order --");
        int stages = readInt("Number of processing stages: ");
        int[] dims = new int[stages + 1];
        System.out.println("Enter " + (stages + 1) + " dimension values (shape boundaries between stages):");
        for (int i = 0; i < dims.length; i++) {
            dims[i] = readInt("  dims[" + i + "]: ");
        }
        MatrixChainMultiplication.Result result = MatrixChainMultiplication.solve(dims);
        System.out.println("Minimum processing cost: " + result.minCost);
        System.out.println("Optimal pipeline order: " + result.order);
    }

    // ============================================================
    // 8. Distribution Network Analysis
    // ============================================================

    private static void distributionNetworkMenu() {
        System.out.println("\n-- Distribution Network Analysis --");
        System.out.println("1. Max distribution capacity (choose algorithm)");
        System.out.println("2. Critical distribution connections (Min-Cut)");
        System.out.println("3. Assign media items to distribution channels (Bipartite Matching)");
        System.out.println("0. Back");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1 -> {
                System.out.println("Using a sample 6-node network: 0=studio (source), 5=viewers (sink), 1-4=CDN nodes");
                System.out.println("a) Ford-Fulkerson  b) Edmonds-Karp  c) Dinic");
                String alg = readLine("Choose algorithm (a/b/c): ").toLowerCase();
                int flow;
                switch (alg) {
                    case "a" -> {
                        FordFulkerson ff = buildSampleFordFulkerson();
                        flow = ff.maxFlow(0, 5);
                    }
                    case "c" -> {
                        Dinic d = buildSampleDinic();
                        flow = d.maxFlow(0, 5);
                    }
                    default -> {
                        EdmondsKarp ek = buildSampleEdmondsKarp();
                        flow = ek.maxFlow(0, 5);
                    }
                }
                System.out.println("Maximum distribution throughput: " + flow);
            }
            case 2 -> {
                MinCut mc = new MinCut(6);
                addSampleEdges(mc);
                MinCut.CutResult result = mc.computeMinCut(0, 5);
                System.out.println("Min-cut capacity: " + result.minCutValue);
                System.out.println("Critical distribution connections:");
                for (int[] edge : result.criticalEdges) {
                    System.out.println("  Node " + edge[0] + " -> Node " + edge[1]);
                }
            }
            case 3 -> {
                String[] channels = {"YouTube", "Netflix", "Hulu", "PublicTV"};
                List<MediaItem> allItems = LIBRARY.getAllItems();
                if (allItems.isEmpty()) {
                    System.out.println("No media items to assign.");
                    return;
                }
                BipartiteMatching bm = new BipartiteMatching(allItems.size(), channels.length);
                for (int i = 0; i < allItems.size(); i++) {
                    MediaItem item = allItems.get(i);
                    bm.addEligibility(i, 0); // every item eligible for YouTube
                    if (item.getType().equalsIgnoreCase("Video")) {
                        bm.addEligibility(i, 1); // Netflix
                        bm.addEligibility(i, 2); // Hulu
                    }
                    if (item.getCategory().equalsIgnoreCase("Politics") || item.getCategory().equalsIgnoreCase("Environment")) {
                        bm.addEligibility(i, 3); // PublicTV
                    }
                }
                int matches = bm.maxMatching();
                System.out.println("Max content-to-channel assignments: " + matches);
                int[] assignment = bm.getChannelAssignment();
                for (int c = 0; c < assignment.length; c++) {
                    if (assignment[c] != -1) {
                        System.out.println(allItems.get(assignment[c]).getTitle() + " -> " + channels[c]);
                    }
                }
            }
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private static void addSampleEdges(MinCut mc) {
        mc.addEdge(0, 1, 16); mc.addEdge(0, 2, 13); mc.addEdge(1, 2, 10);
        mc.addEdge(2, 1, 4);  mc.addEdge(1, 3, 12); mc.addEdge(3, 2, 9);
        mc.addEdge(2, 4, 14); mc.addEdge(4, 3, 7);  mc.addEdge(3, 5, 20); mc.addEdge(4, 5, 4);
    }

    private static FordFulkerson buildSampleFordFulkerson() {
        FordFulkerson ff = new FordFulkerson(6);
        ff.addEdge(0, 1, 16); ff.addEdge(0, 2, 13); ff.addEdge(1, 2, 10);
        ff.addEdge(2, 1, 4);  ff.addEdge(1, 3, 12); ff.addEdge(3, 2, 9);
        ff.addEdge(2, 4, 14); ff.addEdge(4, 3, 7);  ff.addEdge(3, 5, 20); ff.addEdge(4, 5, 4);
        return ff;
    }

    private static EdmondsKarp buildSampleEdmondsKarp() {
        EdmondsKarp ek = new EdmondsKarp(6);
        ek.addEdge(0, 1, 16); ek.addEdge(0, 2, 13); ek.addEdge(1, 2, 10);
        ek.addEdge(2, 1, 4);  ek.addEdge(1, 3, 12); ek.addEdge(3, 2, 9);
        ek.addEdge(2, 4, 14); ek.addEdge(4, 3, 7);  ek.addEdge(3, 5, 20); ek.addEdge(4, 5, 4);
        return ek;
    }

    private static Dinic buildSampleDinic() {
        Dinic d = new Dinic(6);
        d.addEdge(0, 1, 16); d.addEdge(0, 2, 13); d.addEdge(1, 2, 10);
        d.addEdge(2, 1, 4);  d.addEdge(1, 3, 12); d.addEdge(3, 2, 9);
        d.addEdge(2, 4, 14); d.addEdge(4, 3, 7);  d.addEdge(3, 5, 20); d.addEdge(4, 5, 4);
        return d;
    }

    // ============================================================
    // 9. Content-Selection Constraints & Conflicts
    // ============================================================

    private static void constraintsAndConflictsMenu() {
        System.out.println("\n-- Content-Selection Constraints & Conflicts --");
        System.out.println("1. Solve featuring constraints (SAT): no two same-category items together,");
        System.out.println("   at least one item per category must be featured");
        System.out.println("2. Resolve category conflicts (minimum items to drop -- Vertex Cover)");
        System.out.println("0. Back");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1 -> solveFeaturingConstraints();
            case 2 -> resolveConflicts();
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }

    private static void solveFeaturingConstraints() {
        List<MediaItem> items = capItems(8, "SAT solving");
        if (items.isEmpty()) return;

        int n = items.size();
        SAT sat = new SAT(n);
        // No two items sharing a category may both be featured
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (items.get(i).getCategory().equals(items.get(j).getCategory())) {
                    sat.addClause(-(i + 1), -(j + 1));
                }
            }
        }
        // At least one item per category must be featured
        Set<String> categoriesSeen = new LinkedHashSet<>();
        for (MediaItem item : items) categoriesSeen.add(item.getCategory());
        for (String category : categoriesSeen) {
            List<Integer> clause = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (items.get(i).getCategory().equals(category)) clause.add(i + 1);
            }
            sat.addClause(clause.stream().mapToInt(Integer::intValue).toArray());
        }

        int[] assignment = sat.solve();
        if (assignment == null) {
            System.out.println("No valid featuring selection satisfies all constraints (UNSAT).");
            return;
        }
        System.out.println("Feasible featuring plan:");
        for (int i = 0; i < n; i++) {
            String status = assignment[i + 1] == 1 ? "FEATURED" : "not featured";
            System.out.println("  " + items.get(i).getId() + " \"" + items.get(i).getTitle() + "\" -> " + status);
        }
    }

    private static void resolveConflicts() {
        List<MediaItem> items = capItems(12, "conflict resolution");
        if (items.isEmpty()) return;
        int n = items.size();

        List<int[]> conflictEdges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (items.get(i).getCategory().equals(items.get(j).getCategory())) {
                    conflictEdges.add(new int[]{i, j});
                }
            }
        }
        if (conflictEdges.isEmpty()) {
            System.out.println("No category conflicts exist among the current items.");
            return;
        }

        if (n <= 10) {
            CliqueToIndependentSetToVertexCover exact = new CliqueToIndependentSetToVertexCover(n);
            for (int[] e : conflictEdges) exact.addConflict(e[0], e[1]);
            List<Integer> cover = exact.minVertexCover();
            System.out.println("Exact minimum items to drop to resolve all conflicts:");
            for (int idx : cover) System.out.println("  " + items.get(idx).getId() + " \"" + items.get(idx).getTitle() + "\"");
        } else {
            List<Integer> cover = VertexCoverApprox.approximate(n, conflictEdges);
            System.out.println("Approximate (<=2x optimal) items to drop to resolve conflicts:");
            for (int idx : cover) System.out.println("  " + items.get(idx).getId() + " \"" + items.get(idx).getTitle() + "\"");
        }
    }

    private static List<MediaItem> capItems(int max, String purpose) {
        List<MediaItem> items = LIBRARY.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No media items available.");
            return Collections.emptyList();
        }
        if (items.size() > max) {
            System.out.println("Using the first " + max + " items for " + purpose + " (kept small for tractability).");
            return new ArrayList<>(items.subList(0, max));
        }
        return new ArrayList<>(items);
    }

    // ============================================================
    // 10. Record Viewing Event
    // ============================================================

    private static void recordViewingEvent() {
        System.out.println("\n-- Record a Viewing Event --");
        MediaItem item = promptForItem("Item id being viewed");
        if (item == null) return;
        int hour = readInt("Hour bucket (0-7): ");
        LIBRARY.recordView(item.getId(), hour);
        System.out.println("Recorded a view for " + item.getId() + " in hour bucket " + Math.floorMod(hour, MediaLibrary.VIEW_BUCKETS)
                + ". Total views now: " + item.getViewCount());
    }

    // ============================================================
    // 11. Analytics Dashboard
    // ============================================================

    private static void analyticsDashboardMenu() {
        System.out.println("\n-- Real-Time Analytics Dashboard --");
        System.out.println("1. Rank content by engagement (Randomized QuickSort)");
        System.out.println("2. Sample of recent viewing events (Reservoir Sampling)");
        System.out.println("3. Cumulative hourly views (Blelloch parallel scan)");
        System.out.println("4. Total watch-time estimate at scale (Parallel Reduce)");
        System.out.println("5. Parallel runtime estimate (Brent's Theorem)");
        System.out.println("0. Back");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1 -> {
                List<MediaItem> items = LIBRARY.getAllItems();
                if (items.isEmpty()) { System.out.println("No media items yet."); return; }
                int[] scores = new int[items.size()];
                for (int i = 0; i < items.size(); i++) scores[i] = (int) items.get(i).getViewCount();
                int[] sortedScores = scores.clone();
                RandomizedQuickSort.sort(sortedScores);
                System.out.println("Raw engagement scores, sorted ascending: " + Arrays.toString(sortedScores));

                List<MediaItem> ranked = new ArrayList<>(items);
                ranked.sort((a, b) -> Long.compare(b.getViewCount(), a.getViewCount()));
                System.out.println("Ranked content (highest engagement first):");
                for (MediaItem item : ranked) {
                    System.out.println("  " + item.getId() + " \"" + item.getTitle() + "\" -- " + item.getViewCount() + " views");
                }
            }
            case 2 -> {
                List<String> sample = LIBRARY.getViewEventSampler().getSample();
                System.out.println(sample.isEmpty()
                        ? "No viewing events recorded yet."
                        : "Random sample of recent viewing events: " + sample);
            }
            case 3 -> {
                long[] cumulative = LIBRARY.getHourlyViewBuckets();
                long[] raw = cumulative.clone();
                BlellochScan.exclusiveScan(cumulative);
                System.out.println("Views per hour bucket:      " + Arrays.toString(raw));
                System.out.println("Cumulative views (exclusive scan): " + Arrays.toString(cumulative));
            }
            case 4 -> {
                long totalViews = 0;
                for (MediaItem item : LIBRARY.getAllItems()) totalViews += item.getViewCount();
                int syntheticSize = (int) Math.max(100_000, totalViews * 1000);
                long[] watchTimes = new long[syntheticSize];
                for (int i = 0; i < syntheticSize; i++) watchTimes[i] = (i % 300) + 1;
                long start = System.nanoTime();
                long total = ParallelReduce.parallelSum(watchTimes);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                System.out.println("Synthetic watch-time records processed: " + syntheticSize);
                System.out.println("Total watch time: " + total + " seconds (computed in " + elapsedMs + " ms)");
            }
            case 5 -> {
                long totalViews = 0;
                for (MediaItem item : LIBRARY.getAllItems()) totalViews += item.getViewCount();
                long work = Math.max(100_000, totalViews * 1000);
                long span = (long) (Math.log(work) / Math.log(2));
                for (int p : new int[]{1, 2, 4, 8, 16}) {
                    System.out.println(BrentTheorem.estimateRuntime(work, span, p));
                }
                System.out.println("Suggested processor count where returns diminish: "
                        + BrentTheorem.suggestOptimalProcessors(work, span));
            }
            case 0 -> { }
            default -> System.out.println("Invalid option.");
        }
    }
}
