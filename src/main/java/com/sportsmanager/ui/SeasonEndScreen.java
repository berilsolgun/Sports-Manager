package com.sportsmanager.ui;

import com.sportsmanager.MainApp;
import com.sportsmanager.application.LeagueController;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * End-of-season summary with standings, optional championship tally for the player, and rollover via {@link LeagueController#prepareNewSeason}.
 */
public class SeasonEndScreen {

    private final Stage stage;
    private final GameSession session;
    private final IMatchEngine engine;
    private final LeagueController leagueController = new LeagueController();

    private boolean championshipCountedForThisView;

    public SeasonEndScreen(Stage stage, GameSession session, IMatchEngine engine) {
        this.stage = stage;
        this.session = session;
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        String bgColor = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        List<StandingEntry> standings = session.getLeague().getStandings();
        StandingEntry champion = standings.get(0);
        boolean isPlayerChampion = champion.getTeam() == session.getPlayerTeam();
        if (isPlayerChampion && !championshipCountedForThisView) {
            session.incrementChampionshipCount();
            championshipCountedForThisView = true;
        }

        Label resultLabel = new Label(isPlayerChampion ? "CHAMPIONS!" : "Season Over");
        resultLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: "
                + (isPlayerChampion ? "#ffd700" : "#e94560") + ";");

        Label subtitle;
        if (isPlayerChampion) {
            subtitle = new Label("Congratulations! " + session.getPlayerTeam().getName()
                    + " has won the " + session.getSport().getName() + " League!");
        } else {
            int playerPos = findPlayerPosition(standings);
            subtitle = new Label(session.getPlayerTeam().getName() + " finished " + ordinal(playerPos)
                    + ". Champion: " + champion.getTeam().getName());
        }
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #eaeaea;");

        Label trophyLabel = new Label("Total Championships: " + session.getChampionshipCount());
        trophyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");

        Label seasonInfo = new Label("Season " + session.getSeason() + " - Final standings");
        seasonInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #b0b0b0;");

        TableView<StandingRow> table = new TableView<>();
        table.setStyle("-fx-background-color: #1a1a2e;");
        table.setMaxHeight(300);

        TableColumn<StandingRow, Integer> rankCol = col("#", "rank", 40);
        TableColumn<StandingRow, String> teamCol = col("Team", "teamName", 200);
        TableColumn<StandingRow, Integer> pCol = col("P", "played", 50);
        TableColumn<StandingRow, Integer> wCol = col("W", "won", 50);
        TableColumn<StandingRow, Integer> dCol = col("D", "drawn", 50);
        TableColumn<StandingRow, Integer> lCol = col("L", "lost", 50);
        TableColumn<StandingRow, Integer> ptsCol = col("Pts", "points", 60);

        table.getColumns().addAll(rankCol, teamCol, pCol, wCol, dCol, lCol, ptsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        for (int i = 0; i < standings.size(); i++) {
            StandingEntry s = standings.get(i);
            table.getItems().add(new StandingRow(i + 1, s.getTeam().getName(),
                    s.getPlayed(), s.getWon(), s.getDrawn(), s.getLost(), s.getPoints()));
        }

        Button continueBtn = createButton("Continue to Next Season");
        continueBtn.setOnAction(e -> {
            leagueController.prepareNewSeason(session);
            DashboardScreen dashboard = new DashboardScreen(stage, session, engine);
            stage.setScene(dashboard.createScene());
        });

        Button mainMenuBtn = createButton("Main Menu");
        mainMenuBtn.setStyle(mainMenuBtn.getStyle() + "; -fx-background-color: #444;");
        mainMenuBtn.setOnAction(e -> MainApp.returnToSportSelection());

        HBox buttonRow = new HBox(15, continueBtn, mainMenuBtn);
        buttonRow.setAlignment(Pos.CENTER);

        root.getChildren().addAll(resultLabel, subtitle, trophyLabel, seasonInfo, table, buttonRow);
        return new Scene(root, 700, 700);
    }

    private int findPlayerPosition(List<StandingEntry> standings) {
        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i).getTeam() == session.getPlayerTeam()) {
                return i + 1;
            }
        }
        return 0;
    }

    private String ordinal(int n) {
        if (n == 1) {
            return "1st";
        }
        if (n == 2) {
            return "2nd";
        }
        if (n == 3) {
            return "3rd";
        }
        return n + "th";
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 25;");
        return btn;
    }

    private <T> TableColumn<StandingRow, T> col(String header, String property, double width) {
        TableColumn<StandingRow, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    public static class StandingRow {
        private final int rank;
        private final String teamName;
        private final int played;
        private final int won;
        private final int drawn;
        private final int lost;
        private final int points;

        public StandingRow(int rank, String teamName, int played, int won, int drawn, int lost, int points) {
            this.rank = rank;
            this.teamName = teamName;
            this.played = played;
            this.won = won;
            this.drawn = drawn;
            this.lost = lost;
            this.points = points;
        }

        public int getRank() {
            return rank;
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

        public int getPoints() {
            return points;
        }
    }
}

