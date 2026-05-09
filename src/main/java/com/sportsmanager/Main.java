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
 */
public class Main {

    private static final int TEAM_COUNT = 4;
    private static final int WEEKS_TO_SIMULATE = 3;

    public static void main(String[] args) {

        System.out.println("=== Sports Manager ===\n");

        SportRegistry registry = buildRegistry();
        List<Sport> sports = registry.getAll();
        if (sports.isEmpty()) {
            System.err.println("No sports registered.");
            return;
        }

        int sportIndex = 0;
        if (args.length > 0) {
            try {
                sportIndex = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                String name = args[0];
                for (int i = 0; i < sports.size(); i++) {
                    if (sports.get(i).getName().equalsIgnoreCase(name)) {
                        sportIndex = i;
                        break;
                    }
                }
            }
        }
        sportIndex = Math.floorMod(sportIndex, sports.size());

        Sport selectedSport = sports.get(sportIndex);
        System.out.println("Sport: " + selectedSport.getName() + " (index " + sportIndex + ")\n");

        SportFactory factory = selectedSport.createFactory();
        IMatchEngine engine = factory.createMatchEngine();

        List<ITeam> teams = createTeams(factory, TEAM_COUNT);

        ILeague league = factory.createLeague("Super Lig", teams);

        GameSession session = new GameSession();
        session.setSport(selectedSport);
        session.setCurrentWeek(1);
        session.setSeason(2026);
        session.setLeague(league);
        session.setPlayerTeam(teams.get(0));

        System.out.println("Player team: " + session.getPlayerTeam().getName());

        JsonGameRepository repo = new JsonGameRepository();
        repo.save(session);
        System.out.println("Session saved to: " + repo.getFilePath());

        WeekController weekController = new WeekController();
        MatchController matchController = new MatchController();
        LeagueController leagueController = new LeagueController();

        System.out.println("\n--- Season Simulation ---");
        for (int i = 0; i < WEEKS_TO_SIMULATE; i++) {
            matchController.playCurrentWeek(session, engine);

            List<StandingEntry> standings = leagueController.getStandings(session);
            System.out.println("\nWeek " + session.getCurrentWeek() + " Standings:");
            for (StandingEntry entry : standings) {
                System.out.printf("  %-20s %2d pts  (SD %+d)%n",
                        entry.getTeam().getName(),
                        entry.getPoints(),
                        entry.getGoalsFor() - entry.getGoalsAgainst());
            }
            matchController.applyGameWeekInjuryRecovery(session);
            weekController.advanceWeek(session);
        }

        repo.save(session);
        repo.load().ifPresent(s ->
                System.out.println("\nSession loaded. Week: " + s.getCurrentWeek()
                        + ", Season: " + s.getSeason())
        );
    }

    private static SportRegistry buildRegistry() {
        SportRegistry registry = new SportRegistry();
        registry.register(new FootballSport());
        registry.register(new VolleyballSport());
        return registry;
    }

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
