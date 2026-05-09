package com.sportsmanager.application;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

/**
 * Advances the in-game calendar by one week.
 * Also recovers injured players by one game per week.
 */
public class WeekController {

    public void advanceWeek(GameSession session) {
        // Recover injured players across all teams
        if (session.getLeague() != null) {
            for (ITeam team : session.getLeague().getTeams()) {
                for (IPlayer player : team.getSquad()) {
                    if (player.isInjured()) {
                        player.recoverOneGame();
                    }
                }
            }
        }

        session.setCurrentWeek(session.getCurrentWeek() + 1);
    }
}