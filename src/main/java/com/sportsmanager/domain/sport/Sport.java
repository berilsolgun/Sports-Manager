package com.sportsmanager.domain.sport;

public interface Sport {
    String getName();
    int getMaxSquadSize();
    int getMaxMatchSquadSize();
    int getMatchPhaseCount();
    SportFactory createFactory();
}
