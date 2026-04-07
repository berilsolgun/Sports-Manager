package com.sportsmanager.domain.team;

import java.util.Map;

public abstract class AbstractPlayer implements IPlayer {
    protected String name;
    protected int age;
    protected Position position;
    protected int injuryGamesRemaining = 0;

    public AbstractPlayer(String name, int age, Position position) {
        this.name = name;
        this.age = age;
        this.position = position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isInjured(){
        return injuryGamesRemaining > 0;
    }

    @Override
    public void injure(int games){
        this.injuryGamesRemaining = games;
    }

    @Override
    public void recoverOneGame(){
        if (injuryGamesRemaining > 0) {
            injuryGamesRemaining--;
        }
    }

    public abstract int getOverallRating();
    public abstract Map<String, Integer> getAttributes();

}

