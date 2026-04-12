# Sports Manager

Course-style **sports simulation** in Java: a modular domain (teams, leagues, fixtures, match engine), a **football** implementation, JSON persistence, and a small **console** entry point that runs a multi-week season loop.

## Requirements

- **JDK 21**
- **Maven 3.9+** (or use the included `mvnw` / `mvnw.cmd`)

## Quick start

```bash
./mvnw clean test
./mvnw exec:java
```

On Windows:

```text
mvnw.cmd clean test
mvnw.cmd exec:java
```

The default run registers **Football**, builds a league, saves `gamesession.json`, simulates a few weeks (standings printed to stdout), then reloads the session from disk.

## Project layout

| Path | Role |
|------|------|
| `com.sportsmanager.domain.*` | Core interfaces and abstractions (`Sport`, `ILeague`, `IMatchEngine`, `AbstractMatchEngine`, …) |
| `com.sportsmanager.football` | Football concrete types, `FootballMatchEngine`, `FootballLeague`, `FootballFactory` |
| `com.sportsmanager.application` | Game loop helpers: `WeekController`, `MatchController`, `LeagueController` |
| `com.sportsmanager.domain.session` | `GameSession`, `GameRepository`, **`JsonGameRepository`** (Gson) |
| `com.sportsmanager.Main` | Console demo wiring registry → session → save → simulate → load |

See **`roadmap.md`** for how this maps to milestone “Pools” (1–5) and what is still optional (e.g. full JavaFX shell beyond the placeholder FXML).

## Module system

The app is a single Java module **`com.sportsmanager`** (`module-info.java`). It **requires** JavaFX and Gson so `mvn clean compile` resolves those modules; the current **`Main`** path is console-only. A richer JavaFX UI can be layered on later (see `src/main/resources/com/sportsmanager/hello-view.fxml`).

## Persistence

- Default save file: **`gamesession.json`** in the working directory (override with `new JsonGameRepository("path.json")`).
- Tests use a temp file name where needed (`JsonGameRepositoryTest`).

## Useful Maven goals

| Goal | Purpose |
|------|---------|
| `mvn clean compile` | Compile main sources |
| `mvn test` | Run JUnit 5 tests (~55+ cases) |
| `mvn exec:java` | Run `com.sportsmanager.Main` |
| `mvn javafx:run` | JavaFX plugin (configured for future UI entry; same module main class) |

## License / course use

Built for an academic milestone (M2-style deliverable). Adapt license and attribution per your course policy.
