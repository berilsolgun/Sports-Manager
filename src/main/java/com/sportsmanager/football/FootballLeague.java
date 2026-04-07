package com.sportsmanager.football;

import com.sportsmanager.domain.league.AbstractLeague;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.team.ITeam;

import java.util.*;
import java.util.stream.Collectors;

public class FootballLeague extends AbstractLeague {

    private static final int POINTS_FOR_WIN = 3;
    private static final int POINTS_FOR_DRAW = 1;

    public FootballLeague(String name, List<ITeam> teams) {
        super(name, teams);
        generateFixtures();
    }

    private void generateFixtures() {
        int n = teams.size();
        int week = 1;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                fixtures.add(new FootballFixture(teams.get(i), teams.get(j), week));
                week++;
            }
        }

        int secondHalfStart = week;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                fixtures.add(new FootballFixture(teams.get(j), teams.get(i), secondHalfStart));
                secondHalfStart++;
            }
        }
    }

    @Override
    public void recordResult(IFixture fixture, IMatchResult result) {
        if (fixture instanceof FootballFixture ff) {
            ff.setResult(result);
        }
    }

    @Override
    public List<IFixture> getWeekFixtures(int week) {
        return fixtures.stream()
                .filter(f -> f.getWeek() == week)
                .collect(Collectors.toList());
    }

    @Override
    public List<StandingEntry> getStandings() {
        Map<ITeam, StandingEntry> entryMap = new LinkedHashMap<>();
        for (ITeam team : teams) {
            StandingEntry entry = new StandingEntry();
            entry.setTeam(team);
            entryMap.put(team, entry);
        }

        for (IFixture fixture : fixtures) {
            if (!fixture.isPlayed()) continue;

            IMatchResult result = fixture.getResult().orElse(null);
            if (result == null) continue;

            ITeam home = fixture.getHomeTeam();
            ITeam away = fixture.getAwayTeam();
            StandingEntry homeEntry = entryMap.get(home);
            StandingEntry awayEntry = entryMap.get(away);

            homeEntry.setPlayed(homeEntry.getPlayed() + 1);
            awayEntry.setPlayed(awayEntry.getPlayed() + 1);

            homeEntry.setGoalsFor(homeEntry.getGoalsFor() + result.getHOmeScore());
            homeEntry.setGoalsAgainst(homeEntry.getGoalsAgainst() + result.getAwayScore());
            awayEntry.setGoalsFor(awayEntry.getGoalsFor() + result.getAwayScore());
            awayEntry.setGoalsAgainst(awayEntry.getGoalsAgainst() + result.getHOmeScore());

            if (result.isDraw()) {
                homeEntry.setDrawn(homeEntry.getDrawn() + 1);
                awayEntry.setDrawn(awayEntry.getDrawn() + 1);
                homeEntry.setPoints(homeEntry.getPoints() + POINTS_FOR_DRAW);
                awayEntry.setPoints(awayEntry.getPoints() + POINTS_FOR_DRAW);
            } else if (result.getHOmeScore() > result.getAwayScore()) {
                homeEntry.setWon(homeEntry.getWon() + 1);
                awayEntry.setLost(awayEntry.getLost() + 1);
                homeEntry.setPoints(homeEntry.getPoints() + POINTS_FOR_WIN);
            } else {
                awayEntry.setWon(awayEntry.getWon() + 1);
                homeEntry.setLost(homeEntry.getLost() + 1);
                awayEntry.setPoints(awayEntry.getPoints() + POINTS_FOR_WIN);
            }
        }

        List<StandingEntry> standings = new ArrayList<>(entryMap.values());
        standings.sort((a, b) -> {
            if (b.getPoints() != a.getPoints()) return b.getPoints() - a.getPoints();
            int gdA = a.getGoalsFor() - a.getGoalsAgainst();
            int gdB = b.getGoalsFor() - b.getGoalsAgainst();
            if (gdB != gdA) return gdB - gdA;
            if (b.getGoalsFor() != a.getGoalsFor()) return b.getGoalsFor() - a.getGoalsFor();
            return a.getTeam().getName().compareTo(b.getTeam().getName());
        });

        return standings;
    }

    @Override
    public boolean isSeasonOver() {
        return fixtures.stream().allMatch(IFixture::isPlayed);
    }

    public String getName() {
        return name;
    }
}
