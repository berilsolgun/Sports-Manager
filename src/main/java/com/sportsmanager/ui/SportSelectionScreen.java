package com.sportsmanager.ui;

import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class SportSelectionScreen {

    private final SportRegistry registry;
    private final Consumer<Sport> onSportSelected;

    public SportSelectionScreen(SportRegistry registry, Consumer<Sport> onSportSelected) {
        this.registry = registry;
        this.onSportSelected = onSportSelected;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("Sports Manager");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Choose your sport");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #eaeaea;");

        root.getChildren().addAll(title, subtitle);

        for (Sport sport : registry.getAll()) {
            Button btn = new Button(sport.getName());
            btn.setPrefWidth(250);
            btn.setPrefHeight(50);
            btn.setStyle("-fx-font-size: 16px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                    + "-fx-background-radius: 8; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: 16px; -fx-background-color: #e94560; "
                    + "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: 16px; -fx-background-color: #0f3460; "
                    + "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
            btn.setOnAction(e -> onSportSelected.accept(sport));
            root.getChildren().add(btn);
        }

        return new Scene(root, 600, 500);
    }
}
