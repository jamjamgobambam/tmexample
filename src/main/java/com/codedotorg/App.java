package com.codedotorg;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage primaryStage) {
        // ModelDemo example = new ModelDemo();
        BinarySearch example = new BinarySearch();
        example.startApp(primaryStage);
    }

}