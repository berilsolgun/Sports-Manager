package com.sportsmanager.volleyball;

import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.IPlayer;

import java.util.List;
import java.util.Random;

public class VolleyballCoach implements ICoach {
    private final String name;
    private final String speciality;
    private final Random random = new Random();

    public VolleyballCoach(String name, String speciality) {
        this.name = name;
        this.speciality = speciality;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSpeciality() {
        return speciality;
    }

    @Override
    public void conductTraining(List<IPlayer> squad) {
        for(IPlayer player : squad){
            if (player.isInjured() || !(player instanceof VolleyballPlayer vp)){
                continue;
            }
            int boost = random.nextInt(3);

            switch (speciality) {
                case "Fitness" -> vp.setPhysical(Math.min(99, vp.getPhysical() + boost));
                case "Attacking" -> {
                    vp.setSpiking(Math.min(99, vp.getSpiking() + boost));
                    vp.setServing(Math.min(99, vp.getServing() + boost));
                }
                case "Defending" -> {
                    vp.setDigging(Math.min(99, vp.getDigging() + boost));
                    vp.setBlocking(Math.min(99, vp.getBlocking() + boost));
                    vp.setReceiving(Math.min(99, vp.getReceiving() + boost));
                }
                case "Playmaking" -> {
                    if (vp.getVolleyballPosition() == VolleyballPosition.SETTER) {
                        vp.setSetting(Math.min(99, vp.getSetting() + (boost + 1)));
                    } else {
                        vp.setSetting(Math.min(99, vp.getSetting() + boost));
                    }
                }
                case "Service" -> vp.setServing(Math.min(99, vp.getServing() + boost));
                default -> vp.setPhysical(Math.min(99, vp.getPhysical() + boost));
            }
        }

    }
}
