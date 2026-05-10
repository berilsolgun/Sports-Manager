package com.sportsmanager.application;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.ITeam;

public class WeekController {

    public void advanceWeek(GameSession session) {
        runWeeklyTraining(session);
        session.setCurrentWeek(session.getCurrentWeek() + 1);
        session.setTrainedThisWeek(false);
    }

    private void runWeeklyTraining(GameSession session) {
        if (session.getLeague() == null) {
            return;
        }
        ITeam playerTeam = session.getPlayerTeam();
        for (ITeam team : session.getLeague().getTeams()) {
            if (team == playerTeam) {
                continue;
            }
            for (ICoach coach : team.getCoaches()) {
                coach.conductTraining(team.getSquad());
            }
        }
    }
}
