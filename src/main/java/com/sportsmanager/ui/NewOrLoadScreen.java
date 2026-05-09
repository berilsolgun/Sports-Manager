package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameRepository;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.sport.Sport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class NewOrLoadScreen {

    private final Stage stage;
    private final Sport sport;
    private final Runnable onBackToSportSelection;
    private final Runnable onStartNewGame;

    public NewOrLoadScreen(Stage stage, Sport sport,
                           Runnable onBackToSportSelection,
                           Runnable onStartNewGame) {
        this.stage = stage;
        this.sport = sport;
        this.onBackToSportSelection = onBackToSportSelection;
        this.onStartNewGame = onStartNewGame;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        String bgColor = sport.getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label title = new Label(sport.getName() + " Manager");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Choose an option");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #eaeaea;");

        Button newGameBtn = createButton("New Game");
        newGameBtn.setOnAction(e -> onStartNewGame.run());

        Button loadGameBtn = createButton("Load Game");
        loadGameBtn.setOnAction(e -> loadGame());

        Button backBtn = createButton("Back");
        backBtn.setStyle(backBtn.getStyle() + "; -fx-background-color: #444;");
        backBtn.setOnAction(e -> onBackToSportSelection.run());

        root.getChildren().addAll(title, subtitle, newGameBtn, loadGameBtn, backBtn);
        return new Scene(root, 600, 500);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 16px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12 40; -fx-min-width: 200;");
        return btn;
    }

    private void loadGame() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Game");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save Files (*.json)", "*.json"));
        File file = chooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        try {
            GameRepository repo = new JsonGameRepository(file.getAbsolutePath());
            repo.load().ifPresentOrElse(
                    loaded -> {
                        if (loaded.getSport() == null || !loaded.getSport().getName().equalsIgnoreCase(sport.getName())) {
                            showAlert("Wrong Sport", "This save file is for a different sport.");
                            return;
                        }
                        IMatchEngine engine = loaded.getSport().createFactory().createMatchEngine();
                        DashboardScreen dashboard = new DashboardScreen(stage, loaded, engine);
                        stage.setScene(dashboard.createScene());
                    },
                    () -> showAlert("Load Failed", "Could not load save file.")
            );
        } catch (Exception ex) {
            showAlert("Load Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
