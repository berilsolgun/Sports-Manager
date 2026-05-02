package com.sportsmanager.volleyball;

import com.sportsmanager.domain.team.Position;

public enum VolleyballPosition {
    SETTER(Position.SET),
    OUTSIDE_HITTER(Position.HIT),
    OPPOSITE_HITTER(Position.HIT),
    MIDDLE_BLOCKER(Position.HIT),
    LIBERO(Position.LIB);

    private final Position genericPosition;

    VolleyballPosition(Position genericPosition) {
        this.genericPosition = genericPosition;
    }

    public Position getGenericPosition() {
        return genericPosition;
    }

    public boolean isSetter() {
        return this == SETTER;
    }

    public boolean isLibero() {
        return this == LIBERO;
    }

    public boolean isHitterGroup() {
        return genericPosition == Position.HIT;
    }

    public boolean isDefensiveSpecialist() {
        return genericPosition == Position.LIB;
    }
}