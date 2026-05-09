package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.team.ITeam;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectionScreen {

    private final Stage stage;
    private final Sport sport;
    private final List<ITeam> teams;
    private final Runnable navigateBackToNewOrLoad;

    public TeamSelectionScreen(Stage stage, Sport sport, Runnable navigateBackToNewOrLoad) {
        this.stage = stage;
        this.sport = sport;
        this.navigateBackToNewOrLoad = navigateBackToNewOrLoad;
        this.teams = generateTeams();
    }

    private List<ITeam> generateTeams() {
        SportFactory factory = sport.createFactory();
        String[] teamNames = factory.getTeamNames();
        List<ITeam> created = new ArrayList<>();
        int teamCount = Math.min(4, teamNames.length);
        for (int i = 0; i < teamCount; i++) {
            created.add(factory.createTeam(teamNames[i],
                    teamNames[i].toLowerCase().replace(" ", "_") + ".png"));
        }
        return created;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        String bgColor = sport.getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label title = new Label("Choose Your Team");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Pick the team you want to manage");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #eaeaea;");

        VBox teamList = new VBox(10);
        teamList.setAlignment(Pos.CENTER);

        for (ITeam team : teams) {
            int rating = team.getSquad().stream()
                    .mapToInt(p -> p.getOverallRating())
                    .sum() / Math.max(1, team.getSquad().size());

            Button btn = createTeamButton(team, rating);
            btn.setOnAction(e -> startGameWith(team));
            teamList.getChildren().add(btn);
        }

        ScrollPane scroll = new ScrollPane(teamList);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-font-size: 13px; -fx-background-color: #444; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 25;");
        backBtn.setOnAction(e -> navigateBackToNewOrLoad.run());

        root.getChildren().addAll(title, subtitle, scroll, backBtn);
        return new Scene(root, 600, 600);
    }

    private Button createTeamButton(ITeam team, int rating) {
        String baseStyle = "-fx-font-size: 14px; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-cursor: hand; -fx-padding: 14 30; -fx-min-width: 380; "
                + "-fx-text-alignment: center; -fx-alignment: center;";
        Button btn = new Button();
        ImageView logo = new ImageView();
        var img = UiLogo.loadTeamLogo(sport, team.getLogo());
        if (img != null) {
            logo.setImage(img);
            logo.setFitHeight(36);
            logo.setFitWidth(36);
            logo.setPreserveRatio(true);
        }
        Label text = new Label(team.getName() + "\nAvg Rating: " + rating);
        text.setStyle("-fx-text-fill: white;");
        HBox row = new HBox(12, logo, text);
        row.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(row);
        btn.setStyle("-fx-background-color: #0f3460; " + baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e94560; " + baseStyle));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #0f3460; " + baseStyle));
        return btn;
    }

    private void startGameWith(ITeam chosenTeam) {
        SportFactory factory = sport.createFactory();
        GameSession session = new GameSession();
        session.setSport(sport);
        session.setLeague(factory.createLeague(sport.getName() + " League", teams));
        session.setPlayerTeam(chosenTeam);
        session.setCurrentWeek(1);
        session.setSeason(2026);

        IMatchEngine engine = factory.createMatchEngine();

        stage.setScene(new TacticSelectionScreen(stage, session, tac ->
                stage.setScene(new LineupScreen(stage, session, () -> {
                    DashboardScreen dashboard = new DashboardScreen(stage, session, engine);
                    stage.setScene(dashboard.createScene());
                }).createScene())).createScene());
    }
}
