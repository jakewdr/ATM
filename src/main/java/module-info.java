module com.atmbanksimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.atmbanksimulator to javafx.fxml;
    exports com.atmbanksimulator;
}