package com.sportsmanager;

import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.sport.SportRegistry;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballSport;
import com.sportsmanager.ui.DashboardScreen;
import com.sportsmanager.ui.SportSelectionScreen;
import com.sportsmanager.volleyball.VolleyballSport;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainApp extends Application {

    private static final int TEAM_COUNT = 4;
    private Stage primaryStage;
    private SportRegistry registry;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.registry = buildRegistry();

        stage.setTitle("Sports Manager");
        showSportSelection();
        stage.show();
    }

    private void showSportSelection() {
        SportSelectionScreen selectionScreen = new SportSelectionScreen(registry, this::onSportChosen);
        primaryStage.setScene(selectionScreen.createScene());
    }

    private void onSportChosen(Sport sport) {
    com.sportsmanager.ui.NewOrLoadScreen newOrLoad =
            new com.sportsmanager.ui.NewOrLoadScreen(primaryStage, sport);
    primaryStage.setScene(newOrLoad.createScene());
}
    private SportRegistry buildRegistry() {
        SportRegistry reg = new SportRegistry();
        reg.register(new FootballSport());
        reg.register(new VolleyballSport());
        return reg;
    }

    private List<ITeam> createTeams(SportFactory factory, int count) {
        String[] allNames = factory.getTeamNames();
        List<String> nameList = new ArrayList<>(Arrays.asList(allNames));
        Collections.shuffle(nameList);

        List<ITeam> teams = new ArrayList<>();
        for (int i = 0; i < count && i < nameList.size(); i++) {
            String name = nameList.get(i);
            teams.add(factory.createTeam(name, name.toLowerCase().replace(" ", "_") + ".png"));
        }
        return teams;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
