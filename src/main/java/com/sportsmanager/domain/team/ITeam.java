package com.sportsmanager.domain.team;

import java.util.List;

public interface ITeam {
    String getName();
    String getLogo();
    List<IPlayer> getSquad();
    List<ICoach> getCoaches();
    Tactic getCurrentTactic();
    void setTactic(Tactic tactic);
    List<IPlayer> getStartingEleven();
    void setStartingEleven(List<IPlayer> players);
    void addPlayer(IPlayer player);
    void removePlayer(IPlayer player);

}
