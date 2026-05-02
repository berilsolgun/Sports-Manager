package com.sportsmanager.volleyball;

import com.sportsmanager.domain.team.AbstractPlayer;
import java.util.LinkedHashMap;
import java.util.Map;

public class VolleyballPlayer extends AbstractPlayer {
    private int serving;
    private int setting;
    private int spiking;
    private int blocking;
    private int digging;
    private int receiving;
    private int physical;

    private final VolleyballPosition volleyballPosition;

    public VolleyballPlayer(String name, int age, VolleyballPosition volleyballPosition,
                            int serving, int setting, int spiking, int blocking,
                            int digging, int receiving, int physical) {
        super(name, age, volleyballPosition.getGenericPosition());
        this.volleyballPosition = volleyballPosition;
        this.serving = serving;
        this.setting = setting;
        this.spiking = spiking;
        this.blocking = blocking;
        this.digging = digging;
        this.receiving = receiving;
        this.physical = physical;
    }

    public VolleyballPosition getVolleyballPosition() {
        return volleyballPosition;
    }

    @Override
    public int getOverallRating() {
        return switch (volleyballPosition) {
            case SETTER -> (int) (setting * 0.45 + serving * 0.15 + digging * 0.15
                    + physical * 0.10 + blocking * 0.10 + receiving * 0.05);

            case OUTSIDE_HITTER -> (int) (spiking * 0.35 + receiving * 0.20 + serving * 0.15
                    + digging * 0.10 + blocking * 0.10 + physical * 0.10);

            case OPPOSITE_HITTER -> (int) (spiking * 0.40 + serving * 0.15 + physical * 0.15
                    + blocking * 0.15 + digging * 0.10 + setting * 0.05);

            case MIDDLE_BLOCKER -> (int) (blocking * 0.45 + spiking * 0.20 + physical * 0.15
                    + serving * 0.10 + digging * 0.05 + setting * 0.05);

            case LIBERO -> (int) (digging * 0.40 + receiving * 0.40 + setting * 0.10
                    + physical * 0.05 + serving * 0.05);
        };
    }

    @Override
    public int getInjuryGamesRemaining() {
        return injuryGamesRemaining;
    }

    @Override
    public Map<String, Integer> getAttributes() {
        Map<String, Integer> attrs = new LinkedHashMap<>();
        attrs.put("serving", serving);
        attrs.put("setting", setting);
        attrs.put("spiking", spiking);
        attrs.put("blocking", blocking);
        attrs.put("digging", digging);
        attrs.put("receiving", receiving);
        attrs.put("physical", physical);
        return attrs;
    }

    public int getServing() {
        return serving;
    }
    public void setServing(int serving) {
        this.serving = serving;
    }
    public int getSetting() {
        return setting;
    }
    public void setSetting(int setting) {
        this.setting = setting;
    }
    public int getSpiking() {
        return spiking;
    }
    public void setSpiking(int spiking) {
        this.spiking = spiking;
    }
    public int getBlocking() {
        return blocking;
    }
    public void setBlocking(int blocking) {
        this.blocking = blocking;
    }
    public int getDigging() {
        return digging;
    }
    public void setDigging(int digging) {
        this.digging = digging;
    }
    public int getReceiving() {
        return receiving;
    }
    public void setReceiving(int receiving) {
        this.receiving = receiving;
    }
    public int getPhysical() {
        return physical;
    }
    public void setPhysical(int physical) {
        this.physical = physical;
    }
}
