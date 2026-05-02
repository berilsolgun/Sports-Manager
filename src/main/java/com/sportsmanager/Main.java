package com.sportsmanager;

import com.sportsmanager.application.LeagueController;
import com.sportsmanager.application.MatchController;
import com.sportsmanager.application.WeekController;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.sport.SportRegistry;
import com.sportsmanager.domain.team.ITeam;

import com.sportsmanager.football.FootballSport;
import com.sportsmanager.volleyball.VolleyballSport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Console-based entry point for Sports Manager.
 * Demonstrates the full game loop using only framework interfaces.
 * The only football-specific reference is the one-time registration in
 * {@link #buildRegistry()}.
 */
public class Main {

    private static final int TEAM_COUNT = 4;
    private static final int WEEKS_TO_SIMULATE = 3;

    public static void main(String[] args) {

        System.out.println("=== Sports Manager ===\n");

        // ── 1. Register available sports ──────────────────────────────
        SportRegistry registry = buildRegistry();

        // For now, auto-select the first registered sport.
        // When the JavaFX UI is ready, SportSelectionView will let the user choose.
        // Sport selectedSport = registry.getAll().get(0);
        Sport selectedSport = registry.getAll().get(1); //Volleyball
        System.out.println("Sport: " + selectedSport.getName());

        // ── 2. Create game objects through the factory ─────────────────
        SportFactory factory = selectedSport.createFactory();
        IMatchEngine engine = factory.createMatchEngine();

        List<ITeam> teams = createTeams(factory, TEAM_COUNT);

        ILeague league = factory.createLeague("Super Lig", teams);

        // ── 3. Build the session ──────────────────────────────────────
        GameSession session = new GameSession();
        session.setSport(selectedSport);
        session.setCurrentWeek(1);
        session.setSeason(2026);
        session.setLeague(league);
        session.setPlayerTeam(teams.get(0));

        System.out.println("Player team: " + session.getPlayerTeam().getName());

        // ── 4. Persist ───────────────────────────────────────────────
        JsonGameRepository repo = new JsonGameRepository();
        repo.save(session);
        System.out.println("Session saved to: " + repo.getFilePath());

        // ── 5. Simulate a few weeks ──────────────────────────────────
        WeekController weekController = new WeekController();
        MatchController matchController = new MatchController();
        LeagueController leagueController = new LeagueController();

        System.out.println("\n--- Season Simulation ---");
        for (int i = 0; i < WEEKS_TO_SIMULATE; i++) {
            // Play the current gameweek first, then advance the calendar (week 1 was never played before).
            matchController.playCurrentWeek(session, engine);

            List<StandingEntry> standings = leagueController.getStandings(session);
            System.out.println("\nWeek " + session.getCurrentWeek() + " Standings:");
            for (StandingEntry entry : standings) {
                System.out.printf("  %-20s %2d pts  (SD %+d)%n",
                        entry.getTeam().getName(),
                        entry.getPoints(),
                        entry.getGoalsFor() - entry.getGoalsAgainst());
            }
            weekController.advanceWeek(session);
        }

        // ── 6. Reload and verify ─────────────────────────────────────
        repo.save(session);
        repo.load().ifPresent(s ->
                System.out.println("\nSession loaded. Week: " + s.getCurrentWeek()
                        + ", Season: " + s.getSeason())
        );
    }

    // ── helpers ───────────────────────────────────────────────────────

    /**
     * Builds and populates the global sport registry.
     * Add new sports here when they are implemented.
     */
    private static SportRegistry buildRegistry() {
        SportRegistry registry = new SportRegistry();
        registry.register(new FootballSport());
        registry.register(new VolleyballSport());
        // registry.register(new BasketballSport());   // future
        return registry;
    }

    /**
     * Creates {@code count} random teams via the given factory,
     * using the sport-specific team name pool.
     */
    private static List<ITeam> createTeams(SportFactory factory, int count) {
        String[] allNames = factory.getTeamNames();
        List<String> nameList = new ArrayList<>(Arrays.asList(allNames));
        Collections.shuffle(nameList);

        List<ITeam> teams = new ArrayList<>();
        for (int i = 0; i < count && i < nameList.size(); i++) {
            String name = nameList.get(i);
            ITeam team = factory.createTeam(name, name.toLowerCase().replace(" ", "_") + ".png");
            teams.add(team);
        }
        return teams;
    }
}