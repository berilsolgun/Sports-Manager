package com.sportsmanager.volleyball;

import com.sportsmanager.data.ResourceLines;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.team.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VolleyballFactory implements SportFactory {

    private static final String[] FALLBACK_TEAMS = {
            "VakıfBank", "Eczacıbaşı Dynavit", "Fenerbahçe Opet", "THY",
            "Galatasaray Daikin", "Kuzeyboru", "Muratpaşa Bld", "Nilüfer Bld",
            "Çukurova Bld", "Sarıyer Bld", "Aydın B.Şehir", "PTT Spor",
            "Beşiktaş Ayos", "Karayolları"
    };

    private static final String[] FALLBACK_PLAYERS = {
            "Eda Erdem", "Zehra Gunes", "Ebrar Karakurt", "Melissa Vargas",
            "Cansu Ozbay", "Hande Baladin", "Simge Aköz", "Gizem Örge"
    };

    private static final String[] COACH_SPECIALITIES = {
            "Fitness", "Attacking", "Defending", "Playmaking", "Service"
    };

    private static final int MATCH_DAY_CAP = new VolleyballSport().getMaxMatchSquadSize();

    private final Random random = new Random();

    private List<String> playerPool() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/volleyball/players.txt");
        return lines.isEmpty() ? List.of(FALLBACK_PLAYERS) : lines;
    }

    private List<String> coachNamePool() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/volleyball/coaches.txt");
        if (!lines.isEmpty()) {
            return lines;
        }
        return List.of("Giovanni Guidetti", "Marcello Abbondanza", "Zoran Terzić", "Massimo Barbolini");
    }

    private String randomPlayerName() {
        List<String> pool = playerPool();
        return pool.get(random.nextInt(pool.size()));
    }

    @Override
    public ILeague createLeague(String name, List<ITeam> teams) {
        return new VolleyballLeague(name, teams);
    }

    @Override
    public ITeam createTeam(String name, String logo) {
        VolleyballTeam team = new VolleyballTeam(name, logo);

        team.addPlayer(generateRandomPlayer(VolleyballPosition.SETTER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.SETTER));

        team.addPlayer(generateRandomPlayer(VolleyballPosition.LIBERO));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.LIBERO));

        team.addPlayer(generateRandomPlayer(VolleyballPosition.MIDDLE_BLOCKER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.MIDDLE_BLOCKER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.MIDDLE_BLOCKER));

        team.addPlayer(generateRandomPlayer(VolleyballPosition.OUTSIDE_HITTER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.OUTSIDE_HITTER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.OUTSIDE_HITTER));

        team.addPlayer(generateRandomPlayer(VolleyballPosition.OPPOSITE_HITTER));
        team.addPlayer(generateRandomPlayer(VolleyballPosition.OPPOSITE_HITTER));

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
        VolleyballPosition vbPos = mapToVolleyballPosition(position);
        return generatePlayerWithAttributes(name, age, vbPos);
    }

    @Override
    public ICoach createCoach(String name) {
        String speciality = COACH_SPECIALITIES[random.nextInt(COACH_SPECIALITIES.length)];
        return new VolleyballCoach(name, speciality);
    }

    @Override
    public IMatchEngine createMatchEngine() {
        return new VolleyballMatchEngine();
    }

    @Override
    public List<Tactic> generateTactics() {
        List<Tactic> tactics = new ArrayList<>();
        tactics.add(new Tactic("5-1 System", 1.10, 1.00));
        tactics.add(new Tactic("4-2 System", 0.95, 1.05));
        tactics.add(new Tactic("6-2 System", 1.05, 1.05));
        return tactics;
    }

    public VolleyballPlayer generateRandomPlayer(VolleyballPosition pos) {
        String name = randomPlayerName();
        int age = 18 + random.nextInt(15);
        return generatePlayerWithAttributes(name, age, pos);
    }

    private VolleyballPlayer generatePlayerWithAttributes(String name, int age, VolleyballPosition pos) {
        int base = 45 + random.nextInt(35);

        int serving = clampStat(base + random.nextInt(20) - 10);
        int setting = clampStat(base + random.nextInt(20) - 10);
        int spiking = clampStat(base + random.nextInt(20) - 10);
        int blocking = clampStat(base + random.nextInt(20) - 10);
        int digging = clampStat(base + random.nextInt(20) - 10);
        int receiving = clampStat(base + random.nextInt(20) - 10);
        int physical = clampStat(base + random.nextInt(20) - 10);

        if (pos == VolleyballPosition.SETTER) {
            setting = clampStat(setting + 20);
        }
        if (pos == VolleyballPosition.LIBERO) {
            digging = clampStat(digging + 20);
        }
        if (pos == VolleyballPosition.MIDDLE_BLOCKER) {
            blocking = clampStat(blocking + 15);
        }

        return new VolleyballPlayer(name, age, pos, serving, setting, spiking,
                blocking, digging, receiving, physical);
    }

    private int clampStat(int value) {
        return Math.max(1, Math.min(99, value));
    }

    private VolleyballPosition mapToVolleyballPosition(Position pos) {
        return switch (pos) {
            case SET -> VolleyballPosition.SETTER;
            case LIB -> VolleyballPosition.LIBERO;
            case HIT -> VolleyballPosition.OUTSIDE_HITTER;
            default -> VolleyballPosition.OUTSIDE_HITTER;
        };
    }

    @Override
    public String[] getTeamNames() {
        List<String> lines = ResourceLines.load("/com/sportsmanager/data/volleyball/teams.txt");
        if (lines.isEmpty()) {
            return FALLBACK_TEAMS.clone();
        }
        return lines.toArray(new String[0]);
    }

    public String[] getPlayerNames() {
        return playerPool().toArray(new String[0]);
    }
}
