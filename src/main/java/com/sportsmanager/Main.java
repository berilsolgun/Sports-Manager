package com.sportsmanager;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.football.*;
import com.sportsmanager.application.*;
import com.sportsmanager.domain.league.StandingEntry;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== Sports Manager ===");

        // Takımlar oluştur
        FootballTeam team1 = new FootballTeam("Galatasaray", "gs.png");
        FootballTeam team2 = new FootballTeam("Fenerbahce", "fb.png");
        FootballTeam team3 = new FootballTeam("Besiktas", "bjk.png");
        FootballTeam team4 = new FootballTeam("Trabzonspor", "ts.png");

        // Oyuncular ekle
        addPlayers(team1);
        addPlayers(team2);
        addPlayers(team3);
        addPlayers(team4);

        // Lig oluştur
        FootballLeague league = new FootballLeague("Super Lig",
            List.of(team1, team2, team3, team4));

        // Session oluştur
        GameSession session = new GameSession();
        session.setCurrentWeek(1);
        session.setSeason(2026);
        session.setLeague(league);
        session.setPlayerTeam(team1);

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

   private static void addPlayers(FootballTeam team) {
        String[] allNames = {
            "Ahmet", "Mehmet", "Ali", "Mustafa", "Emre", "Kerem", "Burak",
            "Arda", "Hakan", "Serdar", "Ozan", "Caner", "Mert", "Taylan",
            "Berkay", "Dorukhan", "Ismail", "Yunus", "Salih", "Okay"
        };
        FootballPosition[] positions = {
            FootballPosition.GK,
            FootballPosition.CB, FootballPosition.CB,
            FootballPosition.LB, FootballPosition.RB,
            FootballPosition.CM, FootballPosition.CM, FootballPosition.CM,
            FootballPosition.LW, FootballPosition.RW,
            FootballPosition.ST
        };

        java.util.List<String> namePool = new java.util.ArrayList<>(java.util.Arrays.asList(allNames));
        java.util.Collections.shuffle(namePool);

        java.util.List<FootballPlayer> players = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            int r = 65 + (int)(Math.random() * 20);
            FootballPlayer player = new FootballPlayer(
                namePool.get(i),
                18 + (int)(Math.random() * 15),
                positions[i],
                r, r, r, r, r, r,
                positions[i] == FootballPosition.GK ? r : 30
            );
            team.addPlayer(player);
            players.add(player);
        }
        team.setStartingEleven(new java.util.ArrayList<>(players));
    }
}