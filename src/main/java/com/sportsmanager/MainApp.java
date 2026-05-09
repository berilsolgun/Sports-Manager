package com.sportsmanager;

import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportRegistry;
import com.sportsmanager.football.FootballSport;
import com.sportsmanager.ui.NewOrLoadScreen;
import com.sportsmanager.ui.SportSelectionScreen;
import com.sportsmanager.ui.TeamSelectionScreen;
import com.sportsmanager.volleyball.VolleyballSport;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static MainApp instance;

    private Stage primaryStage;
    private SportRegistry registry;

    @Override
    public void start(Stage stage) {
        MainApp.instance = this;
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
        primaryStage.setScene(buildNewOrLoadScene(sport));
    }

    private Scene buildNewOrLoadScene(Sport sport) {
        return new NewOrLoadScreen(primaryStage, sport,
                this::showSportSelection,
                () -> primaryStage.setScene(buildTeamSelectionScene(sport))).createScene();
    }

    private Scene buildTeamSelectionScene(Sport sport) {
        return new TeamSelectionScreen(primaryStage, sport,
                () -> primaryStage.setScene(buildNewOrLoadScene(sport))).createScene();
    }

    private SportRegistry buildRegistry() {
        SportRegistry reg = new SportRegistry();
        reg.register(new FootballSport());
        reg.register(new VolleyballSport());
        return reg;
    }

    /** Returns to sport selection on the primary stage (e.g. from season-end Main Menu). */
    public static void returnToSportSelection() {
        if (instance != null) {
            instance.showSportSelection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
