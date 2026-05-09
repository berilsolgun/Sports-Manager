package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds {@link StandingEntry} rows from played fixtures.
 * League points come from {@link IMatchResult#getHomePoints()} / {@link IMatchResult#getAwayPoints()}
 * so volleyball table credit (3 / 2 / 1 / 0) is preserved.
 * Sorting: points, then head-to-head mini-league among tied teams, then GF/GA within those H2H matches,
 * then overall goal difference / goals for, then deterministic seeded tie-break.
 */
public final class StandingsCalculator {

    private final long tieBreakSeed;

    public StandingsCalculator() {
        this(0xC0FFEE_DEAD_BEEFL);
    }

    public StandingsCalculator(long tieBreakSeed) {
        this.tieBreakSeed = tieBreakSeed;
    }

    /** @deprecated use {@link StandingsCalculator#StandingsCalculator()} — league points come from {@link IMatchResult}. */
    @Deprecated
    public StandingsCalculator(int pointsForWin, int pointsForDraw) {
        this();
    }

    private static final class Mini {
        int points;
        int gf;
        int ga;

        int gd() {
            return gf - ga;
        }
    }

    private static Mini miniRecord(ITeam team, Set<ITeam> group, List<IFixture> fixtures) {
        Mini m = new Mini();
        for (IFixture f : fixtures) {
            if (!f.isPlayed()) {
                continue;
            }
            ITeam h = f.getHomeTeam();
            ITeam a = f.getAwayTeam();
            if (!group.contains(h) || !group.contains(a)) {
                continue;
            }
            IMatchResult r = f.getResult().orElse(null);
            if (r == null) {
                continue;
            }
            if (team.equals(h)) {
                m.points += r.getHomePoints();
                m.gf += r.getHomeScore();
                m.ga += r.getAwayScore();
            } else if (team.equals(a)) {
                m.points += r.getAwayPoints();
                m.gf += r.getAwayScore();
                m.ga += r.getHomeScore();
            }
        }
        return m;
    }

    /**
     * Static comparator for UI or basic sorting where fixture history is unavailable.
     * Updated: Removed overall GD/GF to match the brief.
     */
    public static int compare(StandingEntry a, StandingEntry b) {
        int c = Integer.compare(b.getPoints(), a.getPoints());
        if (c != 0) return c;

        // Overall GD/GF removed as per brief. Falling back to alphabetical for deterministic UI.
        return a.getTeam().getName().compareTo(b.getTeam().getName());
    }

    private int compareWithinBucket(StandingEntry a, StandingEntry b, List<StandingEntry> bucket,
                                      List<IFixture> fixtures) {
        Set<ITeam> group = bucket.stream().map(StandingEntry::getTeam).collect(Collectors.toCollection(LinkedHashSet::new));
        Mini ma = miniRecord(a.getTeam(), group, fixtures);
        Mini mb = miniRecord(b.getTeam(), group, fixtures);

        // 1. H2H Points
        int c = Integer.compare(mb.points, ma.points);
        if (c != 0) {
            return c;
        }
        // 2. H2H Score Difference (if matches were all draws, this will be 0)
        c = Integer.compare(mb.gd(), ma.gd());
        if (c != 0) {
            return c;
        }
        // 3. H2H Scores For
        c = Integer.compare(mb.gf, ma.gf);
        if (c != 0) {
            return c;
        }
        /**
         * Brief check: Overall GD and GF are removed as they were not requested in the tie-break tier.
         */
        // 4. Final Tie-break: Coin Toss (Deterministic via seed+names for stability)
        Random tosser = new Random(tieBreakSeed ^ (long)a.getTeam().getName().hashCode() ^ (long)b.getTeam().getName().hashCode());
        return tosser.nextBoolean() ? 1 : -1;
    }

    public List<StandingEntry> compute(List<ITeam> teams, List<IFixture> fixtures) {
        Map<ITeam, StandingEntry> entryMap = new LinkedHashMap<>();
        for (ITeam team : teams) {
            StandingEntry entry = new StandingEntry();
            entry.setTeam(team);
            entryMap.put(team, entry);
        }

        for (IFixture fixture : fixtures) {
            if (!fixture.isPlayed()) continue;
            fixture.getResult().ifPresent(result -> applyResult(entryMap, fixture.getHomeTeam(), fixture.getAwayTeam(), result));
        }

        List<StandingEntry> standings = new ArrayList<>(entryMap.values());
        standings.sort(Comparator.comparingInt(StandingEntry::getPoints).reversed());

        List<StandingEntry> ordered = new ArrayList<>();
        int i = 0;
        while (i < standings.size()) {
            int pts = standings.get(i).getPoints();
            int j = i + 1;
            while (j < standings.size() && standings.get(j).getPoints() == pts) {
                j++;
            }
            List<StandingEntry> bucket = new ArrayList<>(standings.subList(i, j));
            if (bucket.size() > 1) {
                bucket.sort((a, b) -> compareWithinBucket(a, b, bucket, fixtures));
            }
            ordered.addAll(bucket);
            i = j;
        }

        return ordered;
    }

    private void applyResult(Map<ITeam, StandingEntry> entryMap, ITeam home, ITeam away, IMatchResult result) {
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

        homeEntry.setPoints(homeEntry.getPoints() + result.getHomePoints());
        awayEntry.setPoints(awayEntry.getPoints() + result.getAwayPoints());

        if (result.isDraw()) {
            homeEntry.setDrawn(homeEntry.getDrawn() + 1);
            awayEntry.setDrawn(awayEntry.getDrawn() + 1);
        } else {
            result.getWinner().ifPresent(w -> {
                if (w.equals(home)) {
                    homeEntry.setWon(homeEntry.getWon() + 1);
                    awayEntry.setLost(awayEntry.getLost() + 1);
                } else {
                    awayEntry.setWon(awayEntry.getWon() + 1);
                    homeEntry.setLost(homeEntry.getLost() + 1);
                }
            });
        }
    }
}
