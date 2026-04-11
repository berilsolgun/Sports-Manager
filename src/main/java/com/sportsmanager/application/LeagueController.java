package com.sportsmanager.application;

import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;

import java.util.List;

/**
 * Reads the latest league table from the active session (recomputes from recorded results).
 */
public class LeagueController {

    public List<StandingEntry> getStandings(GameSession session) {
        if (session.getLeague() == null) {
            throw new IllegalStateException("GameSession has no league");
        }
        return session.getLeague().getStandings();
    }

    public boolean isSeasonComplete(GameSession session) {
        if (session.getLeague() == null) {
            return true;
        }
        return session.getLeague().isSeasonOver();
    }
}
