package com.sportsmanager.domain.simulation;

import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

public class MatchEvent {
    private final int minute;
    private final int phase;
    private final MatchEventType type;
    private final String description;
    private final ITeam team;
    private final IPlayer player;

    public MatchEvent(int minute, int phase, MatchEventType type, String description, ITeam team, IPlayer player) {
        this.minute = minute;
        this.phase = phase;
        this.type = type;
        this.description = description;
        this.team = team;
        this.player = player;
    }

    public int getMinute() {
        return minute;
    }

    public int getPhase() {
        return phase;
    }

    public MatchEventType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public ITeam getTeam() {
        return team;
    }

    public IPlayer getPlayer() {
        return player;
    }
}
