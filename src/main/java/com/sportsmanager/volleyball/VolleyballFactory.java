package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.team.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VolleyballFactory implements SportFactory {

    private static final String[] PLAYER_NAMES = {
            "Eda Erdem", "Zehra Gunes", "Ebrar Karakurt", "Melissa Vargas",
            "Cansu Ozbay", "Hande Baladin", "Simge Aköz", "Gizem Örge",
            "Meryem Boz", "Kübra Akman", "Saliha Şahin", "Elif Şahin",
            "Buse Ünal", "Tuğba Şenoğlu", "Ayça Aykaç", "Derya Cebecioğlu",
            "Yasemin Güveli", "Aslı Kalaç", "Meliha Diken", "İlkin Aydın"
    };

    private static final String[] TEAM_NAMES = {
            "VakıfBank", "Eczacıbaşı Dynavit", "Fenerbahçe Opet", "THY",
            "Galatasaray Daikin", "Kuzeyboru", "Muratpaşa Bld", "Nilüfer Bld",
            "Çukurova Bld", "Sarıyer Bld", "Aydın B.Şehir", "PTT Spor",
            "Beşiktaş Ayos", "Karayolları"
    };

    private static final String[] COACH_SPECIALITIES = {
            "Fitness", "Attacking", "Defending", "Playmaking", "Service"
    };

    private final Random random = new Random();

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

        Tactic t1 = new Tactic();
        t1.setName("5-1 System");
        tactics.add(t1);

        Tactic t2 = new Tactic();
        t2.setName("4-2 System");
        tactics.add(t2);

        Tactic t3 = new Tactic();
        t3.setName("6-2 System");
        tactics.add(t3);

        return tactics;
    }

    private VolleyballPlayer generateRandomPlayer(VolleyballPosition pos) {
        String name = PLAYER_NAMES[random.nextInt(PLAYER_NAMES.length)];
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

        if (pos == VolleyballPosition.SETTER) setting = clampStat(setting + 20);
        if (pos == VolleyballPosition.LIBERO) digging = clampStat(digging + 20);
        if (pos == VolleyballPosition.MIDDLE_BLOCKER) blocking = clampStat(blocking + 15);

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

    public String[] getTeamNames() {
        return TEAM_NAMES;
    }

    public String[] getPlayerNames() {
        return PLAYER_NAMES;
    }
}
