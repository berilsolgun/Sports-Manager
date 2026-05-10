package com.sportsmanager.domain.session;

import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.team.ITeam;

/**
 * Holds the entire state of a running game: which sport, which league,
 * which team the player manages, and the current calendar position.
 */
public class GameSession {

    private ITeam playerTeam;
    private ILeague league;
    private Sport sport;
    private int currentWeek;
    private int season;
    private int championshipCount;
    private boolean trainedThisWeek;

    public ITeam getPlayerTeam() {
        return playerTeam;
    }

    public void setPlayerTeam(ITeam playerTeam) {
        this.playerTeam = playerTeam;
    }

    public ILeague getLeague() {
        return league;
    }

    public void setLeague(ILeague league) {
        this.league = league;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public void setCurrentWeek(int currentWeek) {
        if (currentWeek < 0) {
            throw new IllegalArgumentException("Week cannot be negative");
        }
        this.currentWeek = currentWeek;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getChampionshipCount() {
        return championshipCount;
    }

    public void setChampionshipCount(int championshipCount) {
        this.championshipCount = championshipCount;
    }

    public void incrementChampionshipCount() {
        this.championshipCount++;
    }

    public boolean isTrainedThisWeek() {
        return trainedThisWeek;
    }

    public void setTrainedThisWeek(boolean trainedThisWeek) {
        this.trainedThisWeek = trainedThisWeek;
    }

    @Override
    public String toString() {
        String sportName = (sport != null) ? sport.getName() : "N/A";
        String teamName = (playerTeam != null) ? playerTeam.getName() : "N/A";
        return "GameSession{sport=" + sportName
                + ", team=" + teamName
                + ", week=" + currentWeek
                + ", season=" + season + "}";
    }
}
