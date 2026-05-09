package com.sportsmanager.application;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.ITeam;

/**
 * Advances the in-game calendar by one week.
 * Each coach runs {@link com.sportsmanager.domain.team.ICoach#conductTraining} on their squad between weeks.
 */
public class WeekController {

    public void advanceWeek(GameSession session) {
        runWeeklyTraining(session);
        session.setCurrentWeek(session.getCurrentWeek() + 1);
    }

    private void runWeeklyTraining(GameSession session) {
        if (session.getLeague() == null) {
            return;
        }
        for (ITeam team : session.getLeague().getTeams()) {
            for (ICoach coach : team.getCoaches()) {
                coach.conductTraining(team.getSquad());
            }
        }
    }
}
