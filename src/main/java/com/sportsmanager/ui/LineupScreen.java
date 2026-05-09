package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Pick starting lineup from the match-day squad (injured players excluded).
 */
public class LineupScreen {

    private final Stage stage;
    private final GameSession session;
    private final Runnable onContinue;

    public LineupScreen(Stage stage, GameSession session, Runnable onContinue) {
        this.stage = stage;
        this.session = session;
        this.onContinue = onContinue;
    }

    public Scene createScene() {
        ITeam team = session.getPlayerTeam();
        int need = session.getSport().getStartingLineupSize();

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_CENTER);
        String bg = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bg + ";");

        Label title = new Label("Starting lineup");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label help = new Label("Select exactly " + need + " starters for "
                + team.getName() + ". Only non-injured players from your match-day squad are listed.");
        help.setWrapText(true);
        help.setStyle("-fx-text-fill: #eaeaea;");

        List<IPlayer> eligible = team.getMatchDaySquad().stream()
                .filter(p -> !p.isInjured())
                .toList();

        ListView<IPlayer> squadView = new ListView<>(FXCollections.observableArrayList(eligible));
        squadView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        squadView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(IPlayer p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText(p.getName() + " — " + p.getPosition() + " (" + p.getOverallRating() + ")");
                }
            }
        });
        squadView.setPrefHeight(340);

        Label status = new Label("Selected: 0 / " + need);
        status.setStyle("-fx-text-fill: #a8a8a8;");
        squadView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<IPlayer>) c ->
                status.setText("Selected: " + squadView.getSelectionModel().getSelectedItems().size() + " / " + need));

        Button confirm = new Button("Confirm");
        confirm.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 10 28;");
        confirm.setOnAction(e -> {
            List<IPlayer> picked = new ArrayList<>(squadView.getSelectionModel().getSelectedItems());
            if (picked.size() != need) {
                alert("Please select exactly " + need + " players.");
                return;
            }
            Set<IPlayer> uniq = new LinkedHashSet<>(picked);
            if (uniq.size() != need) {
                alert("Each starter must be unique.");
                return;
            }
            try {
                team.setStartingEleven(picked);
                onContinue.run();
            } catch (IllegalArgumentException ex) {
                alert(ex.getMessage());
            }
        });

        root.getChildren().addAll(title, help, squadView, status, confirm);
        return new Scene(root, 640, 560);
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
