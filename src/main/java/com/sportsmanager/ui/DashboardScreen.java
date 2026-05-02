package com.sportsmanager.ui;

import com.sportsmanager.application.LeagueController;
import com.sportsmanager.application.MatchController;
import com.sportsmanager.application.WeekController;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class DashboardScreen {

    private final Stage stage;
    private final GameSession session;
    private final IMatchEngine engine;
    private final MatchController matchController = new MatchController();
    private final WeekController weekController = new WeekController();
    private final LeagueController leagueController = new LeagueController();

    private Label weekLabel;
    private Label teamLabel;
    private TableView<StandingRow> standingsTable;
    private TextArea logArea;

    public DashboardScreen(Stage stage, GameSession session, IMatchEngine engine) {
        this.stage = stage;
        this.session = session;
        this.engine = engine;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #16213e;");

        String sportName = session.getSport().getName();

        Label title = new Label(sportName + " Manager");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        teamLabel = new Label("Team: " + session.getPlayerTeam().getName());
        teamLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #eaeaea;");

        weekLabel = new Label("Week " + session.getCurrentWeek() + "  |  Season " + session.getSeason());
        weekLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a8a8a8;");

        HBox buttons = createButtonBar();

        standingsTable = createStandingsTable(sportName);
        VBox.setVgrow(standingsTable, Priority.ALWAYS);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setStyle("-fx-control-inner-background: #1a1a2e; -fx-text-fill: #eaeaea; -fx-font-family: monospace;");
        logArea.setPromptText("Match log will appear here...");

        root.getChildren().addAll(title, teamLabel, weekLabel, buttons, standingsTable, logArea);
        refreshStandings();

        return new Scene(root, 750, 650);
    }

    private HBox createButtonBar() {
        Button simulateBtn = styledButton("Simulate Week");
        simulateBtn.setOnAction(e -> onSimulateWeek());

        Button squadBtn = styledButton("View Squad");
        squadBtn.setOnAction(e -> showSquadScreen());

        HBox bar = new HBox(12, simulateBtn, squadBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void onSimulateWeek() {
        if (leagueController.isSeasonComplete(session)) {
            logArea.appendText("Season is over!\n");
            return;
        }

        int week = session.getCurrentWeek();
        matchController.playCurrentWeek(session, engine);

        StringBuilder sb = new StringBuilder();
        sb.append("--- Week ").append(week).append(" Results ---\n");

        session.getLeague().getWeekFixtures(week).forEach(f -> {
            f.getResult().ifPresent(r -> {
                String scoreLabel = session.getSport().getName().equals("Football") ? "Goals" : "Sets";
                sb.append(String.format("  %s %d - %d %s\n",
                        f.getHomeTeam().getName(), r.getHomeScore(),
                        r.getAwayScore(), f.getAwayTeam().getName()));
            });
        });

        logArea.appendText(sb.toString());
        weekController.advanceWeek(session);
        weekLabel.setText("Week " + session.getCurrentWeek() + "  |  Season " + session.getSeason());
        refreshStandings();
    }

    private void showSquadScreen() {
        SquadScreen squadScreen = new SquadScreen(stage, this, session);
        stage.setScene(squadScreen.createScene());
    }

    @SuppressWarnings("unchecked")
    private TableView<StandingRow> createStandingsTable(String sportName) {
        TableView<StandingRow> table = new TableView<>();
        table.setStyle("-fx-background-color: #1a1a2e;");

        String scoreHeader = sportName.equals("Football") ? "GF" : "SF";
        String againstHeader = sportName.equals("Football") ? "GA" : "SA";
        String diffHeader = sportName.equals("Football") ? "GD" : "SD";

        TableColumn<StandingRow, String> teamCol = col("Team", "teamName", 160);
        TableColumn<StandingRow, Integer> pCol = col("P", "played", 40);
        TableColumn<StandingRow, Integer> wCol = col("W", "won", 40);
        TableColumn<StandingRow, Integer> dCol = col("D", "drawn", 40);
        TableColumn<StandingRow, Integer> lCol = col("L", "lost", 40);
        TableColumn<StandingRow, Integer> gfCol = col(scoreHeader, "goalsFor", 45);
        TableColumn<StandingRow, Integer> gaCol = col(againstHeader, "goalsAgainst", 45);
        TableColumn<StandingRow, Integer> gdCol = col(diffHeader, "goalDifference", 45);
        TableColumn<StandingRow, Integer> ptsCol = col("Pts", "points", 45);

        table.getColumns().addAll(teamCol, pCol, wCol, dCol, lCol, gfCol, gaCol, gdCol, ptsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private <T> TableColumn<StandingRow, T> col(String header, String property, double width) {
        TableColumn<StandingRow, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        c.setSortable(false);
        return c;
    }

    private void refreshStandings() {
        List<StandingEntry> standings = leagueController.getStandings(session);
        standingsTable.getItems().clear();
        for (StandingEntry e : standings) {
            standingsTable.getItems().add(new StandingRow(
                    e.getTeam().getName(), e.getPlayed(), e.getWon(), e.getDrawn(), e.getLost(),
                    e.getGoalsFor(), e.getGoalsAgainst(), e.getPoints()));
        }
    }

    public Scene getScene() {
        return createScene();
    }

    private Button styledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: 14px; -fx-background-color: #e94560; "
                + "-fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; "
                + "-fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;"));
        return btn;
    }

    public static class StandingRow {
        private final String teamName;
        private final int played, won, drawn, lost, goalsFor, goalsAgainst, points;

        public StandingRow(String teamName, int played, int won, int drawn, int lost,
                           int goalsFor, int goalsAgainst, int points) {
            this.teamName = teamName;
            this.played = played;
            this.won = won;
            this.drawn = drawn;
            this.lost = lost;
            this.goalsFor = goalsFor;
            this.goalsAgainst = goalsAgainst;
            this.points = points;
        }

        public String getTeamName() { return teamName; }
        public int getPlayed() { return played; }
        public int getWon() { return won; }
        public int getDrawn() { return drawn; }
        public int getLost() { return lost; }
        public int getGoalsFor() { return goalsFor; }
        public int getGoalsAgainst() { return goalsAgainst; }
        public int getGoalDifference() { return goalsFor - goalsAgainst; }
        public int getPoints() { return points; }
    }
}
