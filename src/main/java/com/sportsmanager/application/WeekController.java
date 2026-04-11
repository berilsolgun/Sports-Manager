package com.sportsmanager.application;

import com.sportsmanager.domain.session.GameSession;

/**
 * Advances the in-game calendar by one week.
 */
public class WeekController {

    public void advanceWeek(GameSession session) {
        session.setCurrentWeek(session.getCurrentWeek() + 1);
    }
}
