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
        root.addProperty("championshipCount", session.getChampionshipCount());
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
            // Save sport-specific position for accurate restore
if (player instanceof com.sportsmanager.football.FootballPlayer fp) {
    pObj.addProperty("position", fp.getFootballPosition().name());
} else if (player instanceof com.sportsmanager.volleyball.VolleyballPlayer vp) {
    pObj.addProperty("position", vp.getVolleyballPosition().name());
} else {
    pObj.addProperty("position", player.getPosition().name());
}
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
        if (root.has("championshipCount")) {
    session.setChampionshipCount(root.get("championshipCount").getAsInt());
}

        if (!root.has("sportName")) {
            return Optional.of(session);
        }

        String sportName = root.get("sportName").getAsString();
        com.sportsmanager.domain.sport.Sport sport;
        if (sportName.equalsIgnoreCase("Football")) {
            sport = new com.sportsmanager.football.FootballSport();
        } else if (sportName.equalsIgnoreCase("Volleyball")) {
            sport = new com.sportsmanager.volleyball.VolleyballSport();
        } else {
            return Optional.of(session);
        }

        session.setSport(sport);
        com.sportsmanager.domain.sport.SportFactory factory = sport.createFactory();

        // Recreate teams
        java.util.List<com.sportsmanager.domain.team.ITeam> teams = new java.util.ArrayList<>();
        java.util.Map<String, com.sportsmanager.domain.team.ITeam> teamByName = new java.util.HashMap<>();

        if (root.has("league")) {
            JsonObject leagueObj = root.getAsJsonObject("league");

           if (leagueObj.has("teams")) {
                JsonArray teamsArr = leagueObj.getAsJsonArray("teams");
                for (int i = 0; i < teamsArr.size(); i++) {
                    JsonObject teamObj = teamsArr.get(i).getAsJsonObject();
                    String teamName = teamObj.get("name").getAsString();
                    String logo = teamObj.has("logo") ? teamObj.get("logo").getAsString() : "";

                    // Create empty team (no random players)
                    com.sportsmanager.domain.team.ITeam team;
                    if (sportName.equalsIgnoreCase("Football")) {
                        team = new com.sportsmanager.football.FootballTeam(teamName, logo);
                    } else {
                        team = new com.sportsmanager.volleyball.VolleyballTeam(teamName, logo);
                    }

                    // Restore each saved player exactly
                    if (teamObj.has("squad")) {
                        JsonArray squadArr = teamObj.getAsJsonArray("squad");
                        for (int j = 0; j < squadArr.size(); j++) {
                            JsonObject pObj = squadArr.get(j).getAsJsonObject();
                            String pName = pObj.get("name").getAsString();
                            int pAge = pObj.get("age").getAsInt();
                            String posStr = pObj.get("position").getAsString();
                            JsonObject attrs = pObj.has("attributes") ? pObj.getAsJsonObject("attributes") : new JsonObject();

                            com.sportsmanager.domain.team.IPlayer player;

                            if (sportName.equalsIgnoreCase("Football")) {
                                com.sportsmanager.football.FootballPosition fbPos =
                                        com.sportsmanager.football.FootballPosition.valueOf(posStr);
                                player = new com.sportsmanager.football.FootballPlayer(
                                        pName, pAge, fbPos,
                                        attrs.has("pace") ? attrs.get("pace").getAsInt() : 50,
                                        attrs.has("shooting") ? attrs.get("shooting").getAsInt() : 50,
                                        attrs.has("passing") ? attrs.get("passing").getAsInt() : 50,
                                        attrs.has("dribbling") ? attrs.get("dribbling").getAsInt() : 50,
                                        attrs.has("defending") ? attrs.get("defending").getAsInt() : 50,
                                        attrs.has("physical") ? attrs.get("physical").getAsInt() : 50,
                                        attrs.has("goalkeeping") ? attrs.get("goalkeeping").getAsInt() : 50);
                            } else {
                                com.sportsmanager.volleyball.VolleyballPosition vbPos =
                                        com.sportsmanager.volleyball.VolleyballPosition.valueOf(posStr);
                                player = new com.sportsmanager.volleyball.VolleyballPlayer(
                                        pName, pAge, vbPos,
                                        attrs.has("serving") ? attrs.get("serving").getAsInt() : 50,
                                        attrs.has("setting") ? attrs.get("setting").getAsInt() : 50,
                                        attrs.has("spiking") ? attrs.get("spiking").getAsInt() : 50,
                                        attrs.has("blocking") ? attrs.get("blocking").getAsInt() : 50,
                                        attrs.has("digging") ? attrs.get("digging").getAsInt() : 50,
                                        attrs.has("receiving") ? attrs.get("receiving").getAsInt() : 50,
                                        attrs.has("physical") ? attrs.get("physical").getAsInt() : 50);
                            }

                            // Restore injury state
                            if (pObj.has("injured") && pObj.get("injured").getAsBoolean()) {
                                int injuryGames = pObj.has("injuryGamesRemaining")
                                        ? pObj.get("injuryGamesRemaining").getAsInt() : 1;
                                if (injuryGames > 0) {
                                    player.injure(injuryGames);
                                }
                            }

                            team.addPlayer(player);
                        }
                    }

                    teams.add(team);
                    teamByName.put(teamName, team);
                }
            }
            if (root.has("playerTeamName")) {
                String playerTeamName = root.get("playerTeamName").getAsString();
                if (teamByName.containsKey(playerTeamName)) {
                    session.setPlayerTeam(teamByName.get(playerTeamName));
                }
            }

            // Recreate league
            com.sportsmanager.domain.league.ILeague league = factory.createLeague(sportName + " League", teams);

            // Restore fixture results
            if (leagueObj.has("fixtures")) {
                JsonArray fixturesArr = leagueObj.getAsJsonArray("fixtures");
                java.util.List<com.sportsmanager.domain.league.IFixture> leagueFixtures = league.getFixtures();

                for (int i = 0; i < fixturesArr.size(); i++) {
                    JsonObject fObj = fixturesArr.get(i).getAsJsonObject();
                    if (!fObj.has("result")) continue;

                    String homeName = fObj.get("homeTeam").getAsString();
                    String awayName = fObj.get("awayTeam").getAsString();
                    int week = fObj.get("week").getAsInt();

                    JsonObject rObj = fObj.getAsJsonObject("result");
                    int homeScore = rObj.get("homeScore").getAsInt();
                    int awayScore = rObj.get("awayScore").getAsInt();

                    // Find matching fixture in league
                    for (com.sportsmanager.domain.league.IFixture fx : leagueFixtures) {
                        if (fx.getHomeTeam().getName().equals(homeName)
                                && fx.getAwayTeam().getName().equals(awayName)
                                && fx.getWeek() == week) {
                            com.sportsmanager.domain.team.ITeam homeT = teamByName.get(homeName);
                            com.sportsmanager.domain.team.ITeam awayT = teamByName.get(awayName);
                            com.sportsmanager.domain.league.IMatchResult result =
                                    new SimpleMatchResult(homeScore, awayScore, homeT, awayT);
                            league.recordResult(fx, result);
                            break;
                        }
                    }
                }
            }

            session.setLeague(league);
        }

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
    // Helper class for restoring fixture results during load
private static class SimpleMatchResult implements com.sportsmanager.domain.league.IMatchResult {
    private final int homeScore;
    private final int awayScore;
    private final com.sportsmanager.domain.team.ITeam home;
    private final com.sportsmanager.domain.team.ITeam away;

    SimpleMatchResult(int homeScore, int awayScore,
                      com.sportsmanager.domain.team.ITeam home,
                      com.sportsmanager.domain.team.ITeam away) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.home = home;
        this.away = away;
    }

    @Override public int getHomeScore() { return homeScore; }
    @Override public int getAwayScore() { return awayScore; }
    @Override public int getHomePoints() {
        if (homeScore > awayScore) return 3;
        if (homeScore == awayScore) return 1;
        return 0;
    }
    @Override public int getAwayPoints() {
        if (awayScore > homeScore) return 3;
        if (homeScore == awayScore) return 1;
        return 0;
    }
    @Override public boolean isDraw() { return homeScore == awayScore; }
    @Override public java.util.Optional<com.sportsmanager.domain.team.ITeam> getWinner() {
        if (homeScore > awayScore) return java.util.Optional.of(home);
        if (awayScore > homeScore) return java.util.Optional.of(away);
        return java.util.Optional.empty();
    }
    @Override public java.util.List<com.sportsmanager.domain.simulation.MatchEvent> getEvents() {
        return java.util.Collections.emptyList();
    }
    @Override public java.util.List<com.sportsmanager.domain.team.IPlayer> getInjuredPlayers() {
        return java.util.Collections.emptyList();
    }
}
}