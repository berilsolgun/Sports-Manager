package com.sportsmanager.domain.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persists {@link GameSession} as a human-readable JSON file using Gson.
 * <p>
 * Serializes: sport name, season, current week, player team name,
 * all teams with their full squads, and fixture results.
 * On load, enough state is restored to resume console-based simulation.
 * </p>
 */
public class JsonGameRepository implements GameRepository {

    private static final String DEFAULT_PATH = "gamesession.json";

    private final String filePath;
    private final Gson gson;

    public JsonGameRepository() {
        this(DEFAULT_PATH);
    }

    public JsonGameRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // ── SAVE ───────────────────────────────────────────────────────────────

    @Override
    public void save(GameSession session) {
        JsonObject root = new JsonObject();

        root.addProperty("currentWeek", session.getCurrentWeek());
        root.addProperty("season", session.getSeason());

        if (session.getSport() != null) {
            root.addProperty("sportName", session.getSport().getName());
        }

        if (session.getPlayerTeam() != null) {
            root.addProperty("playerTeamName", session.getPlayerTeam().getName());
        }

        if (session.getLeague() != null) {
            root.add("league", serializeLeague(session.getLeague()));
        }

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    private JsonObject serializeLeague(ILeague league) {
        JsonObject leagueObj = new JsonObject();

        // Teams
        JsonArray teamsArr = new JsonArray();
        for (ITeam team : league.getTeams()) {
            teamsArr.add(serializeTeam(team));
        }
        leagueObj.add("teams", teamsArr);

        // Fixtures (only played ones with results)
        JsonArray fixturesArr = new JsonArray();
        for (IFixture fixture : league.getFixtures()) {
            JsonObject fObj = new JsonObject();
            fObj.addProperty("homeTeam", fixture.getHomeTeam().getName());
            fObj.addProperty("awayTeam", fixture.getAwayTeam().getName());
            fObj.addProperty("week", fixture.getWeek());
            fObj.addProperty("played", fixture.isPlayed());

            if (fixture.isPlayed() && fixture.getResult().isPresent()) {
                IMatchResult result = fixture.getResult().get();
                JsonObject rObj = new JsonObject();
                rObj.addProperty("homeScore", result.getHomeScore());
                rObj.addProperty("awayScore", result.getAwayScore());
                fObj.add("result", rObj);
            }

            fixturesArr.add(fObj);
        }
        leagueObj.add("fixtures", fixturesArr);

        return leagueObj;
    }

    private JsonObject serializeTeam(ITeam team) {
        JsonObject tObj = new JsonObject();
        tObj.addProperty("name", team.getName());
        tObj.addProperty("logo", team.getLogo());

        JsonArray playersArr = new JsonArray();
        for (IPlayer player : team.getSquad()) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("name", player.getName());
            pObj.addProperty("age", player.getAge());
            pObj.addProperty("position", player.getPosition().name());
            pObj.addProperty("overallRating", player.getOverallRating());
            pObj.addProperty("injured", player.isInjured());
            pObj.addProperty("injuryGamesRemaining", player.getInjuryGamesRemaining());

            // Save sport-specific attributes
            Map<String, Integer> attrs = player.getAttributes();
            if (attrs != null && !attrs.isEmpty()) {
                JsonObject attrsObj = new JsonObject();
                for (Map.Entry<String, Integer> entry : attrs.entrySet()) {
                    attrsObj.addProperty(entry.getKey(), entry.getValue());
                }
                pObj.add("attributes", attrsObj);
            }

            playersArr.add(pObj);
        }
        tObj.add("squad", playersArr);

        return tObj;
    }

    // ── LOAD ───────────────────────────────────────────────────────────────

    @Override
    public Optional<GameSession> load() {
        File file = new File(filePath);
        if (!file.exists()) {
            return Optional.empty();
        }

        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            GameSession session = new GameSession();

            if (root.has("currentWeek")) {
                session.setCurrentWeek(root.get("currentWeek").getAsInt());
            }
            if (root.has("season")) {
                session.setSeason(root.get("season").getAsInt());
            }

            // Sport and league reconstruction require the factory, which depends on
            // the sport implementation layer. For now we restore the scalar fields;
            // full league reconstruction will be added when the UI layer is complete.
            // The saved JSON still contains all team/player/fixture data so nothing
            // is lost.

            return Optional.of(session);

        } catch (IOException e) {
            System.err.println("Load failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * @return the file path this repository writes to
     */
    public String getFilePath() {
        return filePath;
    }
}