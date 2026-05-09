package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        VBox playerList = new VBox(8);
        playerList.setPadding(new Insets(8));
        List<CheckBox> boxes = new ArrayList<>();

        Label status = new Label("Selected: 0 / " + need);
        status.setStyle("-fx-text-fill: #a8a8a8;");

        for (IPlayer player : eligible) {
            CheckBox box = new CheckBox(player.getName() + " - " + player.getPosition()
                    + " (" + player.getOverallRating() + ")");
            box.setUserData(player);
            box.setStyle("-fx-text-fill: #eaeaea; -fx-font-size: 13px;");
            box.selectedProperty().addListener((obs, oldValue, selected) -> {
                int selectedCount = selectedCount(boxes);
                if (selected && selectedCount > need) {
                    box.setSelected(false);
                    alert("You can select only " + need + " starters.");
                    return;
                }
                status.setText("Selected: " + selectedCount(boxes) + " / " + need);
            });
            boxes.add(box);
            playerList.getChildren().add(box);
        }

        ScrollPane scroll = new ScrollPane(playerList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(340);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button confirm = new Button("Confirm");
        confirm.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 10 28;");
        confirm.setOnAction(e -> {
            List<IPlayer> picked = selectedPlayers(boxes);
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

        root.getChildren().addAll(title, help, scroll, status, confirm);
        return new Scene(root, 640, 560);
    }

    private int selectedCount(List<CheckBox> boxes) {
        return (int) boxes.stream().filter(CheckBox::isSelected).count();
    }

    private List<IPlayer> selectedPlayers(List<CheckBox> boxes) {
        List<IPlayer> selected = new ArrayList<>();
        for (CheckBox box : boxes) {
            if (box.isSelected()) {
                selected.add((IPlayer) box.getUserData());
            }
        }
        return selected;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
