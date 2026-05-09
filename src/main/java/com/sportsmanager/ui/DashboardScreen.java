package com.sportsmanager.ui;

import com.sportsmanager.application.LeagueController;
import com.sportsmanager.application.MatchController;
import com.sportsmanager.application.WeekController;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameRepository;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.domain.simulation.IMatchEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class DashboardScreen {

    private final Stage stage;
    private final GameSession session;
    private final IMatchEngine engine;
    private final MatchController matchController = new MatchController();
    private final WeekController weekController = new WeekController();
    private final LeagueController leagueController = new LeagueController();

    private Scene scene;
    private Label weekLabel;
    private Label teamLabel;
    private ImageView logoView;
    private TableView<StandingRow> standingsTable;
    private TextArea logArea;

    public DashboardScreen(Stage stage, GameSession session, IMatchEngine engine) {
        this.stage = stage;
        this.session = session;
        this.engine = engine;
    }

    public Scene createScene() {
        if (scene == null) {
            buildScene();
        }
        refreshUiState();
        return scene;
    }

    public Scene getScene() {
        return createScene();
    }

    private void buildScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        String bgColor = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        String sportName = session.getSport().getName();

        Label title = new Label(sportName + " Manager");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        logoView = new ImageView();
        logoView.setFitHeight(56);
        logoView.setFitWidth(56);
        logoView.setPreserveRatio(true);

        teamLabel = new Label();
        teamLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #eaeaea;");

        weekLabel = new Label();
        weekLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a8a8a8;");

        HBox top = new HBox(12, logoView, title);
        top.setAlignment(Pos.CENTER_LEFT);

        HBox buttons = createButtonBar();

        standingsTable = createStandingsTable(sportName);
        VBox.setVgrow(standingsTable, Priority.ALWAYS);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setStyle("-fx-control-inner-background: #1a1a2e; -fx-text-fill: #eaeaea; -fx-font-family: monospace;");
        logArea.setPromptText("Match log will appear here...");

        root.getChildren().addAll(top, teamLabel, weekLabel, buttons, standingsTable, logArea);
        scene = new Scene(root, 780, 680);
    }

    private void refreshUiState() {
        teamLabel.setText("Team: " + session.getPlayerTeam().getName());
        weekLabel.setText("Week " + session.getCurrentWeek() + "  |  Season " + session.getSeason());
        var img = UiLogo.loadTeamLogo(session.getSport(), session.getPlayerTeam().getLogo());
        logoView.setImage(img);
        refreshStandings();
    }

    private HBox createButtonBar() {
        Button simulateBtn = styledButton("Simulate Week");
        simulateBtn.setOnAction(e -> onSimulateWeek());

        Button squadBtn = styledButton("View Squad");
        squadBtn.setOnAction(e -> showSquadScreen());

        Button scheduleBtn = styledButton("Schedule");
        scheduleBtn.setOnAction(e -> stage.setScene(
                new ScheduleScreen(stage, session, () -> stage.setScene(createScene())).createScene()));

        Button saveBtn = styledButton("Save Game");
        saveBtn.setOnAction(e -> onSaveGame());

        HBox bar = new HBox(12, simulateBtn, squadBtn, scheduleBtn, saveBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void onSaveGame() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Game");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save Files (*.json)", "*.json"));
        chooser.setInitialFileName("savegame.json");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                GameRepository repo = new JsonGameRepository(file.getAbsolutePath());
                repo.save(session);
                logArea.appendText("Game saved to " + file.getName() + "\n");
            } catch (Exception ex) {
                logArea.appendText("Save failed: " + ex.getMessage() + "\n");
            }
        }
    }

    private void onSimulateWeek() {
        if (leagueController.isSeasonComplete(session)) {
            stage.setScene(new SeasonEndScreen(stage, session, engine).createScene());
            return;
        }

        TacticSelectionScreen tacticScreen = new TacticSelectionScreen(stage, session, chosenTactic -> {
            logArea.appendText("Tactic chosen: " + chosenTactic.getName() + "\n");
            stage.setScene(new LineupScreen(stage, session, this::runWeekAfterLineup).createScene());
        });
        stage.setScene(tacticScreen.createScene());
    }

    private void runWeekAfterLineup() {
        int week = session.getCurrentWeek();
        matchController.playCurrentWeek(session, engine, false);

        var pending = matchController.findPlayerFixtureThisWeek(session, week);
        if (pending.isPresent()) {
            stage.setScene(new LiveMatchScreen(stage, session, engine, pending.get(),
                    () -> finishWeekAfterMatches(week)).createScene());
        } else {
            finishWeekAfterMatches(week);
        }
    }

    private void finishWeekAfterMatches(int weekPlayed) {
        matchController.applyGameWeekInjuryRecovery(session);
        weekController.advanceWeek(session);
        logArea.appendText("--- Week " + weekPlayed + " finished. Injury recovery applied. Training complete. ---\n");
        session.getLeague().getWeekFixtures(weekPlayed).forEach(f ->
                f.getResult().ifPresent(r -> logArea.appendText(String.format("  %s %d - %d %s%n",
                        f.getHomeTeam().getName(), r.getHomeScore(), r.getAwayScore(), f.getAwayTeam().getName()))));

        if (leagueController.isSeasonComplete(session)) {
            stage.setScene(new SeasonEndScreen(stage, session, engine).createScene());
            return;
        }

        stage.setScene(createScene());
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
        private final int played;
        private final int won;
        private final int drawn;
        private final int lost;
        private final int goalsFor;
        private final int goalsAgainst;
        private final int points;

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

        public String getTeamName() {
            return teamName;
        }

        public int getPlayed() {
            return played;
        }

        public int getWon() {
            return won;
        }

        public int getDrawn() {
            return drawn;
        }

        public int getLost() {
            return lost;
        }

        public int getGoalsFor() {
            return goalsFor;
        }

        public int getGoalsAgainst() {
            return goalsAgainst;
        }

        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }

        public int getPoints() {
            return points;
        }
    }
}
