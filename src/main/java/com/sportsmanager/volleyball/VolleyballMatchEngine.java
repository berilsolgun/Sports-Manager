package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.*;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VolleyballMatchEngine extends AbstractMatchEngine {

    private static final double POINT_BASE_PROBABILITY = 0.5;
    private static final double SUB_PROBABILITY = 0.08;
    private static final double INJURY_EVENT_PROBABILITY = 0.01;
    private final Random random;

    private int homeSets;
    private int awaySets;
    private int nextSetNumber;

    public VolleyballMatchEngine() {
        this(new Random());
    }

    public VolleyballMatchEngine(Random random) {
        this.random = random;
    }

    @Override
    protected int getPhaseCount() {
        return 5;
    }

    @Override
    protected void resetStepwiseState() {
        homeSets = 0;
        awaySets = 0;
        nextSetNumber = 1;
    }

    @Override
    public boolean isMatchComplete() {
        return matchStarted && matchFinished;
    }

    @Override
    public PhaseResult playNextPhase() {
        if (!matchStarted || matchFinished) {
            throw new IllegalStateException("Invalid playNextPhase state");
        }
        if (homeSets >= 3 || awaySets >= 3) {
            throw new IllegalStateException("Match already decided");
        }
        PhaseResult pr = simulatePhaseInternal(ctxHome, ctxAway, nextSetNumber);
        if (pr.homeScore > pr.awayScore) {
            homeSets++;
        } else {
            awaySets++;
        }
        maybeVolleyballSubstitutionEvents(nextSetNumber, pr);
        nextSetNumber++;
        if (homeSets >= 3 || awaySets >= 3) {
            matchFinished = true;
        }
        return pr;
    }

    private void maybeVolleyballSubstitutionEvents(int setNumber, PhaseResult pr) {
        if (random.nextDouble() < SUB_PROBABILITY) {
            ITeam side = random.nextBoolean() ? ctxHome : ctxAway;
            List<IPlayer> starters = side.getStartingEleven();
            List<IPlayer> matchDay = side.getMatchDaySquad();
            if (starters != null && !starters.isEmpty() && matchDay != null && matchDay.size() > starters.size()) {
                IPlayer out = starters.get(random.nextInt(starters.size()));
                List<IPlayer> benchOnly = matchDay.stream().filter(p -> !starters.contains(p)).toList();
                if (!benchOnly.isEmpty()) {
                    IPlayer in = benchOnly.get(random.nextInt(benchOnly.size()));
                    recordMatchEvent(new MatchEvent(pr.homeScore + pr.awayScore, setNumber,
                            MatchEventType.SUBSTITUTION, out.getName() + " off, " + in.getName() + " on", side, in));
                }
            }
        }
        if (random.nextDouble() < INJURY_EVENT_PROBABILITY) {
            ITeam side = random.nextBoolean() ? ctxHome : ctxAway;
            List<IPlayer> squad = side.getStartingEleven();
            if (squad == null || squad.isEmpty()) {
                squad = side.getSquad();
            }
            List<IPlayer> avail = squad.stream().filter(p -> !p.isInjured()).toList();
            if (!avail.isEmpty()) {
                IPlayer victim = avail.get(random.nextInt(avail.size()));
                recordMatchEvent(new MatchEvent(pr.homeScore + pr.awayScore, setNumber,
                        MatchEventType.INJURY, victim.getName() + " injury concern", side, victim));
            }
        }
    }

    @Override
    protected IMatchResult buildFinalResult() {
        return finishMatch(ctxHome, ctxAway, homeSets, awaySets);
    }

    @Override
    protected IMatchResult finishMatch(ITeam home, ITeam away, int homeSets, int awaySets) {
        List<IPlayer> injured = calculateInjuries(home, away);
        return new VolleyballMatchResult(homeSets, awaySets, home, away, copyMatchEvents(), injured);
    }

    @Override
    public PhaseResult simulatePhase(ITeam home, ITeam away, int setNumber) {
        return simulatePhaseInternal(home, away, setNumber);
    }

    private PhaseResult simulatePhaseInternal(ITeam home, ITeam away, int setNumber) {
        PhaseResult result = new PhaseResult();
        result.homeScore = 0;
        result.awayScore = 0;
        result.events = new ArrayList<>();

        int pointsNeeded = (setNumber == 5) ? 15 : 25;

        double homeStrength = calculateTeamStrength(home);
        double awayStrength = calculateTeamStrength(away);

        while (!isSetOver(result.homeScore, result.awayScore, pointsNeeded)) {
            double winProb = POINT_BASE_PROBABILITY * (homeStrength / (awayStrength + 0.1));

            if (random.nextDouble() < winProb) {
                result.homeScore++;
                addPointEvent(home, setNumber, result);
            } else {
                result.awayScore++;
                addPointEvent(away, setNumber, result);
            }
        }

        return result;
    }

    private boolean isSetOver(int s1, int s2, int limit) {
        return (s1 >= limit || s2 >= limit) && Math.abs(s1 - s2) >= 2;
    }

    private void addPointEvent(ITeam team, int set, PhaseResult result) {
        IPlayer scorer = pickRandomScorer(team);
        MatchEvent event = new MatchEvent(result.homeScore + result.awayScore, set,
                MatchEventType.GOAL, scorer.getName() + " wins the point!", team, scorer);
        recordMatchEvent(event);
        result.events.add(event);
    }

    private double calculateTeamStrength(ITeam team) {
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

    private IPlayer pickRandomScorer(ITeam team) {
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
        double injuryChance = 0.015;

        for (IPlayer p : home.getSquad()) {
            if (!p.isInjured() && random.nextDouble() < injuryChance) {
                p.injure(random.nextInt(2) + 1);
                injured.add(p);
            }
        }
        for (IPlayer p : away.getSquad()) {
            if (!p.isInjured() && random.nextDouble() < injuryChance) {
                p.injure(random.nextInt(2) + 1);
                injured.add(p);
            }
        }
        return injured;
    }
}
