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
- [ ] Testing brief as written: `FrameworkTest` has 5 tests (injuries, player, squad, starting XI, league), but **no `GameSession` tests** yet

**Pool 1 in one line:** Almost there — add `GameSession` tests if you want to match the brief literally.

---

## Pool 2 — Football implementation specialist

**Goal:** Football on top of Pool 1.

- [X] `FootballSport` and `FootballFactory`
- [X] `FootballPlayer`, `FootballPosition`, `FootballTeam`
- [X] `FootballLeague` and `FootballCoach`
- [ ] Name files `names_male.txt` / `team_names_football.txt` in the repo *(optional — brief also allows hardcoded lists, which is what `FootballFactory` does today)*
- [X] Football rules tests (`FootballTest`, including starting XI / two goalkeepers, etc.)

**Pool 2 in one line:** Done for what the code needs; file-based names are optional.

---

## Pool 3 — Simulation and engine master

**Goal:** Matches, standings, and a clear game loop.

- [X] Match simulation: `FootballMatchEngine` extends **`AbstractMatchEngine`** (phase loop + `finishMatch` + event list)
- [X] **`StandingsCalculator`**: builds rows from played fixtures and sorts (points → GD → GF → name); **`FootballLeague`** delegates to it
- [X] Application controllers: **`WeekController`**, **`MatchController`**, **`LeagueController`** in `com.sportsmanager.application`
- [X] Targeted tests: **`StandingsCalculatorTest`** (compare + `compute`), **`FootballMatchEngineStructureTest`**, **`SimulationControllersTest`** (week play + 2-team season)

**Pool 3 in one line:** Done — engine abstraction, standings calculator, controllers, and supporting tests are in place.

---

## Pool 4 — Infrastructure and minimal UI

**Goal:** Maven, JSON save/load, runnable app.

- [X] Java 21 + JavaFX 21 + JUnit 5 in `pom.xml`
- [ ] Gson **or** Jackson dependency *(not added yet)*
- [X] `GameRepository` interface
- [ ] `JsonGameRepository` implementation
- [ ] `Main.java` entry that actually runs *(currently empty)*
- [ ] JavaFX wiring: `MainMenuController`, `DashboardController`, `MatchViewController` *(placeholder FXML points at a wrong `HelloController` package)*
- [ ] ~3 tests for JSON save/load of `GameSession`
- [ ] Reliable **`mvn clean compile` with JavaFX** on a typical machine *(JavaFX `requires` were removed from `module-info` so the project compiles without a JavaFX module path; add them back with the JavaFX Maven plugin when you ship UI)*
- [ ] `mvn exec:java` meaningful once `Main` exists

**Pool 4 in one line:** POM skeleton yes; persistence, UI, and green builds are the main gap.

---

## Pool 5 — QA, delivery, and documentation

**Goal:** Marks, report, clean handoff, release.

- [X] Test count: **~43** `@Test` methods across `FootballTest`, `FrameworkTest`, `StandingsCalculatorTest`, `FootballMatchEngineStructureTest`, `SimulationControllersTest`
- [X] Week / season style checks: **`SimulationControllersTest`** (play a week, advance week, 2-team double round-robin) — add more if the brief asks for a higher integration count
- [ ] M2 PDF: “what changed from M1 and why”
- [ ] Repo verified on a **fresh clone**: `mvn clean compile`, `mvn test`, `mvn exec:java` all green
- [ ] GitHub **Release** before the course deadline
- [ ] Small **`.txt`** file with the repository URL *(if the course asks for it)*

**Pool 5 in one line:** Tests exist in bulk; submission polish (PDF, release, green Maven, integration tests) is still open.

---

## At a glance

- [X] **Pool 1** — domain code  
- [ ] **Pool 1** — `GameSession` tests (if you treat the brief strictly)  
- [X] **Pool 2** — football + tests  
- [X] **Pool 3** — engine + standings + controllers + tests  
- [ ] **Pool 4** — JSON repo, runnable UI, Gson/Jackson, stable Maven  
- [ ] **Pool 5** — PDF, release, fresh-clone checks *(integration-style week tests exist; still verify full Maven + `exec:java` once Pool 4 lands)*  

---

## Suggested order of attack

1. [ ] Pool 4 — JSON + `Main` + JavaFX (restore `module-info` JavaFX `requires` + plugin / module path as needed)  
2. [X] Pool 3 — *(done)*  
3. [ ] Pool 1 / 5 — `GameSession` tests + any extra integration padding the rubric asks for  
4. [ ] Pool 5 — M2 PDF, GitHub Release, repo URL file  

---

*Last reviewed: **2026-04-11** (Pool 3 marked complete in roadmap.)*