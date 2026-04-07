package com.sportsmanager.football;

import com.sportsmanager.domain.team.AbstractTeam;
import com.sportsmanager.domain.team.IPlayer;

import java.util.List;

public class FootballTeam extends AbstractTeam {

    public FootballTeam(String name, String logo) {
        super(name, logo);
    }

    @Override
    protected boolean validateStartingEleven(List<IPlayer> players) {
        if (players == null || players.size() != 11) {
            return false;
        }

        long goalkeeperCount = players.stream()
                .filter(p -> p instanceof FootballPlayer fp && fp.getFootballPosition() == FootballPosition.GK)
                .count();

        return goalkeeperCount == 1;
    }

    @Override
    public int getTeamRating() {
        List<IPlayer> eleven = getStartingEleven();
        if (eleven == null || eleven.isEmpty()) {
            if (squad.isEmpty()) return 0;
            return squad.stream().mapToInt(IPlayer::getOverallRating).sum() / squad.size();
        }
        return eleven.stream().mapToInt(IPlayer::getOverallRating).sum() / eleven.size();
    }
}
