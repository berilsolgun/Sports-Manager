package com.sportsmanager.domain.team;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTeam implements ITeam {
    protected String name;
    protected String logo;
    protected List<IPlayer> squad = new ArrayList<>();
    protected List<IPlayer> startingEleven = new ArrayList<>();
    /** Empty list means "no explicit match-day filter" — treat whole squad as eligible. */
    protected List<IPlayer> matchDaySquad = new ArrayList<>();
    protected List<ICoach> coaches = new ArrayList<>();
    protected Tactic currentTactic;

    public AbstractTeam(String name, String logo) {
        this.name = name;
        this.logo = logo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLogo() {
        return logo;
    }

    @Override
    public List<IPlayer> getSquad() {
        return squad;
    }

    @Override
    public List<IPlayer> getMatchDaySquad() {
        if (matchDaySquad == null || matchDaySquad.isEmpty()) {
            return squad;
        }
        return matchDaySquad;
    }

    @Override
    public void setMatchDaySquad(List<IPlayer> players, int maxSize) {
        if (players == null) {
            throw new IllegalArgumentException("players required");
        }
        if (players.size() > maxSize) {
            throw new IllegalArgumentException("match-day squad exceeds max " + maxSize);
        }
        for (IPlayer p : players) {
            if (!squad.contains(p)) {
                throw new IllegalArgumentException("match-day player must be in squad: " + p.getName());
            }
        }
        this.matchDaySquad = new ArrayList<>(new LinkedHashSet<>(players));
    }

    @Override
    public void initializeMatchDaySquad(int maxSize) {
        int n = Math.min(maxSize, squad.size());
        matchDaySquad = new ArrayList<>(squad.subList(0, n));
    }

    @Override
    public List<ICoach> getCoaches() {
        return coaches;
    }

    @Override
    public Tactic getCurrentTactic() {
        return currentTactic;
    }

    @Override
    public void setTactic(Tactic tactic) {
        this.currentTactic = tactic;
    }

    @Override
    public void addPlayer(IPlayer player) {
        squad.add(player);
    }

    @Override
    public void removePlayer(IPlayer player) {
        squad.remove(player);
    }

    public void addCoach(ICoach coach) {
        this.coaches.add(coach);
    }

    @Override
    public List<IPlayer> getStartingEleven() {
        return startingEleven;
    }

    @Override
    public void setStartingEleven(List<IPlayer> players) {
        List<IPlayer> eligible = getMatchDaySquad();
        if (players != null) {
            for (IPlayer p : players) {
                if (!eligible.contains(p)) {
                    throw new IllegalArgumentException("Starter must be in match-day squad: " + p.getName());
                }
            }
        }
        if (validateStartingEleven(players)) {
            this.startingEleven = players == null ? new ArrayList<>() : new ArrayList<>(players);
        } else {
            throw new IllegalArgumentException("This team is violating the rules of the sport!");
        }
    }

    @Override
    public void substitute(IPlayer out, IPlayer in) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(in, "in");
        if (startingEleven == null || startingEleven.isEmpty()) {
            throw new IllegalStateException("Starting lineup not set");
        }
        List<IPlayer> eligible = getMatchDaySquad();
        if (!eligible.contains(in)) {
            throw new IllegalArgumentException("Substitute must be on match-day squad");
        }
        if (startingEleven.contains(in)) {
            throw new IllegalArgumentException("Player already on pitch");
        }
        if (!startingEleven.contains(out)) {
            throw new IllegalArgumentException("Player not on pitch");
        }
        if (out.isInjured() || in.isInjured()) {
            throw new IllegalArgumentException("Injured players cannot substitute in/out this way");
        }
        int idx = startingEleven.indexOf(out);
        ArrayList<IPlayer> trial = new ArrayList<>(startingEleven);
        trial.set(idx, in);
        if (!validateStartingEleven(trial)) {
            throw new IllegalArgumentException("Substitution violates formation rules");
        }
        startingEleven.clear();
        startingEleven.addAll(trial);
    }

    protected abstract boolean validateStartingEleven(List<IPlayer> players);

    public abstract int getTeamRating();
}
