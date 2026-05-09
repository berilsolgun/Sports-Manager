package com.sportsmanager.ui;

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

public class HelpScreen {

    private final Stage stage;
    private final GameSession session;
    private final Runnable onBack;

    public HelpScreen(Stage stage, GameSession session, Runnable onBack) {
        this.stage = stage;
        this.session = session;
        this.onBack = onBack;
    }

    public Scene createScene() {
        String bg = session.getSport().getName().equalsIgnoreCase("Volleyball") ? "#4a1c40" : "#16213e";

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + bg + ";");

        Label title = new Label("How to Play");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        TabPane tabs = new TabPane();
        Tab general = new Tab("General");
        general.setClosable(false);
        general.setContent(scrollText(generalHelp()));

        Tab football = new Tab("Football");
        football.setClosable(false);
        football.setContent(scrollText(footballHelp()));

        Tab volleyball = new Tab("Volleyball");
        volleyball.setClosable(false);
        volleyball.setContent(scrollText(volleyballHelp()));

        if (session.getSport().getName().equalsIgnoreCase("Volleyball")) {
            tabs.getTabs().addAll(general, volleyball, football);
        } else {
            tabs.getTabs().addAll(general, football, volleyball);
        }

        Button back = new Button("Back to Dashboard");
        back.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 25;");
        back.setOnAction(e -> onBack.run());

        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.getChildren().addAll(title, tabs, back);
        return new Scene(root, 760, 640);
    }

    private ScrollPane scrollText(String body) {
        Label lbl = new Label(body);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: #eaeaea; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px; "
                + "-fx-padding: 14; -fx-background-color: #1a1a2e;");
        VBox wrapper = new VBox(lbl);
        wrapper.setStyle("-fx-background-color: #1a1a2e;");
        ScrollPane sp = new ScrollPane(wrapper);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        return sp;
    }

    private String generalHelp() {
        return "GENERAL\n"
                + "=======\n\n"
                + "GOAL: Manage your team across a full season. Win matches to climb the standings, win the league at season end, and accumulate championships across multiple seasons.\n\n"
                + "DASHBOARD BUTTONS:\n"
                + "- Play Match: Plays the current week. If you have already chosen a tactic and a valid lineup, you go straight into the match. Otherwise you'll be prompted.\n"
                + "- Quick Sim Week: Auto-resolves the entire week (including your own match) without showing the live match screen. Uses your current tactic and lineup; auto-picks if missing.\n"
                + "- Change Tactic: Pick or change your tactic at any time. Your current tactic is highlighted.\n"
                + "- Change Lineup: Edit your starting players. The Auto-pick best button fills the strongest legal lineup automatically.\n"
                + "- View Squad: Browse rosters and player ratings, including other clubs.\n"
                + "- Schedule: See past results and upcoming fixtures.\n"
                + "- Save Game: Save your progress to a JSON file. Reload it from the start menu.\n\n"
                + "MATCH FLOW:\n"
                + "- Tactic and lineup persist between weeks. After choosing them once, future weeks skip the setup unless someone got injured.\n"
                + "- During a live match, you can change tactic mid-match, sub players, play one phase at a time, or click Sim to End to fast-forward.\n"
                + "- After the final phase, the FINAL score is shown on screen. Click Continue to return to the dashboard and see all weekly results.\n\n"
                + "STANDINGS:\n"
                + "- Sorted by points, then head-to-head, then goal/set difference, then goals/sets scored.\n\n"
                + "INJURIES:\n"
                + "- Injured players cannot start. They recover one game per match week their team plays. They heal fully at season end.\n";
    }

    private String footballHelp() {
        return "FOOTBALL\n"
                + "========\n\n"
                + "SQUAD SIZES:\n"
                + "- Full squad: 25 players\n"
                + "- Match-day squad: 18 players\n"
                + "- Starting eleven: 11 players\n\n"
                + "LINEUP RULES:\n"
                + "- Exactly 1 goalkeeper (GK).\n"
                + "- Any combination of 10 outfield players (DF / MF / FW).\n\n"
                + "POSITIONS:\n"
                + "- GK: Goalkeeper\n"
                + "- CB / LB / RB: Defenders\n"
                + "- CDM / CM / CAM / LM / RM: Midfielders\n"
                + "- LW / RW / ST / CF: Forwards\n\n"
                + "PLAYER ATTRIBUTES (drive ratings differently per position):\n"
                + "- pace, shooting, passing, dribbling, defending, physical, goalkeeping.\n"
                + "- Overall rating is a weighted blend, weighted by position. A striker's overall is mostly shooting/pace; a CB's is mostly defending/physical.\n\n"
                + "MATCH STRUCTURE:\n"
                + "- 2 phases (first half, second half). Each phase produces goals, events, and possible injuries.\n"
                + "- Click Play next phase to play the current half. Click Sim to end to fast-forward both halves.\n\n"
                + "TACTICS:\n"
                + "- Attacking: boosts attack, weaker defence.\n"
                + "- Defensive: boosts defence, weaker attack.\n"
                + "- Balanced: even bonuses.\n"
                + "- AI teams pick a random tactic each match.\n\n"
                + "POINTS:\n"
                + "- Win: 3 points. Draw: 1 point each. Loss: 0.\n";
    }

    private String volleyballHelp() {
        return "VOLLEYBALL\n"
                + "==========\n\n"
                + "SQUAD SIZES:\n"
                + "- Full squad: 14 players (FIVB rules)\n"
                + "- Match-day squad: 12 players\n"
                + "- Starting six: 6 players\n\n"
                + "LINEUP RULES:\n"
                + "- At least 1 SETTER.\n"
                + "- At most 1 LIBERO.\n"
                + "- Remaining slots are hitters (Outside, Opposite, Middle Blocker).\n\n"
                + "POSITIONS:\n"
                + "- SETTER: Sets the ball for hitters.\n"
                + "- OUTSIDE_HITTER / OPPOSITE_HITTER / MIDDLE_BLOCKER: Attackers.\n"
                + "- LIBERO: Defensive specialist (back-row only).\n\n"
                + "MATCH STRUCTURE:\n"
                + "- Best of 5 sets. Match ends as soon as a team wins 3 sets.\n"
                + "- Each phase is one set. Click Play next phase set-by-set or Sim to end to play out the rest.\n\n"
                + "TACTICS:\n"
                + "- Attacking: boosts attack power, weaker block/defence.\n"
                + "- Defensive: boosts block/defence, weaker attack.\n"
                + "- Balanced: even bonuses.\n\n"
                + "POINTS:\n"
                + "- 3-0 or 3-1 win: 3 points to winner, 0 to loser.\n"
                + "- 3-2 win: 2 points to winner, 1 to loser.\n";
    }
}
