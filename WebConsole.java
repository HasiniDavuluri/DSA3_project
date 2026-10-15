package mediamine;

import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs the existing console program (Main) and mirrors it in a web page.
 * Run THIS class instead of Main, then open http://localhost:8080
 * No changes to Main or any other class are needed.
 */
public class WebConsole {

    private static final int PORT = 8080;
    private static final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private static final LinkedBlockingQueue<Integer> input = new LinkedBlockingQueue<>();
    private static final java.util.Set<Thread> retired = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile Thread app;


    // ---------- saving media items ----------
    // Every completed "Add Media Item" (option 1) is saved to a file next to the project.
    // On every start (and restart) the saved entries are replayed silently into the program,
    // so the items are back in memory without changing any of your classes.
    private static final java.nio.file.Path SAVE_FILE = java.nio.file.Path.of("mediamine-saved-media.txt");
    private static final String PROMPT = "Choose an option:";
    private static final String MENU = "---------------- MAIN MENU";
    private static final Object lock = new Object();
    private static PrintStream real;
    private static final StringBuilder tail = new StringBuilder();
    private static java.util.List<String> rec = null;
    private static boolean recSeenOutput = false;
    private static boolean quiet = false;
    private static int quietGen = 0;
    private static int expectedMenus = 0;
    private static int restoredCount = 0;
    private static final StringBuilder quietBuf = new StringBuilder();

    private static boolean atMenuTail() {
        return tail.toString().stripTrailing().endsWith(PROMPT);
    }

    private static int countPrompts(CharSequence text) {
        String t = text.toString();
        int n = 0, i = 0;
        while ((i = t.indexOf(PROMPT, i)) >= 0) { n++; i += PROMPT.length(); }
        return n;
    }

    private static void setTail(String text) {
        tail.setLength(0);
        tail.append(text.length() > 200 ? text.substring(text.length() - 200) : text);
    }

    /** All output of the program goes through here. */
    private static void onOutput(byte[] b, int off, int len) {
        if (real != null) { real.write(b, off, len); real.flush(); }
        String chunk = new String(b, off, len, StandardCharsets.UTF_8);
        synchronized (lock) {
            if (quiet) {
                quietBuf.append(chunk);
                if (countPrompts(quietBuf) >= expectedMenus) endQuiet(false);
                return;
            }
            buffer.write(b, off, len);
            tail.append(chunk);
            if (tail.length() > 200) tail.delete(0, tail.length() - 200);
            if (rec != null) {
                recSeenOutput = true;
                if (atMenuTail()) {           // the option finished and the menu is back
                    saveSession(rec);
                    rec = null;
                }
            }
        }
    }

    /** Ends the silent replay and shows a normal-looking start screen. Call with lock held. */
    private static void endQuiet(boolean timedOut) {
        quiet = false;
        String all = quietBuf.toString();
        String shown;
        if (timedOut) {
            shown = all;
        } else {
            int first = all.indexOf(MENU);
            int last = all.lastIndexOf(MENU);
            shown = (first > 0 ? all.substring(0, first) : "")
                  + "[Restored " + restoredCount + " saved media entr" + (restoredCount == 1 ? "y" : "ies") + "]\n\n"
                  + (last >= 0 ? all.substring(last) : all);
        }
        byte[] bytes = shown.getBytes(StandardCharsets.UTF_8);
        buffer.write(bytes, 0, bytes.length);
        setTail(shown);
        quietBuf.setLength(0);
    }

    private static void saveSession(java.util.List<String> lines) {
        try {
            String text = String.join("\n", lines) + "\n---\n";
            java.nio.file.Files.writeString(SAVE_FILE, text, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            if (real != null) real.println("Could not save media item: " + e.getMessage());
        }
    }

    private static java.util.List<java.util.List<String>> loadSessions() {
        java.util.List<java.util.List<String>> out = new java.util.ArrayList<>();
        try {
            if (!java.nio.file.Files.exists(SAVE_FILE)) return out;
            java.util.List<String> cur = new java.util.ArrayList<>();
            for (String line : java.nio.file.Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8)) {
                if (line.equals("---")) { if (!cur.isEmpty()) out.add(cur); cur = new java.util.ArrayList<>(); }
                else cur.add(line);
            }
        } catch (IOException e) {
            if (real != null) real.println("Could not read saved media: " + e.getMessage());
        }
        return out;
    }

    /** Sets up the silent replay of saved entries; call before the program thread starts. */
    private static void prepareReplay() {
        java.util.List<java.util.List<String>> sessions = loadSessions();
        synchronized (lock) {
            rec = null;
            quietBuf.setLength(0);
            restoredCount = sessions.size();
            if (sessions.isEmpty()) { quiet = false; return; }
            quiet = true;
            expectedMenus = sessions.size() + 1;
            final int gen = ++quietGen;
            for (java.util.List<String> session : sessions) {
                for (String line : session) {
                    for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) input.add(b & 0xFF);
                }
            }
            Thread watchdog = new Thread(() -> {
                try { Thread.sleep(10000); } catch (InterruptedException ignored) { }
                synchronized (lock) { if (quiet && quietGen == gen) endQuiet(true); }
            });
            watchdog.setDaemon(true);
            watchdog.start();
        }
    }

    private static void restartApp() {
        Thread old = app;
        if (old != null) { retired.add(old); old.interrupt(); }
        input.clear();
        startApp();
    }

    /** Starts (or restarts) the console program on its own thread. */
    private static void startApp() {
        prepareReplay();
        Thread t = new Thread(() -> {
            try {
                // Load a fresh copy of the program every time, so each (re)start gets
                // its own Scanner and its own data instead of sharing static fields.
                java.net.URL loc = WebConsole.class.getProtectionDomain().getCodeSource().getLocation();
                try (java.net.URLClassLoader loader =
                         new java.net.URLClassLoader(new java.net.URL[] { loc }, ClassLoader.getPlatformClassLoader())) {
                    Class<?> c = Class.forName("mediamine.Main", true, loader);
                    c.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                }
            } catch (Throwable e) {
                Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null)
                        ? e.getCause() : e;
                if (app == Thread.currentThread()) cause.printStackTrace();
            }
            if (app == Thread.currentThread()) System.out.println("\n[Program ended]");
        }, "mediamine-main");
        t.setDaemon(true);
        app = t;
        t.start();
    }

    private static final String PAGE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>MediaMine Dashboard</title>
        <style>
          :root { --bg:#f3f5f9; --card:#ffffff; --fg:#1b2233; --muted:#6a7488; --line:#e3e7ef;
                  --accent:#3b6ef5; --accent2:#7c4dff; --ok:#1fa971; --term:#10141c; --termfg:#d6e0f2; --side:#131a2b; --sidefg:#c4cde0; }
          @media (prefers-color-scheme: dark) {
            :root { --bg:#0c1018; --card:#161c29; --fg:#e8ecf5; --muted:#8c96ab; --line:#252d3d;
                    --accent:#6c93ff; --accent2:#9a7bff; --ok:#3ccf91; --term:#090c12; --side:#0a0e16; }
          }
          * { box-sizing: border-box; }
          body { margin:0; font-family: system-ui, "Segoe UI", sans-serif; background:var(--bg); color:var(--fg); display:flex; min-height:100vh; }
          aside { width:270px; background:var(--side); color:var(--sidefg); padding:20px 14px; flex-shrink:0; overflow-y:auto; height:100vh; position:sticky; top:0; }
          .brand { font-size:20px; font-weight:700; color:#fff; margin:0 8px 2px; }
          .brand span { color:var(--accent); }
          .tag { font-size:12px; margin:0 8px 18px; opacity:.7; }
          .grp { font-size:11px; letter-spacing:.08em; text-transform:uppercase; opacity:.55; margin:16px 8px 6px; }
          .nav { display:flex; align-items:center; gap:10px; width:100%; text-align:left; background:transparent; border:0; color:inherit;
                 padding:9px 10px; border-radius:8px; font-size:14px; cursor:pointer; }
          .nav:hover { background:rgba(255,255,255,.07); }
          .nav.on { background:linear-gradient(90deg,var(--accent),var(--accent2)); color:#fff; }
          .nav .n { width:24px; height:24px; border-radius:6px; background:rgba(255,255,255,.12); display:grid; place-items:center; font-size:12px; flex-shrink:0; }
          .nav small { display:block; opacity:.65; font-size:11px; }
          main { flex:1; padding:24px; min-width:0; }
          header { display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:18px; }
          header h1 { margin:0; font-size:22px; }
          .pill { padding:5px 12px; border-radius:999px; font-size:13px; background:var(--card); border:1px solid var(--line); }
          .dot { display:inline-block; width:8px; height:8px; border-radius:50%; background:var(--ok); margin-right:6px; }
          .dot.off { background:#d9534f; }
          .stats { display:grid; grid-template-columns:repeat(auto-fit,minmax(170px,1fr)); gap:14px; margin-bottom:18px; }
          .card { background:var(--card); border:1px solid var(--line); border-radius:12px; padding:16px; }
          .stat .l { font-size:12px; color:var(--muted); text-transform:uppercase; letter-spacing:.05em; }
          .stat .v { font-size:24px; font-weight:700; margin-top:6px; }
          .grid { display:grid; grid-template-columns:1fr; gap:14px; }
          .mh { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; flex-wrap:wrap; }
          .mh h2 { margin:0 0 4px; font-size:18px; }
          .mh p { margin:0; color:var(--muted); font-size:14px; }
          .tabs { display:flex; gap:6px; margin:14px 0 8px; }
          .tabs button { border:1px solid var(--line); background:transparent; color:var(--muted); padding:5px 12px; border-radius:8px; cursor:pointer; font-size:13px; }
          .tabs button.on { background:var(--accent); border-color:var(--accent); color:#fff; }
          pre { margin:0; background:var(--term); color:var(--termfg); padding:16px; border-radius:10px; height:50vh; overflow:auto;
                font:14px/1.5 Consolas, "Courier New", monospace; white-space:pre-wrap; }
          form { display:flex; gap:8px; margin-top:12px; }
          input[type=text] { flex:1; padding:11px 12px; font-size:15px; border-radius:8px; border:1px solid var(--line); background:var(--bg); color:var(--fg); }
          .btn { padding:10px 18px; font-size:14px; border:0; border-radius:8px; background:var(--accent); color:#fff; cursor:pointer; }
          .btn.ghost { background:transparent; color:var(--accent); border:1px solid var(--accent); }
          @media (max-width:800px) { body { flex-direction:column; } aside { width:100%; height:auto; position:static; } }
        </style>
        </head>
        <body>
        <aside>
          <div class="brand">Media<span>Mine</span></div>
          <div class="tag">Intelligent Media Analytics Platform</div>
          <div id="nav"></div>
          <div class="grp">Saved data</div>
          <button class="nav" id="clearsaved"><span class="n">&#10005;</span><span>Delete saved media<small>Removes the saved file</small></span></button>
        </aside>
        <main>
          <header>
            <h1>Dashboard</h1>
            <span class="pill"><span class="dot" id="dot"></span><span id="state">Running</span></span>
          </header>
          <section class="stats">
            <div class="card stat"><div class="l">Modules</div><div class="v">11</div></div>
            <div class="card stat"><div class="l">Commands sent</div><div class="v" id="sent">0</div></div>
            <div class="card stat"><div class="l">Active module</div><div class="v" id="act">None</div></div>
            <div class="card stat"><div class="l">Output lines</div><div class="v" id="lines">0</div></div>
          </section>
          <section class="grid">
            <div class="card">
              <div class="mh">
                <div><h2 id="mt">Welcome</h2><p id="md">Pick a module from the sidebar to run it.</p></div>
                <button class="btn ghost" id="back">Clear selection</button>
              </div>
              <div class="tabs">
                <button id="t1" class="on">This run</button>
                <button id="t2">Full log</button>
              </div>
              <pre id="out"></pre>
              <form id="f">
                <input type="text" id="in" placeholder="Type the program's requested input, then press Enter" autocomplete="off" autofocus>
                <button class="btn" type="submit">Send</button>
              </form>
            </div>
          </section>
        </main>
        <script>
          const MODS = [
            [1,'Add Media Item','Library','Add a new item to the media library.',
              'Adds a new media item (title, category, transcript) to the library. All other modules work on this data.'],
            [2,'View All Media','Library','List every item stored in the library.',
              'Lists every media item currently stored in the library.'],
            [10,'Record a Viewing Event','Library','Log that a media item was watched.',
              'Logs that someone watched an item. These events feed the analytics in option 11.'],
            [3,'Search Content','M1: String Algorithms','Pattern matching and search over media content.',
              'Searches media text for keywords or patterns. Aho-Corasick can look for many keywords in one pass over the text.'],
            [5,'Index a Document','M2: Suffix Structures','Index a document and retrieve substrings.',
              'Builds a suffix structure (suffix array or tree) for a document. After that, any substring can be located very quickly.'],
            [4,'Compare Transcripts','M3: Dynamic Programming','Edit-distance comparison and search query correction.',
              'Uses edit distance (the fewest inserts, deletes and replacements to turn one text into another) to measure how similar two transcripts are and to suggest a corrected search query. The weighted version gives different costs to different edits.'],
            [6,'Optimal Content Categories','M3: Bitmask DP','Select the best set of content categories.',
              'Uses bitmask dynamic programming. Each subset of categories is a bit pattern, and the program finds the best combination of categories.'],
            [7,'Media-Processing Pipeline','M3: Matrix Chain','Find the cheapest order for processing steps.',
              'Uses matrix chain multiplication to find the cheapest order to run a chain of processing steps. The result is the same, but the cost depends heavily on the order.'],
            [8,'Distribution Network','M4: Network Flow','Max-flow, min-cut and matching analysis.',
              'Treats distribution as a network. Computes maximum flow (Ford-Fulkerson, Edmonds-Karp, Dinic), the minimum cut (the bottleneck), and bipartite matching (one-to-one pairing of items and slots).'],
            [9,'Selection Constraints','M5: NP-Completeness','Content-selection constraints and conflicts.',
              'Shows hard problems and how they relate. SAT checks whether constraints can all be satisfied. 3-SAT, Clique, Independent Set and Vertex Cover are converted into one another. Vertex Cover Approx gives a fast, approximately good answer.'],
            [11,'Analytics Dashboard','M6: Randomized / Parallel','Real-time analytics with randomized and parallel algorithms.',
              'Combines faster, scalable techniques: reservoir sampling (fair random sample of a stream), randomized quicksort, Miller-Rabin primality test, Brent theorem (parallel speedup bound), parallel reduce and Blelloch scan.']
          ];
          const out = document.getElementById('out'), inp = document.getElementById('in');
          let full = '', mark = 0, sent = 0, cur = null, view = 'run';

          const nav = document.getElementById('nav');
          let lastGroup = '';
          MODS.forEach(m => {
            const grp = m[2].startsWith('Library') ? 'Library' : 'Algorithm modules';
            if (grp !== lastGroup) { const g = document.createElement('div'); g.className = 'grp'; g.textContent = grp; nav.appendChild(g); lastGroup = grp; }
            const b = document.createElement('button');
            b.className = 'nav'; b.id = 'nav' + m[0];
            b.innerHTML = '<span class="n">' + m[0] + '</span><span>' + m[1] + '<small>' + m[2] + '</small></span>';
            b.onclick = () => run(m);
            nav.appendChild(b);
          });

          function runText() {
            const NL = String.fromCharCode(10);
            if (!cur) return (note ? note + NL + NL : '') + 'Select an option from the sidebar to run it.';
            let body = full.slice(mark);
            const MENU = '---------------- MAIN MENU';
            const i = body.indexOf(MENU);
            const done = i >= 0;
            if (done) body = body.slice(0, i);
            const echo = String(cur[0]) + NL;
            if (body.startsWith(echo)) body = body.slice(echo.length);
            const head = 'OPTION ' + cur[0] + ': ' + cur[1] + ' (' + cur[2] + ')' + NL +
                         'WHAT IT DOES: ' + cur[4] + NL +
                         '------------------------------------------------------' + NL + NL;
            return (note ? note + NL + NL : '') + head + (done ? body.trimEnd() + NL + NL + '[Finished. Pick another option from the sidebar.]' : body);
          }
          function atMenu() { return full.trimEnd().endsWith('Choose an option:'); }
          function render() {
            const atBottom = out.scrollTop + out.clientHeight >= out.scrollHeight - 40;
            out.textContent = view === 'run' ? runText() : full;
            if (atBottom) out.scrollTop = out.scrollHeight;
            const ended = full.trimEnd().endsWith('[Program ended]');
            document.getElementById('state').textContent = ended ? 'Program ended' : 'Running';
            document.getElementById('dot').className = ended ? 'dot off' : 'dot';
            document.getElementById('lines').textContent = full.split(String.fromCharCode(10)).length - 1;
            document.getElementById('sent').textContent = sent;
          }
          let tries = 0;
          async function poll() {
            try {
              const t = await (await fetch('/output')).text();
              if (t !== full) { full = t; render(); }
              if (full === '' && ++tries > 8) out.textContent = 'Connected, but the program has printed nothing yet. Check the Eclipse console for errors.';
            } catch (e) {
              out.textContent = 'Cannot reach the Java server. Is WebConsole still running in Eclipse?';
            }
          }
          window.onerror = (m) => { out.textContent = 'Page error: ' + m; };
          async function send(text) {
            sent++;
            await fetch('/input', { method: 'POST', body: text });
            poll();
          }
          let note = '';
          async function run(m) {
            note = '';
            if (!atMenu()) {
              await fetch('/restart', { method: 'POST' });
              for (let k = 0; k < 40 && !atMenu(); k++) {
                await new Promise(r => setTimeout(r, 100));
                await poll();
              }
              note = 'Note: the previous option was cancelled and the program restarted (items added earlier are cleared).';
            }
            cur = m; mark = full.length; view = 'run'; setTabs();
            document.querySelectorAll('.nav').forEach(x => x.classList.remove('on'));
            document.getElementById('nav' + m[0]).classList.add('on');
            document.getElementById('mt').textContent = m[1];
            document.getElementById('md').textContent = m[3];
            render();
            document.getElementById('act').textContent = 'Option ' + m[0];
            send(String(m[0]));
            inp.focus();
          }
          function setTabs() {
            document.getElementById('t1').className = view === 'run' ? 'on' : '';
            document.getElementById('t2').className = view === 'full' ? 'on' : '';
            render();
          }
          document.getElementById('t1').onclick = () => { view = 'run'; setTabs(); };
          document.getElementById('t2').onclick = () => { view = 'full'; setTabs(); };
          document.getElementById('back').onclick = () => {
            document.querySelectorAll('.nav').forEach(x => x.classList.remove('on'));
            cur = null; render(); document.getElementById('act').textContent = 'None';
            document.getElementById('mt').textContent = 'Welcome';
            document.getElementById('md').textContent = 'Pick a module from the sidebar to run it.';
          };
          document.getElementById('f').addEventListener('submit', e => {
            e.preventDefault(); send(inp.value); inp.value = ''; inp.focus();
          });
          document.getElementById('clearsaved').onclick = async () => {
            if (!confirm('Delete all saved media items and restart the program?')) return;
            await fetch('/clear-saved', { method: 'POST' });
            cur = null; mark = full.length; note = 'Saved media items deleted.';
            document.querySelectorAll('.nav').forEach(x => x.classList.remove('on'));
            document.getElementById('act').textContent = 'None';
            document.getElementById('mt').textContent = 'Welcome';
            document.getElementById('md').textContent = 'Pick a module from the sidebar to run it.';
            render();
          };
          setInterval(poll, 300); poll();
        </script>
        </body>
        </html>
        """;

    public static void main(String[] args) throws Exception {
        PrintStream realOut = System.out;
        real = realOut;

        // Capture everything the program prints
        OutputStream capture = new OutputStream() {
            @Override public void write(int b) {
                onOutput(new byte[] { (byte) b }, 0, 1);
            }
            @Override public void write(byte[] b, int off, int len) {
                onOutput(b, off, len);
            }
        };
        PrintStream ps = new PrintStream(capture, true, StandardCharsets.UTF_8);
        System.setOut(ps);
        System.setErr(ps);

        // Feed browser input to the program as if typed in the console
        System.setIn(new InputStream() {
            private void parkIfRetired() {
                if (retired.contains(Thread.currentThread())) {
                    while (true) java.util.concurrent.locks.LockSupport.park();
                }
            }
            @Override public int read() throws IOException {
                parkIfRetired();
                try { return input.take(); }
                catch (InterruptedException e) { parkIfRetired(); return -1; }
            }
            @Override public int read(byte[] b, int off, int len) throws IOException {
                if (len == 0) return 0;
                int first = read();
                if (first == -1) return -1;
                b[off] = (byte) first;
                int n = 1;
                Integer next;
                while (n < len && (next = input.poll()) != null) {
                    b[off + n++] = (byte) (int) next;
                }
                return n;
            }
        });

        // Web server
        HttpServer server = null;
        int port = PORT;
        for (; port < PORT + 20; port++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (java.net.BindException e) {
                realOut.println("Port " + port + " is busy, trying the next one...");
            }
        }
        if (server == null) {
            realOut.println("No free port found between " + PORT + " and " + (PORT + 19) + ".");
            return;
        }

        server.createContext("/", ex -> {
            byte[] body = PAGE.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(body); }
        });

        server.createContext("/output", ex -> {
            byte[] body;
            synchronized (lock) { body = buffer.toByteArray(); }
            ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            ex.getResponseHeaders().set("Cache-Control", "no-store");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(body); }
        });

        server.createContext("/input", ex -> {
            String line = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            // Echo like a real console, then feed the program
            synchronized (lock) {
                byte[] echo = (line + "\n").getBytes(StandardCharsets.UTF_8);
                buffer.write(echo, 0, echo.length);
                if (!quiet) {
                    if (rec != null) {
                        rec.add(line);
                    } else if (line.trim().equals("1") && atMenuTail()) {
                        rec = new java.util.ArrayList<>();
                        rec.add(line);
                        recSeenOutput = false;
                    }
                }
            }
            for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) {
                input.add(b & 0xFF);
            }
            ex.sendResponseHeaders(204, -1);
            ex.close();
        });

        server.createContext("/restart", ex -> {
            restartApp();
            ex.sendResponseHeaders(204, -1);
            ex.close();
        });

        server.createContext("/clear-saved", ex -> {
            java.nio.file.Files.deleteIfExists(SAVE_FILE);
            restartApp();
            ex.sendResponseHeaders(204, -1);
            ex.close();
        });

        server.start();

        // Run the existing console program
        startApp();

        realOut.println("MediaMine web console running at http://localhost:" + port);

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + port));
            }
        } catch (Exception ignored) { }
    }
}