package com.sportsmanager.application;

import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballLeague;
import com.sportsmanager.volleyball.VolleyballLeague;

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

    public ITeam getTableLeader(GameSession session) {
        List<StandingEntry> table = getStandings(session);
        return table.isEmpty() ? null : table.get(0).getTeam();
    }

    /** Increments season number, heals and ages players, resets fixtures on the concrete league implementation. */
    public void prepareNewSeason(GameSession session) {
        if (session.getLeague() == null || session.getSport() == null) {
            throw new IllegalStateException("GameSession incomplete");
        }
        int maxMd = session.getSport().getMaxMatchSquadSize();
        session.setSeason(session.getSeason() + 1);
        session.setCurrentWeek(1);

        for (ITeam team : session.getLeague().getTeams()) {
            for (IPlayer p : team.getSquad()) {
                p.healFully();
                p.incrementAge();
            }
            team.resetForNewSeason(maxMd);
        }

        if (session.getLeague() instanceof FootballLeague fl) {
            fl.resetSeason();
        } else if (session.getLeague() instanceof VolleyballLeague vl) {
            vl.resetSeason();
        }
    }
}
