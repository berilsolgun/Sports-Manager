package com.sportsmanager.domain.team;

import java.util.List;

public interface ICoach {
    String getName();
    String getSpeciality();
    void conductTraining(List<IPlayer> squad);


}
