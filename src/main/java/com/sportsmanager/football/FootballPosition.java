package com.sportsmanager.football;

import com.sportsmanager.domain.team.Position;

public enum FootballPosition {
    GK(Position.GK),
    CB(Position.DF), LB(Position.DF), RB(Position.DF),
    CDM(Position.MF), CM(Position.MF), CAM(Position.MF), LM(Position.MF), RM(Position.MF),
    LW(Position.FW), RW(Position.FW), ST(Position.FW), CF(Position.FW);

    private final Position genericPosition;

    FootballPosition(Position genericPosition) {
        this.genericPosition = genericPosition;
    }

    public Position getGenericPosition() {
        return genericPosition;
    }

    public boolean isGoalkeeper() {
        return this == GK;
    }

    public boolean isDefender() {
        return genericPosition == Position.DF;
    }

    public boolean isMidfielder() {
        return genericPosition == Position.MF;
    }

    public boolean isAttacker() {
        return genericPosition == Position.FW;
    }
}
