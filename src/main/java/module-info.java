module com.sportsmanager {

    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports com.sportsmanager;
    exports com.sportsmanager.domain.sport;
    exports com.sportsmanager.domain.team;
    exports com.sportsmanager.domain.league;
    exports com.sportsmanager.domain.simulation;
    exports com.sportsmanager.domain.session;
    exports com.sportsmanager.application;
    exports com.sportsmanager.football;
    exports com.sportsmanager.volleyball;
    exports com.sportsmanager.ui;

    opens com.sportsmanager to javafx.fxml;
    opens com.sportsmanager.ui to javafx.base, javafx.fxml;
    opens com.sportsmanager.domain.session to com.google.gson;
    opens com.sportsmanager.football to com.google.gson;
    opens com.sportsmanager.domain.league to com.google.gson;
    opens com.sportsmanager.domain.team to com.google.gson;
    opens com.sportsmanager.domain.simulation to com.google.gson;
}