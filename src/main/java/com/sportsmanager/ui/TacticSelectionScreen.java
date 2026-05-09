package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.Tactic;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class TacticSelectionScreen {

    private final Stage stage;
    private final GameSession session;
    private final Consumer<Tactic> onTacticChosen;
    private final Runnable onCancel;

    public TacticSelectionScreen(Stage stage, GameSession session, Consumer<Tactic> onTacticChosen) {
        this(stage, session, onTacticChosen, null);
    }

    public TacticSelectionScreen(Stage stage, GameSession session,
                                 Consumer<Tactic> onTacticChosen, Runnable onCancel) {
        this.stage = stage;
        this.session = session;
        this.onTacticChosen = onTacticChosen;
        this.onCancel = onCancel;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        String bgColor = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label title = new Label("Choose Your Tactic");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Select a tactic for " + session.getPlayerTeam().getName() + " this week");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #eaeaea;");

        root.getChildren().addAll(title, subtitle);

        Tactic current = session.getPlayerTeam().getCurrentTactic();
        if (current != null) {
            Label currentLbl = new Label("Current: " + current.getName());
            currentLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #a8a8a8;");
            root.getChildren().add(currentLbl);
        }

        List<Tactic> tactics = session.getSport().createFactory().generateTactics();
        for (Tactic t : tactics) {
            boolean isCurrent = current != null && current.getName().equals(t.getName());
            Button btn = createTacticButton(t, isCurrent);
            btn.setOnAction(e -> {
                session.getPlayerTeam().setTactic(t);
                onTacticChosen.accept(t);
            });
            root.getChildren().add(btn);
        }

        if (onCancel != null) {
            Button cancel = new Button("Cancel");
            cancel.setStyle("-fx-font-size: 13px; -fx-background-color: #444; -fx-text-fill: white; "
                    + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 25;");
            cancel.setOnAction(e -> onCancel.run());
            root.getChildren().add(cancel);
        }

        return new Scene(root, 600, 620);
    }

    private Button createTacticButton(Tactic t, boolean isCurrent) {
        String description = describeTactic(t);
        String baseStyle = "-fx-font-size: 14px; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-cursor: hand; -fx-padding: 12 30; -fx-min-width: 350; "
                + "-fx-text-alignment: center; -fx-alignment: center;";
        String label = (isCurrent ? "[Current] " : "") + t.getName() + "\n" + description;
        Button btn = new Button(label);
        String defaultBg = isCurrent ? "#1f7a4a" : "#0f3460";
        btn.setStyle("-fx-background-color: " + defaultBg + "; " + baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e94560; " + baseStyle));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + defaultBg + "; " + baseStyle));
        return btn;
    }

    private String describeTactic(Tactic t) {
        double atk = t.getAttackBonus();
        double def = t.getDefenseBonus();
        if (atk > def + 0.05) return "Attacking (+" + Math.round((atk - 1) * 100) + "% Attack)";
        if (def > atk + 0.05) return "Defensive (+" + Math.round((def - 1) * 100) + "% Defense)";
        return "Balanced";
    }
}
