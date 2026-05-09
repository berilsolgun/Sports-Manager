package com.sportsmanager.domain.team;

import java.util.List;

public interface ITeam {
    String getName();

    String getLogo();

    List<IPlayer> getSquad();

    /** Players eligible for this match day (bench + starters). Empty means entire {@link #getSquad()} is eligible. */
    List<IPlayer> getMatchDaySquad();

    /**
     * Sets match-day roster: must be subset of squad, size {@code <= maxSize}.
     */
    void setMatchDaySquad(List<IPlayer> players, int maxSize);

    /**
     * Fill match-day squad from current squad (first {@code maxSize} players).
     */
    void initializeMatchDaySquad(int maxSize);

    List<ICoach> getCoaches();

    Tactic getCurrentTactic();

    void setTactic(Tactic tactic);

    List<IPlayer> getStartingEleven();

    void setStartingEleven(List<IPlayer> players);

    void addPlayer(IPlayer player);

    void removePlayer(IPlayer player);

    /** Swap {@code out} (must be on pitch) with {@code in} (must be on bench, same match-day squad). */
    void substitute(IPlayer out, IPlayer in);

    int getTeamRating();

    /** Clears starters and rebuilds match-day squad cap for a new season rollover. */
    default void resetForNewSeason(int maxMatchSquadSize) {
        getStartingEleven().clear();
        initializeMatchDaySquad(maxMatchSquadSize);
    }
}
