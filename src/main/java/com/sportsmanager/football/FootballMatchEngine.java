package com.sportsmanager.football;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.*;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FootballMatchEngine implements IMatchEngine {

    private static final double GOAL_BASE_PROBABILITY = 0.08;
    private final Random random = new Random();
    private final List<MatchEvent> events = new ArrayList<>();

    @Override
    public IMatchResult simulate(ITeam home, ITeam away) {
        events.clear();
        int homeScore = 0;
        int awayScore = 0;

        for (int phase = 1; phase <= 2; phase++) {
            PhaseResult pr = simulatePhase(home, away, phase);
            homeScore += pr.homeScore;
            awayScore += pr.awayScore;
        }

        List<IPlayer> injured = calculateInjuries(home, away);
        return new FootballMatchResult(homeScore, awayScore, home, away,
                new ArrayList<>(events), injured);
    }

    @Override
    public PhaseResult simulatePhase(ITeam home, ITeam away, int phase) {
        PhaseResult result = new PhaseResult();
        result.homeScore = 0;
        result.awayScore = 0;
        result.events = new ArrayList<>();

        int startMinute = (phase == 1) ? 1 : 46;
        int endMinute = (phase == 1) ? 45 : 90;

        double homeAttack = calculateAttackStrength(home);
        double homeDefense = calculateDefenseStrength(home);
        double awayAttack = calculateAttackStrength(away);
        double awayDefense = calculateDefenseStrength(away);

        for (int minute = startMinute; minute <= endMinute; minute++) {
            double homeChance = GOAL_BASE_PROBABILITY * (homeAttack / (awayDefense + 1));
            if (random.nextDouble() < homeChance) {
                IPlayer scorer = pickRandomAttacker(home);
                MatchEvent event = new MatchEvent(minute, phase, MatchEventType.GOAL,
                        scorer.getName() + " scores!", home, scorer);
                events.add(event);
                result.events.add(event);
                result.homeScore++;
            }

            double awayChance = GOAL_BASE_PROBABILITY * (awayAttack / (homeDefense + 1));
            if (random.nextDouble() < awayChance) {
                IPlayer scorer = pickRandomAttacker(away);
                MatchEvent event = new MatchEvent(minute, phase, MatchEventType.GOAL,
                        scorer.getName() + " scores!", away, scorer);
                events.add(event);
                result.events.add(event);
                result.awayScore++;
            }
        }

        return result;
    }

    @Override
    public List<MatchEvent> getMatchEvents() {
        return events;
    }

    private double calculateAttackStrength(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) squad = team.getSquad();
        return squad.stream()
                .filter(p -> !p.isInjured())
                .mapToInt(IPlayer::getOverallRating)
                .average()
                .orElse(50.0) / 50.0;
    }

    private double calculateDefenseStrength(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) squad = team.getSquad();
        return squad.stream()
                .filter(p -> !p.isInjured())
                .mapToInt(IPlayer::getOverallRating)
                .average()
                .orElse(50.0) / 50.0;
    }

    private IPlayer pickRandomAttacker(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) squad = team.getSquad();
        List<IPlayer> available = squad.stream().filter(p -> !p.isInjured()).toList();
        if (available.isEmpty()) return squad.get(0);
        return available.get(random.nextInt(available.size()));
    }

    private List<IPlayer> calculateInjuries(ITeam home, ITeam away) {
        List<IPlayer> injured = new ArrayList<>();
        for (IPlayer p : home.getSquad()) {
            if (!p.isInjured() && random.nextDouble() < 0.02) {
                p.injure(random.nextInt(3) + 1);
                injured.add(p);
            }
        }
        for (IPlayer p : away.getSquad()) {
            if (!p.isInjured() && random.nextDouble() < 0.02) {
                p.injure(random.nextInt(3) + 1);
                injured.add(p);
            }
        }
        return injured;
    }
}
