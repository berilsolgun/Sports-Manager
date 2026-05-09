package com.sportsmanager.ui;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.session.GameSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;

/** Past results and upcoming fixtures by week. */
public class ScheduleScreen {

    private final Stage stage;
    private final GameSession session;
    private final Runnable onBack;

    public ScheduleScreen(Stage stage, GameSession session, Runnable onBack) {
        this.stage = stage;
        this.session = session;
        this.onBack = onBack;
    }

    public Scene createScene() {
        String bg = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + bg + ";");

        Label title = new Label("Schedule");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        List<IFixture> fixtures = session.getLeague().getFixtures().stream()
                .sorted(Comparator.comparingInt(IFixture::getWeek))
                .toList();

        TabPane tabs = new TabPane();
        Tab past = new Tab("Results");
        past.setClosable(false);
        past.setContent(scrollText(formatPlayed(fixtures)));

        Tab upcoming = new Tab("Upcoming");
        upcoming.setClosable(false);
        upcoming.setContent(scrollText(formatUpcoming(fixtures)));

        tabs.getTabs().addAll(past, upcoming);

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        back.setOnAction(e -> onBack.run());

        VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().addAll(title, tabs, back);
        return new Scene(root, 700, 560);
    }

    private ScrollPane scrollText(String body) {
        Label lbl = new Label(body);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: #eaeaea; -fx-font-family: monospace;");
        ScrollPane sp = new ScrollPane(lbl);
        sp.setFitToWidth(true);
        return sp;
    }

    private String formatPlayed(List<IFixture> fixtures) {
        StringBuilder sb = new StringBuilder();
        for (IFixture f : fixtures) {
            if (!f.isPlayed()) {
                continue;
            }
            f.getResult().ifPresent(r -> sb.append(String.format("W%d  %s %d-%d %s  (pts %d-%d)%n",
                    f.getWeek(),
                    f.getHomeTeam().getName(), r.getHomeScore(), r.getAwayScore(), f.getAwayTeam().getName(),
                    r.getHomePoints(), r.getAwayPoints())));
        }
        if (sb.length() == 0) {
            return "No results yet.";
        }
        return sb.toString();
    }

    private String formatUpcoming(List<IFixture> fixtures) {
        StringBuilder sb = new StringBuilder();
        for (IFixture f : fixtures) {
            if (f.isPlayed()) {
                continue;
            }
            sb.append(String.format("W%d  %s vs %s%n",
                    f.getWeek(), f.getHomeTeam().getName(), f.getAwayTeam().getName()));
        }
        if (sb.length() == 0) {
            return "Season complete — no upcoming fixtures.";
        }
        return sb.toString();
    }
}
