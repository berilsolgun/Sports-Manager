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

- [X] Match simulation that works: `FootballMatchEngine` (`IMatchEngine`, phases, scores, events)
- [ ] `AbstractMatchEngine` filled in or used — class is **empty**; engine does **not** extend it
- [ ] `StandingsCalculator` doing the table sort — class is **empty**; points + sort live in `FootballLeague.getStandings()`
- [ ] Application controllers: `WeekController`, `MatchController`, `LeagueController` *(not in the repo)*
- [ ] Testing brief to the letter: lots of good coverage in `FootballTest`, but no focused suite on a real `StandingsCalculator` + deep tie-break cases as separate unit tests

**Pool 3 in one line:** Playable simulation exists; the named abstractions and controllers from the brief still need work.

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
- [ ] Reliable `mvn clean compile` on a typical machine *(JavaFX + `module-info` often needs extra plugin / module path setup — compile failed here with “module not found: javafx.controls”)*
- [ ] `mvn exec:java` meaningful once `Main` exists

**Pool 4 in one line:** POM skeleton yes; persistence, UI, and green builds are the main gap.

---

## Pool 5 — QA, delivery, and documentation

**Goal:** Marks, report, clean handoff, release.

- [X] Test count: on the order of **37** `@Test` methods (`FootballTest` + `FrameworkTest`) — likely enough **if** the suite builds and runs
- [ ] ~4 **integration** tests (e.g. simulate a full week end-to-end) — hard without the week / match controllers
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
- [ ] **Pool 3** — `AbstractMatchEngine` / `StandingsCalculator` / controllers / strict test split  
- [ ] **Pool 4** — JSON repo, runnable UI, Gson/Jackson, stable Maven  
- [ ] **Pool 5** — PDF, release, fresh-clone checks, week-style integration tests  

---

## Suggested order of attack

1. [ ] Pool 4 — JSON + `Main` + JavaFX so the project runs and compiles reliably  
2. [ ] Pool 3 — move standings into `StandingsCalculator`, wire controllers, flesh out `AbstractMatchEngine`  
3. [ ] Pool 1 / 5 — `GameSession` tests + a few full-week integration tests  
4. [ ] Pool 5 — M2 PDF, GitHub Release, repo URL file  

---

*Last reviewed: **2026-04-11***