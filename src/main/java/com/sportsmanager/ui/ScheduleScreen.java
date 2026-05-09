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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;

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
        tabs.setStyle("-fx-background-color: #1a1a2e;");

        Tab past = new Tab("Results");
        past.setClosable(false);
        past.setContent(buildPlayedView(fixtures));

        Tab upcoming = new Tab("Upcoming");
        upcoming.setClosable(false);
        upcoming.setContent(buildUpcomingView(fixtures));

        tabs.getTabs().addAll(past, upcoming);

        Button back = new Button("Back");
        back.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 25;");
        back.setOnAction(e -> onBack.run());

        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.getChildren().addAll(title, tabs, back);
        return new Scene(root, 720, 600);
    }

    private ScrollPane buildPlayedView(List<IFixture> fixtures) {
        VBox content = new VBox(6);
        content.setPadding(new Insets(14));
        content.setStyle("-fx-background-color: #1a1a2e;");

        int currentWeek = -1;
        boolean any = false;
        for (IFixture f : fixtures) {
            if (!f.isPlayed()) {
                continue;
            }
            any = true;
            if (f.getWeek() != currentWeek) {
                currentWeek = f.getWeek();
                content.getChildren().add(weekHeader("Week " + currentWeek));
            }
            f.getResult().ifPresent(r -> {
                String line = String.format("  %s  %d - %d  %s    (pts %d-%d)",
                        f.getHomeTeam().getName(), r.getHomeScore(), r.getAwayScore(),
                        f.getAwayTeam().getName(), r.getHomePoints(), r.getAwayPoints());
                content.getChildren().add(rowLabel(line));
            });
        }
        if (!any) {
            content.getChildren().add(rowLabel("No results yet — play some matches first."));
        }
        return wrapInScroll(content);
    }

    private ScrollPane buildUpcomingView(List<IFixture> fixtures) {
        VBox content = new VBox(6);
        content.setPadding(new Insets(14));
        content.setStyle("-fx-background-color: #1a1a2e;");

        int currentWeek = -1;
        boolean any = false;
        for (IFixture f : fixtures) {
            if (f.isPlayed()) {
                continue;
            }
            any = true;
            if (f.getWeek() != currentWeek) {
                currentWeek = f.getWeek();
                content.getChildren().add(weekHeader("Week " + currentWeek));
            }
            String line = String.format("  %s  vs  %s",
                    f.getHomeTeam().getName(), f.getAwayTeam().getName());
            content.getChildren().add(rowLabel(line));
        }
        if (!any) {
            content.getChildren().add(rowLabel("Season complete — no upcoming fixtures."));
        }
        return wrapInScroll(content);
    }

    private Label weekHeader(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 0 2 0;");
        return l;
    }

    private Label rowLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #eaeaea; -fx-font-family: monospace; -fx-font-size: 13px;");
        return l;
    }

    private ScrollPane wrapInScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        return sp;
    }
}
