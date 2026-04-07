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
    Map<String,Integer> getAttributes();

}
