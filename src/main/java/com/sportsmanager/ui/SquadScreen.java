package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class SquadScreen {

    private final Stage stage;
    private final DashboardScreen dashboard;
    private final GameSession session;

    public SquadScreen(Stage stage, DashboardScreen dashboard, GameSession session) {
        this.stage = stage;
        this.dashboard = dashboard;
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #16213e;");

        Label title = new Label(session.getPlayerTeam().getName() + " - Squad");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        TableView<PlayerRow> table = new TableView<>();
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
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, ageCol, posCol, ratingCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        List<IPlayer> squad = session.getPlayerTeam().getSquad();
        for (IPlayer p : squad) {
            String status = p.isInjured() ? "Injured (" + p.getInjuryGamesRemaining() + " games)" : "Fit";
            table.getItems().add(new PlayerRow(p.getName(), p.getAge(),
                    p.getPosition().name(), p.getOverallRating(), status));
        }

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
        backBtn.setOnAction(e -> stage.setScene(dashboard.getScene()));

        root.getChildren().addAll(title, table, backBtn);
        return new Scene(root, 750, 650);
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
