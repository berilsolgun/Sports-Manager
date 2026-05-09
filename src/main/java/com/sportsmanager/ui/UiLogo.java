package com.sportsmanager.ui;

import com.sportsmanager.domain.sport.Sport;
import javafx.scene.image.Image;

import java.net.URL;

/** Loads team logos from classpath {@code /com/sportsmanager/data/{sport}/logos/}. */
public final class UiLogo {

    private UiLogo() {
    }

    public static Image loadTeamLogo(Sport sport, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String folder = sport.getName().equalsIgnoreCase("Volleyball") ? "volleyball" : "football";
        String path = "/com/sportsmanager/data/" + folder + "/logos/" + fileName;
        URL url = UiLogo.class.getResource(path);
        if (url == null) {
            return null;
        }
        return new Image(url.toExternalForm(), true);
    }
}
