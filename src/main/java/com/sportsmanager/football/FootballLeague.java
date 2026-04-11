package com.sportsmanager.football;

import com.sportsmanager.domain.league.AbstractLeague;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.league.StandingsCalculator;
import com.sportsmanager.domain.team.ITeam;

import java.util.List;
import java.util.stream.Collectors;

public class FootballLeague extends AbstractLeague {

    private static final int POINTS_FOR_WIN = 3;
    private static final int POINTS_FOR_DRAW = 1;

    private final StandingsCalculator standingsCalculator =
            new StandingsCalculator(POINTS_FOR_WIN, POINTS_FOR_DRAW);

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
