package com.sportsmanager.domain.team;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTeam implements ITeam {
    protected String name;
    protected String logo;
    protected List<IPlayer> squad = new ArrayList<>();
    protected List<IPlayer> startingEleven = new ArrayList<>();
    protected List<ICoach> coaches = new ArrayList<>();
    protected Tactic currentTactic;

    public AbstractTeam(String name, String logo) {
        this.name = name;
        this.logo = logo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLogo() {
        return logo;
    }

    @Override
    public List<IPlayer> getSquad() {
        return squad;
    }

    @Override
    public List<ICoach> getCoaches() {
        return coaches;
    }

    @Override
    public Tactic getCurrentTactic() {
        return currentTactic;
    }

    @Override
    public void setTactic(Tactic tactic) {
        this.currentTactic = tactic;
    }

    @Override
    public void addPlayer(IPlayer player) {
        squad.add(player);
    }

    @Override
    public void removePlayer(IPlayer player){
        squad.remove(player);
    }

    @Override
    public List<IPlayer> getStartingEleven() {
        return startingEleven;
    }

    @Override
    public void setStartingEleven(List<IPlayer> players) {
        if (validateStartingEleven(players)){
            this.startingEleven = players;
        } else {
            throw new IllegalArgumentException("This team is violating the rules of the sport!");
        }
    }

    protected abstract boolean validateStartingEleven(List<IPlayer> players);
    public abstract int getTeamRating();

}
