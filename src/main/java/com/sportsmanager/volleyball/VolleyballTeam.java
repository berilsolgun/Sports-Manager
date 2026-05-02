package com.sportsmanager.volleyball;

import com.sportsmanager.domain.team.AbstractTeam;
import com.sportsmanager.domain.team.IPlayer;

import java.util.List;

public class VolleyballTeam extends AbstractTeam {

    public VolleyballTeam(String name, String logo) {
        super(name, logo);
    }

    @Override
    protected boolean validateStartingEleven(List<IPlayer> players) {
        if (players == null || players.size() != 6) {
            return false;
        }
        long setterCount = players.stream()
                .filter(p -> p instanceof VolleyballPlayer vp && vp.getVolleyballPosition() == VolleyballPosition.SETTER)
                .count();

        long liberoCount = players.stream()
                .filter(p -> p instanceof VolleyballPlayer vp && vp.getVolleyballPosition() == VolleyballPosition.LIBERO)
                .count();

        return setterCount >= 1 && liberoCount <= 1;
    }

    @Override
    public int getTeamRating() {
        List<IPlayer> startingSix = getStartingEleven();

        if (startingSix == null || startingSix.isEmpty()) {
            if (squad.isEmpty()) return 0;
            return squad.stream().mapToInt(IPlayer::getOverallRating).sum() / squad.size();
        }

        return startingSix.stream().mapToInt(IPlayer::getOverallRating).sum() / startingSix.size();
    }
}