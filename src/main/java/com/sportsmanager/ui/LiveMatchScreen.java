package com.sportsmanager.ui;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.simulation.PhaseResult;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.domain.team.Tactic;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class LiveMatchScreen {

    private final Stage stage;
    private final GameSession session;
    private final IMatchEngine engine;
    private final IFixture fixture;
    private final Runnable onFinished;

    private int cumulativeHome;
    private int cumulativeAway;
    private boolean begun;
    private boolean matchRecorded;

    public LiveMatchScreen(Stage stage, GameSession session, IMatchEngine engine, IFixture fixture, Runnable onFinished) {
        this.stage = stage;
        this.session = session;
        this.engine = engine;
        this.fixture = fixture;
        this.onFinished = onFinished;
    }

    public Scene createScene() {
        ITeam home = fixture.getHomeTeam();
        ITeam away = fixture.getAwayTeam();
        ITeam player = session.getPlayerTeam();

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setAlignment(Pos.TOP_CENTER);
        String bg = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";
        root.setStyle("-fx-background-color: " + bg + ";");

        Label title = new Label(home.getName() + " vs " + away.getName());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label score = new Label("Score: 0 - 0");
        score.setStyle("-fx-font-size: 16px; -fx-text-fill: #eaeaea;");

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(14);
        log.setStyle("-fx-control-inner-background: #1a1a2e; -fx-text-fill: #eaeaea; -fx-font-family: monospace;");

        List<Tactic> tactics = session.getSport().createFactory().generateTactics();
        ComboBox<Tactic> tacticBox = new ComboBox<>(FXCollections.observableArrayList(tactics));
        configureTacticCombo(tacticBox);
        tacticBox.setPromptText("Change tactic (your team)");
        tacticBox.setOnAction(e -> {
            Tactic t = tacticBox.getSelectionModel().getSelectedItem();
            if (t != null && player != null) {
                player.setTactic(t);
                log.appendText("Tactic set: " + t.getName() + "\n");
            }
        });

        ComboBox<IPlayer> outSub = new ComboBox<>();
        ComboBox<IPlayer> inSub = new ComboBox<>();
        configurePlayerCombo(outSub);
        configurePlayerCombo(inSub);
        refreshSubCombos(player, outSub, inSub);

        Button subBtn = new Button("Substitute");
        subBtn.setOnAction(e -> applySubstitution(player, outSub.getValue(), inSub.getValue(), log, outSub, inSub));

        HBox subRow = new HBox(10, new Label("Out:"), outSub, new Label("In:"), inSub, subBtn);
        subRow.setAlignment(Pos.CENTER_LEFT);

        Button nextBtn = new Button("Play next phase");
        nextBtn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 10 22;");

        Button simEndBtn = new Button("Sim to end");
        simEndBtn.setStyle("-fx-background-color: #1f7a4a; -fx-text-fill: white; -fx-padding: 10 22;");

        Button continueBtn = new Button("Continue");
        continueBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-padding: 10 22; -fx-font-weight: bold;");
        continueBtn.setVisible(false);
        continueBtn.setManaged(false);
        continueBtn.setOnAction(e -> {
            if (matchRecorded) {
                onFinished.run();
            }
        });

        nextBtn.setOnAction(e -> {
            if (matchRecorded) {
                return;
            }
            if (!begun) {
                engine.beginMatch(home, away);
                begun = true;
                log.appendText("Kickoff / first set.\n");
            }
            if (engine.isMatchComplete()) {
                finalizeMatch(log, score, nextBtn, simEndBtn, continueBtn);
                return;
            }
            PhaseResult pr = engine.playNextPhase();
            cumulativeHome += pr.homeScore;
            cumulativeAway += pr.awayScore;
            score.setText(formatScoreLabel(home, away, cumulativeHome, cumulativeAway));
            log.appendText(String.format("Phase complete: +%d / +%d (running %d - %d)\n",
                    pr.homeScore, pr.awayScore, cumulativeHome, cumulativeAway));
            appendPhaseEvents(pr, log);
            refreshSubCombos(player, outSub, inSub);

            if (engine.isMatchComplete()) {
                finalizeMatch(log, score, nextBtn, simEndBtn, continueBtn);
            }
        });

        simEndBtn.setOnAction(e -> {
            if (matchRecorded) {
                return;
            }
            if (!begun) {
                engine.beginMatch(home, away);
                begun = true;
                log.appendText("Kickoff / first set.\n");
            }
            while (!engine.isMatchComplete()) {
                PhaseResult pr = engine.playNextPhase();
                cumulativeHome += pr.homeScore;
                cumulativeAway += pr.awayScore;
                log.appendText(String.format("Phase complete: +%d / +%d (running %d - %d)\n",
                        pr.homeScore, pr.awayScore, cumulativeHome, cumulativeAway));
                appendPhaseEvents(pr, log);
            }
            score.setText(formatScoreLabel(home, away, cumulativeHome, cumulativeAway));
            refreshSubCombos(player, outSub, inSub);
            finalizeMatch(log, score, nextBtn, simEndBtn, continueBtn);
        });

        HBox actionRow = new HBox(10, nextBtn, simEndBtn, continueBtn);
        actionRow.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, score, tacticBox, subRow, log, actionRow);
        return new Scene(root, 720, 640);
    }


    private void configureTacticCombo(ComboBox<Tactic> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Tactic tactic) {
                return tactic == null ? "" : tactic.getName();
            }

            @Override
            public Tactic fromString(String string) {
                return null;
            }
        });
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Tactic tactic, boolean empty) {
                super.updateItem(tactic, empty);
                setText(empty || tactic == null ? null : tactic.getName());
            }
        });
    }

    private void configurePlayerCombo(ComboBox<IPlayer> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(IPlayer player) {
                return playerLabel(player);
            }

            @Override
            public IPlayer fromString(String string) {
                return null;
            }
        });
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(IPlayer player, boolean empty) {
                super.updateItem(player, empty);
                setText(empty || player == null ? null : playerLabel(player));
            }
        });
    }

    private String playerLabel(IPlayer player) {
        if (player == null) {
            return "";
        }
        return player.getName() + " - " + player.getPosition() + " (" + player.getOverallRating() + ")";
    }

    private String formatScoreLabel(ITeam home, ITeam away, int h, int a) {
        boolean vb = session.getSport().getName().equalsIgnoreCase("Volleyball");
        String u = vb ? "sets" : "goals";
        return String.format("%s %d - %d %s (%s)", home.getName(), h, a, away.getName(), u);
    }

    private void appendPhaseEvents(PhaseResult pr, TextArea log) {
        if (pr.events == null) {
            return;
        }
        for (var ev : pr.events) {
            log.appendText(String.format("  [%d'] %s %s\n",
                    ev.getMinute(), ev.getType(), ev.getDescription()));
        }
    }

    private void finalizeMatch(TextArea log, Label score, Button nextBtn, Button simEndBtn, Button continueBtn) {
        if (matchRecorded) {
            return;
        }
        try {
            IMatchResult res = engine.endMatch();
            session.getLeague().recordResult(fixture, res);
            matchRecorded = true;
            nextBtn.setDisable(true);
            simEndBtn.setDisable(true);
            log.appendText(String.format("Full time: %d - %d\n", res.getHomeScore(), res.getAwayScore()));
            score.setText("FINAL  " + formatScoreLabel(fixture.getHomeTeam(), fixture.getAwayTeam(),
                    res.getHomeScore(), res.getAwayScore()));
            score.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e94560;");
            continueBtn.setVisible(true);
            continueBtn.setManaged(true);
        } catch (IllegalStateException ex) {
            alert(ex.getMessage());
        }
    }

    private void refreshSubCombos(ITeam player, ComboBox<IPlayer> outSub, ComboBox<IPlayer> inSub) {
        if (player == null) {
            return;
        }
        List<IPlayer> starters = player.getStartingEleven();
        List<IPlayer> bench = player.getMatchDaySquad().stream()
                .filter(p -> starters.stream().noneMatch(s -> s == p))
                .filter(p -> !p.isInjured())
                .collect(Collectors.toList());
        outSub.setItems(FXCollections.observableArrayList(starters));
        inSub.setItems(FXCollections.observableArrayList(bench));
    }

    private void applySubstitution(ITeam player, IPlayer out, IPlayer in, TextArea log,
                                   ComboBox<IPlayer> outSub, ComboBox<IPlayer> inSub) {
        if (player == null || out == null || in == null) {
            alert("Pick both players for substitution.");
            return;
        }
        try {
            player.substitute(out, in);
            log.appendText("Substitution: " + out.getName() + " -> " + in.getName() + "\n");
            refreshSubCombos(player, outSub, inSub);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            alert(ex.getMessage());
        }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
