package com.sportsmanager.football;

import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.IPlayer;

import java.util.List;
import java.util.Random;

public class FootballCoach implements ICoach {

    private final String name;
    private final String speciality;
    private final Random random = new Random();

    public FootballCoach(String name, String speciality) {
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
        for (IPlayer player : squad) {
            if (player.isInjured() || !(player instanceof FootballPlayer fp)) {
                continue;
            }
            int boost = random.nextInt(3);
            switch (speciality) {
                case "Fitness" -> fp.setPhysical(Math.min(99, fp.getPhysical() + boost));
                case "Attacking" -> fp.setShooting(Math.min(99, fp.getShooting() + boost));
                case "Defending" -> fp.setDefending(Math.min(99, fp.getDefending() + boost));
                case "Goalkeeping" -> {
                    if (fp.getFootballPosition() == FootballPosition.GK) {
                        fp.setGoalkeeping(Math.min(99, fp.getGoalkeeping() + boost));
                    }
                }
                case "Tactics" -> fp.setPassing(Math.min(99, fp.getPassing() + boost));
                default -> fp.setPhysical(Math.min(99, fp.getPhysical() + boost));
            }
        }
    }
}
