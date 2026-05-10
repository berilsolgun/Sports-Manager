package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class SquadScreen {

    private final Stage stage;
    private final DashboardScreen dashboard;
    private final GameSession session;

    private TableView<PlayerRow> table;
    private Label title;
    private ListView<ICoach> coachList;

    public SquadScreen(Stage stage, DashboardScreen dashboard, GameSession session) {
        this.stage = stage;
        this.dashboard = dashboard;
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public Scene createScene() {
        coachList = new ListView<>();
        coachList.setPrefHeight(100);

        VBox trainingBox = getVBox();


        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        String bgColor = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        title = new Label(session.getPlayerTeam().getName() + " - Squad");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        // Team selector dropdown
        Label selectorLabel = new Label("View Team:");
        selectorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #eaeaea;");

        ComboBox<String> teamSelector = new ComboBox<>();
        List<ITeam> allTeams = session.getLeague().getTeams();
        for (ITeam t : allTeams) {
            teamSelector.getItems().add(t.getName());
        }
        teamSelector.setValue(session.getPlayerTeam().getName());
        teamSelector.setStyle("-fx-font-size: 14px; -fx-pref-width: 200;");

        HBox selectorRow = new HBox(10, selectorLabel, teamSelector);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        // Table setup
        table = new TableView<>();
        table.setStyle("-fx-background-color: #1a1a2e;");

        TableColumn<PlayerRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);

        TableColumn<PlayerRow, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(50);

        TableColumn<PlayerRow, String> posCol = new TableColumn<>("Position");
        posCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        posCol.setPrefWidth(100);

        TableColumn<PlayerRow, Integer> ratingCol = new TableColumn<>("OVR");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(60);

        TableColumn<PlayerRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, ageCol, posCol, ratingCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Initial load
        loadTeam(session.getPlayerTeam());

        // Update on team selection
        teamSelector.setOnAction(e -> {
            String selected = teamSelector.getValue();
            for (ITeam t : allTeams) {
                if (t.getName().equals(selected)) {
                    loadTeam(t);
                    break;
                }
            }
        });

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
        backBtn.setOnAction(e -> stage.setScene(dashboard.getScene()));

        root.getChildren().addAll(title, selectorRow, trainingBox, table, backBtn);
        return new Scene(root, 750, 650);
    }

    private VBox getVBox() {
        Button trainBtn = new Button("Run Weekly Training");
        trainBtn.setOnAction(e -> {
            for (ITeam t : session.getLeague().getTeams()) {
                for (ICoach c : t.getCoaches()) {
                    c.conductTraining(t.getSquad());
                }
            }
            loadTeam(session.getPlayerTeam());
        });

        Label coachLabel = new Label("Team Coaches:");
        coachLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        return new VBox(5, coachLabel, coachList, trainBtn);
    }

    private void loadTeam(ITeam team) {
        title.setText(team.getName() + " - Squad");
        table.setItems(FXCollections.observableArrayList());
        coachList.setItems(FXCollections.observableArrayList(team.getCoaches()));
        for (IPlayer p : team.getSquad()) {
            String status = p.isInjured() ? "Injured (" + p.getInjuryGamesRemaining() + " games)" : "Fit";
            table.getItems().add(new PlayerRow(p.getName(), p.getAge(),
                    p.getPosition().name(), p.getOverallRating(), status));
        }
    }

    public static class PlayerRow {
        private final String name;
        private final int age;
        private final String position;
        private final int rating;
        private final String status;

        public PlayerRow(String name, int age, String position, int rating, String status) {
            this.name = name;
            this.age = age;
            this.position = position;
            this.rating = rating;
            this.status = status;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
        public String getPosition() { return position; }
        public int getRating() { return rating; }
        public String getStatus() { return status; }
    }
}