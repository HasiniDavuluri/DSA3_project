# MediaMine – Intelligent Media Analytics Platform

MediaMine is a Java application that manages a small media library and uses it to demonstrate
the main algorithm topics of a Design and Analysis of Algorithms / Data Structures course:
string algorithms, suffix structures, dynamic programming, network flow, NP-completeness and
randomized / parallel algorithms.

It runs as a normal **console program** and also has a **web dashboard** that shows the same
program in a browser.

---

## Features (main menu)

| Option | What it does | Topic |
|--------|--------------|-------|
| 1 | Add Media Item – add a title, category and transcript to the library | Library |
| 2 | View All Media – list every item in the library | Library |
| 3 | Search Content – search media text for keywords or patterns (Aho-Corasick) | M1: String Algorithms |
| 4 | Compare Transcripts / Correct Search Query – edit distance and weighted edit distance | M3: Dynamic Programming |
| 5 | Index a Document & Retrieve Substrings | M2: Suffix Structures |
| 6 | Select Optimal Content Categories | M3: Bitmask DP |
| 7 | Optimize Media-Processing Pipeline Order | M3: Matrix Chain Multiplication |
| 8 | Distribution Network Analysis – max flow (Ford-Fulkerson, Edmonds-Karp, Dinic), min cut, bipartite matching | M4: Network Flow |
| 9 | Content-Selection Constraints & Conflicts – SAT, 3-SAT → Clique → Independent Set → Vertex Cover, vertex cover approximation | M5: NP-Completeness |
| 10 | Record a Viewing Event | Library |
| 11 | Real-Time Analytics Dashboard – reservoir sampling, randomized quicksort, Miller-Rabin, Brent's theorem, parallel reduce, Blelloch scan | M6: Randomized / Parallel |
| 0 | Exit | |

---

## Project structure

```
mediamine/                  Main.java (menu), WebConsole.java (web dashboard)
mediamine/model/            MediaItem, MediaLibrary
mediamine/strings/          String algorithms (e.g. AhoCorasick)
mediamine/dp/               BitmaskDP, EditDistance, WeightedEditDistance, MatrixChainMultiplication
mediamine/flow/             FordFulkerson, EdmondsKarp, Dinic, MinCut, BipartiteMatching
mediamine/npc/              SAT, ThreeSATToClique, CliqueToIndependentSetToVertexCover, VertexCoverApprox
mediamine/randomized/       MillerRabin, RandomizedQuickSort, ReservoirSampling, BrentTheorem,
                            ParallelReduce, BlellochScan
```

---

## Requirements

- JDK 17 or newer (developed with JDK 25)
- Eclipse (or any Java IDE). No external libraries are needed.

---

## How to run

### 1. Console version
Run `mediamine.Main` as a Java Application. Choose an option by typing its number.

### 2. Web dashboard version
1. Run `mediamine.WebConsole` as a Java Application (instead of `Main`).
2. Open the address printed in the console, normally **http://localhost:8080**.
   If port 8080 is busy, the next free port is used (8081, 8082, ...), and the address is printed.
3. Click a module in the sidebar. A short explanation appears first, followed by the output.
   Type any input the module asks for in the box below the output and press Enter.

> If you get a "package jdk.httpserver does not exist" error and your project has a
> `module-info.java`, add `requires jdk.httpserver;` to it.

---

## Saved data (web dashboard)

Every media item added with option 1 is saved to `mediamine-saved-media.txt` in the project folder.
The saved items are loaded again each time the program starts. The **Delete saved media** button
in the sidebar removes the file. If you change what option 1 asks for, delete the saved file first.

Note: switching to another option while one is still waiting for input restarts the program.
Saved media items are restored automatically.

---

## Author

Hasini Davuluri
