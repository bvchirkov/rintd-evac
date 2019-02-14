package ru.eesystem.simview.ui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final String RESOURCE = "main.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        /*Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);*/
        Scene scene = new Scene(View.getMainVindow(), 400, 300);

        stage.setTitle("My JavaFX Application");
        stage.setScene(scene);
        stage.show();
    }
}
