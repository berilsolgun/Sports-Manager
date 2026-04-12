package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds {@link StandingEntry} rows from played fixtures and sorts by points,
 * goal difference, goals scored, then team name.
 */
public final class StandingsCalculator {

    private final int pointsForWin;
    private final int pointsForDraw;

    public StandingsCalculator(int pointsForWin, int pointsForDraw) {
        this.pointsForWin = pointsForWin;
        this.pointsForDraw = pointsForDraw;
    }

    /**
     * Comparator order: higher points first, then higher goal difference,
     * then more goals for, then team name ascending (stable tie-break).
     *
     * @return negative if {@code a} ranks above {@code b}, positive if below, 0 if equal
     */
    public static int compare(StandingEntry a, StandingEntry b) {
        int c = Integer.compare(b.getPoints(), a.getPoints());
        if (c != 0) {
            return c;
        }
        int gdA = a.getGoalsFor() - a.getGoalsAgainst();
        int gdB = b.getGoalsFor() - b.getGoalsAgainst();
        c = Integer.compare(gdB, gdA);
        if (c != 0) {
            return c;
        }
        c = Integer.compare(b.getGoalsFor(), a.getGoalsFor());
        if (c != 0) {
            return c;
        }
        return a.getTeam().getName().compareTo(b.getTeam().getName());
    }

    public List<StandingEntry> compute(List<ITeam> teams, List<IFixture> fixtures) {
        Map<ITeam, StandingEntry> entryMap = new LinkedHashMap<>();
        for (ITeam team : teams) {
            StandingEntry entry = new StandingEntry();
            entry.setTeam(team);
            entryMap.put(team, entry);
        }

        for (IFixture fixture : fixtures) {
            if (!fixture.isPlayed()) {
                continue;
            }
            IMatchResult result = fixture.getResult().orElse(null);
            if (result == null) {
                continue;
            }
            applyResult(entryMap, fixture.getHomeTeam(), fixture.getAwayTeam(), result);
        }

        List<StandingEntry> standings = new ArrayList<>(entryMap.values());
        standings.sort(StandingsCalculator::compare);
        return standings;
    }

    private void applyResult(Map<ITeam, StandingEntry> entryMap, ITeam home, ITeam away,
                             IMatchResult result) {
        StandingEntry homeEntry = entryMap.get(home);
        StandingEntry awayEntry = entryMap.get(away);
        if (homeEntry == null || awayEntry == null) {
            return;
        }

        homeEntry.setPlayed(homeEntry.getPlayed() + 1);
        awayEntry.setPlayed(awayEntry.getPlayed() + 1);

        homeEntry.setGoalsFor(homeEntry.getGoalsFor() + result.getHomeScore());
        homeEntry.setGoalsAgainst(homeEntry.getGoalsAgainst() + result.getAwayScore());
        awayEntry.setGoalsFor(awayEntry.getGoalsFor() + result.getAwayScore());
        awayEntry.setGoalsAgainst(awayEntry.getGoalsAgainst() + result.getHomeScore());

        if (result.isDraw()) {
            homeEntry.setDrawn(homeEntry.getDrawn() + 1);
            awayEntry.setDrawn(awayEntry.getDrawn() + 1);
            homeEntry.setPoints(homeEntry.getPoints() + pointsForDraw);
            awayEntry.setPoints(awayEntry.getPoints() + pointsForDraw);
        } else if (result.getHomeScore() > result.getAwayScore()) {
            homeEntry.setWon(homeEntry.getWon() + 1);
            awayEntry.setLost(awayEntry.getLost() + 1);
            homeEntry.setPoints(homeEntry.getPoints() + pointsForWin);
        } else {
            awayEntry.setWon(awayEntry.getWon() + 1);
            homeEntry.setLost(homeEntry.getLost() + 1);
            awayEntry.setPoints(awayEntry.getPoints() + pointsForWin);
        }
    }
}
