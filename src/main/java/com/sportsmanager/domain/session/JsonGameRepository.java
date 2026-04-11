package com.sportsmanager.domain.session;

import java.io.*;
import java.util.Optional;

public class JsonGameRepository implements GameRepository {

    private static final String FILE_PATH = "gamesession.json";

    @Override
    public void save(GameSession session) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            writer.write("{\n");
            writer.write("  \"currentWeek\": " + session.getCurrentWeek() + ",\n");
            writer.write("  \"season\": " + session.getSeason() + "\n");
            writer.write("}");
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    @Override
    public Optional<GameSession> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return Optional.empty();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            GameSession session = new GameSession();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"currentWeek\"")) {
                    int val = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    session.setCurrentWeek(val);
                } else if (line.startsWith("\"season\"")) {
                    int val = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    session.setSeason(val);
                }
            }
            return Optional.of(session);
        } catch (IOException e) {
            System.err.println("Load failed: " + e.getMessage());
            return Optional.empty();
        }
    }
}