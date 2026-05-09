package com.sportsmanager.domain.sport;

public interface Sport {
    String getName();
    int getMaxSquadSize();
    int getMaxMatchSquadSize();

    /** Starters required on pitch (e.g. 11 football, 6 volleyball). */
    int getStartingLineupSize();

    int getMatchPhaseCount();
    SportFactory createFactory();
}
