# M2 roadmap (Pools 1–5)

How this repo lines up with the course pools.  
**`[X]`** = in good shape · **`[ ]`** = still to do (or only started)

---

## Pool 1 — Framework architect (core domain)

**Goal:** Domain backbone in Java.

- [X] Core interfaces: `Sport`, `SportFactory`, `IMatchEngine`
- [X] Team / player interfaces: `ITeam`, `IPlayer`, `ICoach`
- [X] League interfaces: `ILeague`, `IFixture`, `IMatchResult`
- [X] Abstract classes: `AbstractPlayer`, `AbstractTeam`, `AbstractLeague`
- [X] Value types: `StandingEntry`, `MatchEvent`, `Tactic`, `GameSession`, `PhaseResult`
- [ ] Testing brief as written: `FrameworkTest` covers injuries, player, squad, starting XI, league — add **`GameSession`-specific** tests if the rubric asks for them explicitly

**Pool 1 in one line:** Domain code complete; optional gap is extra `GameSession` unit tests.

---

## Pool 2 — Football implementation specialist

**Goal:** Football on top of Pool 1.

- [X] `FootballSport` and `FootballFactory`
- [X] `FootballPlayer`, `FootballPosition`, `FootballTeam`
- [X] `FootballLeague` and `FootballCoach`
- [ ] Name files `names_male.txt` / `team_names_football.txt` *(optional — factory uses hardcoded name arrays today)*
- [X] Football rules tests (`FootballTest`, etc.)

**Pool 2 in one line:** Done for implementation and tests; file-based names remain optional.

---

## Pool 3 — Simulation and engine master

**Goal:** Matches, standings, and a clear game loop.

- [X] `FootballMatchEngine` extends **`AbstractMatchEngine`** (phase loop + `finishMatch` + event list)
- [X] **`StandingsCalculator`** + delegation from **`FootballLeague`**
- [X] **`WeekController`**, **`MatchController`**, **`LeagueController`** (`com.sportsmanager.application`)
- [X] Tests: **`StandingsCalculatorTest`**, **`FootballMatchEngineStructureTest`**, **`SimulationControllersTest`**, **`SimulationIntegrationTest`**

**Pool 3 in one line:** Done.

---

## Pool 4 — Infrastructure and minimal UI

**Goal:** Maven, JSON save/load, runnable app.

- [X] Java **21**, **JavaFX 21**, **JUnit 5**, **Gson** in `pom.xml` (+ `javafx-maven-plugin`, `maven-compiler-plugin`)
- [X] **`JsonGameRepository`** (`GameRepository` implementation; default `gamesession.json`)
- [X] **`Main.java`** — runnable **console** demo (registry, session, save/load, week loop)
- [ ] Full **JavaFX product UI** (`MainMenuController`, `DashboardController`, `MatchViewController`) — only a placeholder **`hello-view.fxml`** exists today
- [X] JSON persistence tests — **`JsonGameRepositoryTest`**
- [X] **`mvn clean compile`** and **`mvn test`** on a normal JDK 21 install
- [X] **`mvn exec:java`** runs the console `Main`

**Pool 4 in one line:** Core infra + console runner + JSON are in place; a full JavaFX shell is still optional / future work.

---

## Pool 5 — QA, delivery, and documentation

**Goal:** Marks, report, clean handoff, release.

- [X] Test volume: **~57** `@Test` methods across the test tree (domain, football, session JSON, integration)
- [X] Integration-style flows: e.g. **`SimulationControllersTest`**, **`SimulationIntegrationTest`**
- [ ] M2 PDF: “what changed from M1 and why”
- [ ] Repo smoke on a **fresh clone** you personally verify (`mvn clean compile`, `mvn test`, `mvn exec:java`) before submission
- [ ] GitHub **Release** before the course deadline (if required)
- [ ] **`.txt`** with repository URL (if the course asks for it)

**Pool 5 in one line:** Code and tests are strong; remaining work is mostly **documentation + release hygiene**.

---

## At a glance

- [X] **Pool 1** — domain code  
- [ ] **Pool 1** — extra `GameSession`-only tests *(optional / rubric-dependent)*  
- [X] **Pool 2** — football + tests  
- [X] **Pool 3** — engine + standings + controllers + tests  
- [X] **Pool 4** — Maven + Gson + JSON repo + console `Main` + tests *(JavaFX “full UI” still open)*  
- [ ] **Pool 5** — PDF, release, submission checklist  

---

## Suggested order of attack

1. [ ] Pool 5 — M2 PDF, optional GitHub Release, repo URL file, final clone smoke test  
2. [ ] Pool 4 (optional) — replace placeholder FXML with real JavaFX screens if the brief requires GUI beyond console  
3. [ ] Pool 1 / 5 — add `GameSession` tests if the rubric is strict  

---

*Last reviewed: **2026-04-11** — aligned with repo (`README.md`, `Main`, `JsonGameRepository`, tests).*
