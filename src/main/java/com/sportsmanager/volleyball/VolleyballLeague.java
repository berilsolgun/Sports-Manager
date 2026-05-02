package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.AbstractLeague;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.league.StandingsCalculator;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VolleyballLeague extends AbstractLeague {
    private static final int POINTS_FOR_WIN = 3;
    private static final int POINTS_FOR_DRAW = 0;

    private final StandingsCalculator standingsCalculator =
            new StandingsCalculator(POINTS_FOR_WIN, POINTS_FOR_DRAW);

    public VolleyballLeague(String name, List<ITeam> teams) {
        super(name, teams);
        generateFixtures();
    }

    private void generateFixtures() {
        int teamCount = teams.size();
        if (teamCount < 2) {
            return;
        }

        List<ITeam> roster = new ArrayList<>(teams);
        if (teamCount % 2 == 1) {
            roster.add(null);
        }
        int n = roster.size();
        int rounds = n - 1;
        int half = n / 2;
        int week = 1;

        List<List<VolleyballFixture>> firstHalfByRound = new ArrayList<>();

        for (int r = 0; r < rounds; r++) {
            List<VolleyballFixture> roundFixtures = new ArrayList<>();
            for (int i = 0; i < half; i++) {
                ITeam a = roster.get(i);
                ITeam b = roster.get(n - 1 - i);
                if (a == null || b == null) {
                    continue;
                }
                VolleyballFixture fx = new VolleyballFixture(a, b, week);
                fixtures.add(fx);
                roundFixtures.add(fx);
            }
            firstHalfByRound.add(roundFixtures);
            ITeam tail = roster.remove(n - 1);
            roster.add(1, tail);
            week++;
        }

        for (int r = 0; r < rounds; r++) {
            for (VolleyballFixture firstLeg : firstHalfByRound.get(r)) {
                fixtures.add(new VolleyballFixture(
                        firstLeg.getAwayTeam(),
                        firstLeg.getHomeTeam(),
                        week));
            }
            week++;
        }
    }

    @Override
    public void recordResult(IFixture fixture, IMatchResult result) {
        if (fixture instanceof VolleyballFixture vf) {
            vf.setResult(result);
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
        return standingsCalculator.compute(teams, fixtures);
    }

    @Override
    public boolean isSeasonOver() {
        return fixtures.stream().allMatch(IFixture::isPlayed);
    }

    public String getName() {
        return name;
    }
}