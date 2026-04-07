package com.sportsmanager.football;

import com.sportsmanager.domain.team.AbstractPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class FootballPlayer extends AbstractPlayer {

    private int pace;
    private int shooting;
    private int passing;
    private int dribbling;
    private int defending;
    private int physical;
    private int goalkeeping;
    private final FootballPosition footballPosition;

    public FootballPlayer(String name, int age, FootballPosition footballPosition,
                          int pace, int shooting, int passing, int dribbling,
                          int defending, int physical, int goalkeeping) {
        super(name, age, footballPosition.getGenericPosition());
        this.footballPosition = footballPosition;
        this.pace = pace;
        this.shooting = shooting;
        this.passing = passing;
        this.dribbling = dribbling;
        this.defending = defending;
        this.physical = physical;
        this.goalkeeping = goalkeeping;
    }

    public FootballPosition getFootballPosition() {
        return footballPosition;
    }

    @Override
    public int getOverallRating() {
        return switch (footballPosition) {
            case GK -> (int) (goalkeeping * 0.40 + physical * 0.20 + pace * 0.10
                    + passing * 0.10 + defending * 0.15 + shooting * 0.025 + dribbling * 0.025);
            case CB -> (int) (defending * 0.35 + physical * 0.25 + pace * 0.10
                    + passing * 0.10 + shooting * 0.05 + dribbling * 0.15);
            case LB, RB -> (int) (defending * 0.25 + pace * 0.20 + physical * 0.15
                    + passing * 0.15 + dribbling * 0.15 + shooting * 0.05 + goalkeeping * 0.05);
            case CDM -> (int) (defending * 0.30 + passing * 0.20 + physical * 0.20
                    + pace * 0.10 + dribbling * 0.10 + shooting * 0.05 + goalkeeping * 0.05);
            case CM -> (int) (passing * 0.25 + dribbling * 0.15 + defending * 0.15
                    + physical * 0.15 + shooting * 0.15 + pace * 0.10 + goalkeeping * 0.05);
            case CAM -> (int) (passing * 0.25 + dribbling * 0.20 + shooting * 0.25
                    + pace * 0.10 + physical * 0.10 + defending * 0.05 + goalkeeping * 0.05);
            case LM, RM -> (int) (pace * 0.20 + passing * 0.20 + dribbling * 0.20
                    + shooting * 0.15 + physical * 0.10 + defending * 0.10 + goalkeeping * 0.05);
            case LW, RW -> (int) (pace * 0.25 + dribbling * 0.25 + shooting * 0.20
                    + passing * 0.15 + physical * 0.10 + defending * 0.05);
            case ST, CF -> (int) (shooting * 0.30 + pace * 0.20 + dribbling * 0.15
                    + physical * 0.15 + passing * 0.10 + defending * 0.05 + goalkeeping * 0.05);
        };
    }

    @Override
    public int getInjuryGamesRemaining() {
        return injuryGamesRemaining;
    }

    @Override
    public Map<String, Integer> getAttributes() {
        Map<String, Integer> attrs = new LinkedHashMap<>();
        attrs.put("pace", pace);
        attrs.put("shooting", shooting);
        attrs.put("passing", passing);
        attrs.put("dribbling", dribbling);
        attrs.put("defending", defending);
        attrs.put("physical", physical);
        attrs.put("goalkeeping", goalkeeping);
        return attrs;
    }

    public int getPace() { return pace; }
    public int getShooting() { return shooting; }
    public int getPassing() { return passing; }
    public int getDribbling() { return dribbling; }
    public int getDefending() { return defending; }
    public int getPhysical() { return physical; }
    public int getGoalkeeping() { return goalkeeping; }

    public void setPace(int pace) { this.pace = pace; }
    public void setShooting(int shooting) { this.shooting = shooting; }
    public void setPassing(int passing) { this.passing = passing; }
    public void setDribbling(int dribbling) { this.dribbling = dribbling; }
    public void setDefending(int defending) { this.defending = defending; }
    public void setPhysical(int physical) { this.physical = physical; }
    public void setGoalkeeping(int goalkeeping) { this.goalkeeping = goalkeeping; }
}
