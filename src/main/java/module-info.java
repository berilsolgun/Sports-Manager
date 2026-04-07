module com.sportsmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.sportsmanager.domain.sport to javafx.fxml;
    opens com.sportsmanager.domain.team to javafx.fxml;

    exports com.sportsmanager;
    exports com.sportsmanager.domain.sport;
    exports com.sportsmanager.domain.team;
    exports com.sportsmanager.domain.league;
    exports com.sportsmanager.domain.simulation;
    exports com.sportsmanager.domain.session;
}