module com.codedotorg {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;

    opens com.codedotorg to javafx.fxml;
    exports com.codedotorg;
}
