package com.sportsmanager;

import com.sportsmanager.application.LeagueController;
import com.sportsmanager.application.MatchController;
import com.sportsmanager.application.WeekController;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballMatchEngine;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== Sports Manager ===");

        FootballFactory factory = new FootballFactory();

        // Takımlar rastgele seç
        String[] allTeamNames = factory.getTeamNames();
        List<String> nameList = new ArrayList<>(Arrays.asList(allTeamNames));
        Collections.shuffle(nameList);

        List<ITeam> teams = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String name = nameList.get(i);
            ITeam team = factory.createTeam(name, name.toLowerCase() + ".png");
            setStartingEleven((FootballTeam) team);
            teams.add(team);
        }

        // Lig oluştur
        ILeague league = factory.createLeague("Super Lig", teams);

        // Session oluştur
        GameSession session = new GameSession();
        session.setCurrentWeek(1);
        session.setSeason(2026);
        session.setLeague(league);
        session.setPlayerTeam(teams.get(0));

        // Kaydet
        JsonGameRepository repo = new JsonGameRepository();
        repo.save(session);
        System.out.println("Session kaydedildi.");

        // Simülasyon
        FootballMatchEngine engine = new FootballMatchEngine();
        WeekController weekController = new WeekController();
        MatchController matchController = new MatchController();
        LeagueController leagueController = new LeagueController();

        System.out.println("\n--- Sezon Simulasyonu ---");
        for (int i = 0; i < 3; i++) {
            weekController.advanceWeek(session);
            matchController.playCurrentWeek(session, engine);
            List<StandingEntry> standings = leagueController.getStandings(session);
            System.out.println("\nHafta " + (i + 1) + " Puan Durumu:");
            for (StandingEntry entry : standings) {
                System.out.println(entry.getTeam().getName() + " - " + entry.getPoints() + " puan");
            }
        }

        // Yükle
        repo.load().ifPresent(s ->
            System.out.println("\nSession yuklendi. Hafta: " + s.getCurrentWeek())
        );
    }

    private static void setStartingEleven(FootballTeam team) {
        List<FootballPlayer> squad = team.getSquad().stream()
            .filter(p -> p instanceof FootballPlayer)
            .map(p -> (FootballPlayer) p)
            .toList();

        List<FootballPlayer> eleven = new ArrayList<>();
        FootballPosition[] positions = {
            FootballPosition.GK,
            FootballPosition.CB, FootballPosition.CB,
            FootballPosition.LB, FootballPosition.RB,
            FootballPosition.CM, FootballPosition.CM, FootballPosition.CM,
            FootballPosition.LW, FootballPosition.RW,
            FootballPosition.ST
        };

        for (FootballPosition pos : positions) {
            squad.stream()
                .filter(p -> p.getFootballPosition() == pos && !eleven.contains(p))
                .findFirst()
                .ifPresent(eleven::add);
        }

        if (eleven.size() == 11) {
            team.setStartingEleven(new ArrayList<>(eleven));
        }
    }
}