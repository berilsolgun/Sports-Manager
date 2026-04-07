package com.sportsmanager.domain.league;

import com.sportsmanager.domain.simulation.MatchEvent;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.List;
import java.util.Optional;

public interface IMatchResult {
    int getHOmeScore();
    int getAwayScore();
    Optional<ITeam> getWinner();
    boolean isDraw();
    int getHomePoints();
    int getAwayPoints();
    List<MatchEvent> getEvents();
    List<IPlayer> getInjuredPlayers();

}
