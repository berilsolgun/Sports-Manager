package com.sportsmanager.football;

import com.sportsmanager.data.ResourceLines;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.team.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FootballFactory implements SportFactory {

    private static final String[] FALLBACK_TEAMS = {
            "Galatasaray", "Fenerbahce", "Besiktas", "Trabzonspor",
            "Basaksehir", "Antalyaspor", "Konyaspor", "Sivasspor",
            "Alanyaspor", "Kasimpasa", "Rizespor", "Hatayspor",
            "Kayserispor", "Samsunspor", "Adana Demirspor", "Gaziantep FK",
            "Pendikspor", "Fatih Karagumruk", "Istanbulspor", "Ankaragücü"
    };

    private static final String[] FALLBACK_PLAYERS = {
            "Ali Yilmaz", "Mehmet Demir", "Ahmet Kaya", "Mustafa Ozturk",
            "Hasan Celik", "Huseyin Dogan", "Ibrahim Arslan", "Ismail Koc"
    };

    private static final String[] COACH_SPECIALITIES = {
            "Fitness", "Attacking", "Defending", "Goalkeeping", "Tactics"
    };

    private static final int MATCH_DAY_CAP = new FootballSport().getMaxMatchSquadSize();

    private final Random random = new Random();

    private List<String> malePool() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/football/players_male.txt");
        return lines.isEmpty() ? List.of(FALLBACK_PLAYERS) : lines;
    }

    private String randomPlayerName() {
        List<String> pool = malePool();
        return pool.get(random.nextInt(pool.size()));
    }

    private List<String> coachNamePool() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/football/coaches.txt");
        if (!lines.isEmpty()) {
            return lines;
        }
        return List.of("Fatih Terim", "Şenol Güneş", "Okan Buruk", "Abdullah Avcı", "Vincenzo Montella");
    }

    @Override
    public ILeague createLeague(String name, List<ITeam> teams) {
        return new FootballLeague(name, teams);
    }

    @Override
    public ITeam createTeam(String name, String logo) {
        FootballTeam team = new FootballTeam(name, logo);

        team.addPlayer(generateRandomPlayer(FootballPosition.GK));
        team.addPlayer(generateRandomPlayer(FootballPosition.GK));

        team.addPlayer(generateRandomPlayer(FootballPosition.CB));
        team.addPlayer(generateRandomPlayer(FootballPosition.CB));
        team.addPlayer(generateRandomPlayer(FootballPosition.CB));
        team.addPlayer(generateRandomPlayer(FootballPosition.LB));
        team.addPlayer(generateRandomPlayer(FootballPosition.RB));

        team.addPlayer(generateRandomPlayer(FootballPosition.CDM));
        team.addPlayer(generateRandomPlayer(FootballPosition.CM));
        team.addPlayer(generateRandomPlayer(FootballPosition.CM));
        team.addPlayer(generateRandomPlayer(FootballPosition.CAM));
        team.addPlayer(generateRandomPlayer(FootballPosition.LM));
        team.addPlayer(generateRandomPlayer(FootballPosition.RM));

        team.addPlayer(generateRandomPlayer(FootballPosition.LW));
        team.addPlayer(generateRandomPlayer(FootballPosition.RW));
        team.addPlayer(generateRandomPlayer(FootballPosition.ST));
        team.addPlayer(generateRandomPlayer(FootballPosition.ST));
        team.addPlayer(generateRandomPlayer(FootballPosition.CF));

        List<String> coachesPick = new ArrayList<>(coachNamePool());
        Collections.shuffle(coachesPick, random);
        int coachCount = Math.min(3, Math.max(2, coachesPick.size() >= 2 ? 2 + random.nextInt(2) : coachesPick.size()));
        for (int i = 0; i < coachCount && i < coachesPick.size(); i++) {
            team.getCoaches().add(createCoach(coachesPick.get(i)));
        }

        team.initializeMatchDaySquad(MATCH_DAY_CAP);
        return team;
    }

    @Override
    public IPlayer createPlayer(String name, int age, Position position) {
        FootballPosition fbPos = mapToFootballPosition(position);
        return generatePlayerWithAttributes(name, age, fbPos);
    }

    @Override
    public ICoach createCoach(String name) {
        String speciality = COACH_SPECIALITIES[random.nextInt(COACH_SPECIALITIES.length)];
        return new FootballCoach(name, speciality);
    }

    @Override
    public IMatchEngine createMatchEngine() {
        return new FootballMatchEngine();
    }

    public List<Tactic> generateTactics() {
        List<Tactic> tactics = new ArrayList<>();
        tactics.add(new Tactic("4-4-2", 1.00, 1.00));
        tactics.add(new Tactic("4-3-3", 1.15, 0.90));
        tactics.add(new Tactic("3-5-2", 1.10, 0.95));
        tactics.add(new Tactic("4-2-3-1", 1.05, 1.05));
        tactics.add(new Tactic("5-3-2", 0.90, 1.15));
        return tactics;
    }

    public FootballPlayer generateRandomPlayer(FootballPosition pos) {
        String name = randomPlayerName();
        int age = 18 + random.nextInt(18);
        return generatePlayerWithAttributes(name, age, pos);
    }

    private FootballPlayer generatePlayerWithAttributes(String name, int age, FootballPosition pos) {
        int base = 40 + random.nextInt(40);
        int pace = clampStat(base + random.nextInt(20) - 10);
        int shooting = clampStat(base + random.nextInt(20) - 10);
        int passing = clampStat(base + random.nextInt(20) - 10);
        int dribbling = clampStat(base + random.nextInt(20) - 10);
        int defending = clampStat(base + random.nextInt(20) - 10);
        int physical = clampStat(base + random.nextInt(20) - 10);
        int goalkeeping = clampStat(base + random.nextInt(20) - 10);

        if (pos == FootballPosition.GK) {
            goalkeeping = clampStat(goalkeeping + 20);
        }

        return new FootballPlayer(name, age, pos, pace, shooting, passing,
                dribbling, defending, physical, goalkeeping);
    }

    private int clampStat(int value) {
        return Math.max(1, Math.min(99, value));
    }

    private FootballPosition mapToFootballPosition(Position pos) {
        return switch (pos) {
            case GK -> FootballPosition.GK;
            case DF -> FootballPosition.CB;
            case MF -> FootballPosition.CM;
            case FW -> FootballPosition.ST;
            default -> FootballPosition.CM;
        };
    }

    @Override
    public String[] getTeamNames() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/football/teams.txt");
        if (lines.isEmpty()) {
            return FALLBACK_TEAMS.clone();
        }
        return lines.toArray(new String[0]);
    }

    public String[] getPlayerNames() {
        return malePool().toArray(new String[0]);
    }
}
