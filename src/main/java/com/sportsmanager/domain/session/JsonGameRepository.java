package com.sportsmanager.domain.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.MatchEvent;
import com.sportsmanager.domain.simulation.MatchEventType;
import com.sportsmanager.domain.team.ICoach;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.domain.team.Tactic;
import com.sportsmanager.football.FootballCoach;
import com.sportsmanager.football.FootballMatchResult;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballTeam;
import com.sportsmanager.volleyball.VolleyballCoach;
import com.sportsmanager.volleyball.VolleyballMatchResult;
import com.sportsmanager.volleyball.VolleyballPlayer;
import com.sportsmanager.volleyball.VolleyballPosition;
import com.sportsmanager.volleyball.VolleyballTeam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persists {@link GameSession} as JSON using Gson.
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

        JsonArray teamsArr = new JsonArray();
        for (ITeam team : league.getTeams()) {
            teamsArr.add(serializeTeam(team));
        }
        leagueObj.add("teams", teamsArr);

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
                rObj.addProperty("homePoints", result.getHomePoints());
                rObj.addProperty("awayPoints", result.getAwayPoints());

                JsonArray evArr = new JsonArray();
                for (MatchEvent ev : result.getEvents()) {
                    JsonObject eObj = new JsonObject();
                    eObj.addProperty("minute", ev.getMinute());
                    eObj.addProperty("phase", ev.getPhase());
                    eObj.addProperty("type", ev.getType().name());
                    eObj.addProperty("description", ev.getDescription());
                    if (ev.getTeam() != null) {
                        eObj.addProperty("teamName", ev.getTeam().getName());
                    }
                    if (ev.getPlayer() != null) {
                        eObj.addProperty("playerName", ev.getPlayer().getName());
                    }
                    evArr.add(eObj);
                }
                rObj.add("events", evArr);

                JsonArray injArr = new JsonArray();
                for (IPlayer ip : result.getInjuredPlayers()) {
                    injArr.add(ip.getName());
                }
                rObj.add("injuredPlayers", injArr);

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

        JsonArray coachesArr = new JsonArray();
        for (ICoach c : team.getCoaches()) {
            JsonObject cObj = new JsonObject();
            cObj.addProperty("name", c.getName());
            cObj.addProperty("speciality", c.getSpeciality());
            coachesArr.add(cObj);
        }
        tObj.add("coaches", coachesArr);

        if (team.getCurrentTactic() != null) {
            Tactic tac = team.getCurrentTactic();
            JsonObject tacObj = new JsonObject();
            tacObj.addProperty("name", tac.getName() != null ? tac.getName() : "");
            tacObj.addProperty("attackBonus", tac.getAttackBonus());
            tacObj.addProperty("defenseBonus", tac.getDefenseBonus());
            tObj.add("tactic", tacObj);
        }

        JsonArray mds = new JsonArray();
        for (IPlayer p : team.getMatchDaySquad()) {
            mds.add(p.getName());
        }
        tObj.add("matchDaySquad", mds);

        JsonArray starters = new JsonArray();
        for (IPlayer p : team.getStartingEleven()) {
            starters.add(p.getName());
        }
        tObj.add("startingEleven", starters);

        JsonArray playersArr = new JsonArray();
        for (IPlayer player : team.getSquad()) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("name", player.getName());
            pObj.addProperty("age", player.getAge());
            if (player instanceof FootballPlayer fp) {
                pObj.addProperty("position", fp.getFootballPosition().name());
            } else if (player instanceof VolleyballPlayer vp) {
                pObj.addProperty("position", vp.getVolleyballPosition().name());
            } else {
                pObj.addProperty("position", player.getPosition().name());
            }
            pObj.addProperty("overallRating", player.getOverallRating());
            pObj.addProperty("injured", player.isInjured());
            pObj.addProperty("injuryGamesRemaining", player.getInjuryGamesRemaining());

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
            int maxMatch = sport.getMaxMatchSquadSize();

            List<ITeam> teams = new ArrayList<>();
            Map<String, ITeam> teamByName = new HashMap<>();

            if (root.has("league")) {
                JsonObject leagueObj = root.getAsJsonObject("league");

                if (leagueObj.has("teams")) {
                    JsonArray teamsArr = leagueObj.getAsJsonArray("teams");
                    for (int i = 0; i < teamsArr.size(); i++) {
                        JsonObject teamObj = teamsArr.get(i).getAsJsonObject();
                        String teamName = teamObj.get("name").getAsString();
                        String logo = teamObj.has("logo") ? teamObj.get("logo").getAsString() : "";

                        ITeam team;
                        if (sportName.equalsIgnoreCase("Football")) {
                            team = new FootballTeam(teamName, logo);
                        } else {
                            team = new VolleyballTeam(teamName, logo);
                        }

                        if (teamObj.has("squad")) {
                            JsonArray squadArr = teamObj.getAsJsonArray("squad");
                            for (int j = 0; j < squadArr.size(); j++) {
                                JsonObject pObj = squadArr.get(j).getAsJsonObject();
                                team.addPlayer(deserializePlayer(sportName, pObj));
                            }
                        }

                        if (teamObj.has("coaches")) {
                            JsonArray carr = teamObj.getAsJsonArray("coaches");
                            for (int k = 0; k < carr.size(); k++) {
                                JsonObject cObj = carr.get(k).getAsJsonObject();
                                String cn = cObj.get("name").getAsString();
                                String spec = cObj.has("speciality") ? cObj.get("speciality").getAsString() : "Fitness";
                                if (sportName.equalsIgnoreCase("Football")) {
                                    team.getCoaches().add(new FootballCoach(cn, spec));
                                } else {
                                    team.getCoaches().add(new VolleyballCoach(cn, spec));
                                }
                            }
                        }

                        if (teamObj.has("tactic")) {
                            JsonObject tacObj = teamObj.getAsJsonObject("tactic");
                            Tactic tac = new Tactic(
                                    tacObj.has("name") ? tacObj.get("name").getAsString() : "",
                                    tacObj.has("attackBonus") ? tacObj.get("attackBonus").getAsDouble() : 1.0,
                                    tacObj.has("defenseBonus") ? tacObj.get("defenseBonus").getAsDouble() : 1.0);
                            team.setTactic(tac);
                        }

                        if (teamObj.has("matchDaySquad") && teamObj.get("matchDaySquad").isJsonArray()) {
                            JsonArray mds = teamObj.getAsJsonArray("matchDaySquad");
                            if (mds.size() > 0) {
                                List<IPlayer> mdPlayers = resolvePlayersByName(team, mds);
                                team.setMatchDaySquad(mdPlayers, maxMatch);
                            } else {
                                team.initializeMatchDaySquad(maxMatch);
                            }
                        } else {
                            team.initializeMatchDaySquad(maxMatch);
                        }

                        if (teamObj.has("startingEleven") && teamObj.get("startingEleven").isJsonArray()) {
                            JsonArray se = teamObj.getAsJsonArray("startingEleven");
                            if (se.size() > 0) {
                                team.setStartingEleven(resolvePlayersByName(team, se));
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

                ILeague league = factory.createLeague(sportName + " League", teams);

                if (leagueObj.has("fixtures")) {
                    JsonArray fixturesArr = leagueObj.getAsJsonArray("fixtures");
                    List<IFixture> leagueFixtures = league.getFixtures();

                    for (int i = 0; i < fixturesArr.size(); i++) {
                        JsonObject fObj = fixturesArr.get(i).getAsJsonObject();
                        if (!fObj.has("result")) {
                            continue;
                        }

                        String homeName = fObj.get("homeTeam").getAsString();
                        String awayName = fObj.get("awayTeam").getAsString();
                        int week = fObj.get("week").getAsInt();

                        JsonObject rObj = fObj.getAsJsonObject("result");
                        int homeScore = rObj.get("homeScore").getAsInt();
                        int awayScore = rObj.get("awayScore").getAsInt();

                        ITeam homeT = teamByName.get(homeName);
                        ITeam awayT = teamByName.get(awayName);

                        List<MatchEvent> events = rObj.has("events")
                                ? deserializeEvents(rObj.getAsJsonArray("events"), teamByName)
                                : Collections.emptyList();
                        List<IPlayer> injured = rObj.has("injuredPlayers")
                                ? resolveInjured(rObj.getAsJsonArray("injuredPlayers"), homeT, awayT)
                                : Collections.emptyList();

                        IMatchResult result = buildRestoredResult(sportName, homeScore, awayScore,
                                homeT, awayT, events, injured);

                        for (IFixture fx : leagueFixtures) {
                            if (fx.getHomeTeam().getName().equals(homeName)
                                    && fx.getAwayTeam().getName().equals(awayName)
                                    && fx.getWeek() == week) {
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

    private static IPlayer deserializePlayer(String sportName, JsonObject pObj) {
        String pName = pObj.get("name").getAsString();
        int pAge = pObj.get("age").getAsInt();
        String posStr = pObj.get("position").getAsString();
        JsonObject attrs = pObj.has("attributes") ? pObj.getAsJsonObject("attributes") : new JsonObject();

        IPlayer player;
        if (sportName.equalsIgnoreCase("Football")) {
            FootballPosition fbPos = FootballPosition.valueOf(posStr);
            player = new FootballPlayer(
                    pName, pAge, fbPos,
                    attrs.has("pace") ? attrs.get("pace").getAsInt() : 50,
                    attrs.has("shooting") ? attrs.get("shooting").getAsInt() : 50,
                    attrs.has("passing") ? attrs.get("passing").getAsInt() : 50,
                    attrs.has("dribbling") ? attrs.get("dribbling").getAsInt() : 50,
                    attrs.has("defending") ? attrs.get("defending").getAsInt() : 50,
                    attrs.has("physical") ? attrs.get("physical").getAsInt() : 50,
                    attrs.has("goalkeeping") ? attrs.get("goalkeeping").getAsInt() : 50);
        } else {
            VolleyballPosition vbPos = VolleyballPosition.valueOf(posStr);
            player = new VolleyballPlayer(
                    pName, pAge, vbPos,
                    attrs.has("serving") ? attrs.get("serving").getAsInt() : 50,
                    attrs.has("setting") ? attrs.get("setting").getAsInt() : 50,
                    attrs.has("spiking") ? attrs.get("spiking").getAsInt() : 50,
                    attrs.has("blocking") ? attrs.get("blocking").getAsInt() : 50,
                    attrs.has("digging") ? attrs.get("digging").getAsInt() : 50,
                    attrs.has("receiving") ? attrs.get("receiving").getAsInt() : 50,
                    attrs.has("physical") ? attrs.get("physical").getAsInt() : 50);
        }

        if (pObj.has("injured") && pObj.get("injured").getAsBoolean()) {
            int injuryGames = pObj.has("injuryGamesRemaining")
                    ? pObj.get("injuryGamesRemaining").getAsInt() : 1;
            if (injuryGames > 0) {
                player.injure(injuryGames);
            }
        }

        return player;
    }

    private static List<IPlayer> resolvePlayersByName(ITeam team, JsonArray names) {
        List<IPlayer> out = new ArrayList<>();
        List<IPlayer> pool = new ArrayList<>(team.getSquad());
        for (int i = 0; i < names.size(); i++) {
            String nm = names.get(i).getAsString();
            for (int j = 0; j < pool.size(); j++) {
                if (pool.get(j).getName().equals(nm)) {
                    out.add(pool.remove(j));
                    break;
                }
            }
        }
        return out;
    }

    private static Optional<IPlayer> findPlayer(ITeam team, String name) {
        return team.getSquad().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    private static List<MatchEvent> deserializeEvents(JsonArray arr, Map<String, ITeam> teamByName) {
        List<MatchEvent> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.get(i).getAsJsonObject();
            int minute = o.get("minute").getAsInt();
            int phase = o.get("phase").getAsInt();
            MatchEventType type = MatchEventType.valueOf(o.get("type").getAsString());
            String desc = o.has("description") ? o.get("description").getAsString() : "";
            ITeam team = null;
            if (o.has("teamName")) {
                team = teamByName.get(o.get("teamName").getAsString());
            }
            IPlayer pl = null;
            if (team != null && o.has("playerName")) {
                pl = findPlayer(team, o.get("playerName").getAsString()).orElse(null);
            }
            list.add(new MatchEvent(minute, phase, type, desc, team, pl));
        }
        return list;
    }

    private static List<IPlayer> resolveInjured(JsonArray arr, ITeam home, ITeam away) {
        List<IPlayer> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            String nm = arr.get(i).getAsString();
            findPlayer(home, nm).ifPresent(list::add);
            findPlayer(away, nm).ifPresent(list::add);
        }
        return list;
    }

    private static IMatchResult buildRestoredResult(String sportName, int homeScore, int awayScore,
                                                    ITeam homeT, ITeam awayT,
                                                    List<MatchEvent> events, List<IPlayer> injured) {
        if (sportName.equalsIgnoreCase("Football")) {
            return new FootballMatchResult(homeScore, awayScore, homeT, awayT, events, injured);
        }
        return new VolleyballMatchResult(homeScore, awayScore, homeT, awayT, events, injured);
    }

    public String getFilePath() {
        return filePath;
    }
}
