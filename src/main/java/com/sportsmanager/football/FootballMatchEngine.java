package com.sportsmanager.football;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.*;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FootballMatchEngine extends AbstractMatchEngine {

    private static final double GOAL_BASE_PROBABILITY = 0.08;
    private static final double SUB_PROBABILITY = 0.12;
    private static final double INJURY_EVENT_PROBABILITY = 0.015;
    private final Random random;

    private int phasesCompleted;
    private int cumulativeHomeGoals;
    private int cumulativeAwayGoals;

    public FootballMatchEngine() {
        this(new Random());
    }

    public FootballMatchEngine(Random random) {
        this.random = random;
    }

    @Override
    protected int getPhaseCount() {
        return 2;
    }

    @Override
    protected void resetStepwiseState() {
        phasesCompleted = 0;
        cumulativeHomeGoals = 0;
        cumulativeAwayGoals = 0;
    }

    @Override
    public boolean isMatchComplete() {
        return matchStarted && phasesCompleted >= getPhaseCount();
    }

    @Override
    public PhaseResult playNextPhase() {
        if (!matchStarted || matchFinished) {
            throw new IllegalStateException("Invalid playNextPhase state");
        }
        if (phasesCompleted >= getPhaseCount()) {
            throw new IllegalStateException("All phases already played");
        }
        int phase = phasesCompleted + 1;
        PhaseResult pr = simulatePhaseInternal(ctxHome, ctxAway, phase);
        cumulativeHomeGoals += pr.homeScore;
        cumulativeAwayGoals += pr.awayScore;
        phasesCompleted++;
        if (phasesCompleted >= getPhaseCount()) {
            matchFinished = true;
        }
        return pr;
    }

    @Override
    protected IMatchResult buildFinalResult() {
        return finishMatch(ctxHome, ctxAway, cumulativeHomeGoals, cumulativeAwayGoals);
    }

    @Override
    protected IMatchResult finishMatch(ITeam home, ITeam away, int homeScore, int awayScore) {
        List<IPlayer> injured = calculateInjuries(home, away);
        return new FootballMatchResult(homeScore, awayScore, home, away, copyMatchEvents(), injured);
    }

    @Override
    public PhaseResult simulatePhase(ITeam home, ITeam away, int phase) {
        return simulatePhaseInternal(home, away, phase);
    }

    private PhaseResult simulatePhaseInternal(ITeam home, ITeam away, int phase) {
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
            maybeSubstitutionOrInjuryEvent(home, away, phase, minute);
            double homeChance = GOAL_BASE_PROBABILITY * (homeAttack / (awayDefense + 1));
            if (random.nextDouble() < homeChance) {
                IPlayer scorer = pickRandomAttacker(home);
                MatchEvent event = new MatchEvent(minute, phase, MatchEventType.GOAL,
                        scorer.getName() + " scores!", home, scorer);
                recordMatchEvent(event);
                result.events.add(event);
                result.homeScore++;
            }

            double awayChance = GOAL_BASE_PROBABILITY * (awayAttack / (homeDefense + 1));
            if (random.nextDouble() < awayChance) {
                IPlayer scorer = pickRandomAttacker(away);
                MatchEvent event = new MatchEvent(minute, phase, MatchEventType.GOAL,
                        scorer.getName() + " scores!", away, scorer);
                recordMatchEvent(event);
                result.events.add(event);
                result.awayScore++;
            }
        }

        return result;
    }

    private void maybeSubstitutionOrInjuryEvent(ITeam home, ITeam away, int phase, int minute) {
        if (random.nextDouble() < SUB_PROBABILITY) {
            ITeam side = random.nextBoolean() ? home : away;
            List<IPlayer> starters = side.getStartingEleven();
            List<IPlayer> bench = side.getMatchDaySquad();
            if (starters != null && !starters.isEmpty() && bench != null && bench.size() > starters.size()) {
                IPlayer out = starters.get(random.nextInt(starters.size()));
                List<IPlayer> benchOnly = bench.stream().filter(p -> !starters.contains(p)).toList();
                if (!benchOnly.isEmpty()) {
                    IPlayer in = benchOnly.get(random.nextInt(benchOnly.size()));
                    MatchEvent ev = new MatchEvent(minute, phase, MatchEventType.SUBSTITUTION,
                            out.getName() + " off, " + in.getName() + " on", side, in);
                    recordMatchEvent(ev);
                }
            }
        }
        if (random.nextDouble() < INJURY_EVENT_PROBABILITY) {
            ITeam side = random.nextBoolean() ? home : away;
            List<IPlayer> squad = side.getStartingEleven();
            if (squad == null || squad.isEmpty()) {
                squad = side.getSquad();
            }
            List<IPlayer> avail = squad.stream().filter(p -> !p.isInjured()).toList();
            if (!avail.isEmpty()) {
                IPlayer victim = avail.get(random.nextInt(avail.size()));
                MatchEvent ev = new MatchEvent(minute, phase, MatchEventType.INJURY,
                        victim.getName() + " picks up a knock", side, victim);
                recordMatchEvent(ev);
            }
        }
    }

    private double calculateAttackStrength(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) {
            squad = team.getSquad();
        }
        double base = squad.stream()
                .filter(p -> !p.isInjured())
                .mapToInt(IPlayer::getOverallRating)
                .average()
                .orElse(50.0) / 50.0;

        double tacticBonus = (team.getCurrentTactic() != null) ? team.getCurrentTactic().getAttackBonus() : 1.0;
        return base * tacticBonus;
    }

    private double calculateDefenseStrength(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) {
            squad = team.getSquad();
        }
        double base = squad.stream()
                .filter(p -> !p.isInjured())
                .mapToInt(IPlayer::getOverallRating)
                .average()
                .orElse(50.0) / 50.0;

        double tacticBonus = (team.getCurrentTactic() != null) ? team.getCurrentTactic().getDefenseBonus() : 1.0;
        return base * tacticBonus;
    }

    private IPlayer pickRandomAttacker(ITeam team) {
        List<IPlayer> squad = team.getStartingEleven();
        if (squad == null || squad.isEmpty()) {
            squad = team.getSquad();
        }
        List<IPlayer> available = squad.stream().filter(p -> !p.isInjured()).toList();
        if (available.isEmpty()) {
            return squad.get(0);
        }
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
