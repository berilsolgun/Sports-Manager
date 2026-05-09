package com.sportsmanager.domain.team;

import java.util.Map;

public interface IPlayer {
    String getName();
    int getAge();
    Position getPosition();
    int getOverallRating();
    boolean isInjured();
    int getInjuryGamesRemaining();
    void injure(int games);
    void recoverOneGame();

    /** Clears injury timer (used when starting a new season). */
    void healFully();

    /** Increments age by one year (new season rollover). */
    void incrementAge();

    Map<String,Integer> getAttributes();

}
