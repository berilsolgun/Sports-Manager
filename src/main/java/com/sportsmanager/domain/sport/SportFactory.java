package com.sportsmanager.domain.sport;

import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.team.*;

import java.util.List;

public interface SportFactory {
    ILeague createLeague(String name, List<ITeam> teams);
    ITeam createTeam(String name, String logo);
    IPlayer createPlayer(String name, int age, Position position);
    ICoach createCoach(String name);
    IMatchEngine createMatchEngine();
    List<Tactic> generateTactics();

}
