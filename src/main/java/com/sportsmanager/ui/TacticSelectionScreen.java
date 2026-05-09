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

    public TacticSelectionScreen(Stage stage, GameSession session, Consumer<Tactic> onTacticChosen) {
        this.stage = stage;
        this.session = session;
        this.onTacticChosen = onTacticChosen;
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

        List<Tactic> tactics = session.getSport().createFactory().generateTactics();
        for (Tactic t : tactics) {
            Button btn = createTacticButton(t);
            btn.setOnAction(e -> {
                session.getPlayerTeam().setTactic(t);
                onTacticChosen.accept(t);
            });
            root.getChildren().add(btn);
        }

        return new Scene(root, 600, 600);
    }

   private Button createTacticButton(Tactic t) {
    String description = describeTactic(t);
    String baseStyle = "-fx-font-size: 14px; -fx-text-fill: white; -fx-background-radius: 8; "
            + "-fx-cursor: hand; -fx-padding: 12 30; -fx-min-width: 350; "
            + "-fx-text-alignment: center; -fx-alignment: center;";
    Button btn = new Button(t.getName() + "\n" + description);
    btn.setStyle("-fx-background-color: #0f3460; " + baseStyle);
    btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e94560; " + baseStyle));
    btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #0f3460; " + baseStyle));
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