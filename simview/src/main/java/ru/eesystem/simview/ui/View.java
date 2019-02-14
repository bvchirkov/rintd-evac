package ru.eesystem.simview.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class View {

    private static AnchorPane rootPane;

    public static AnchorPane getMainVindow() {
        rootPane = new AnchorPane();

        Pane fLeftPane = new Pane();
        Pane fCenterPane = new Pane();

        Pane fRightPane = new Pane();

        Label fTimeLabelText = new Label("Время:");
        HBox fBottomBar = new HBox(fTimeLabelText);
        BorderPane fBorderPane = new BorderPane();
        fBorderPane.setBottom(fBottomBar);
        fBorderPane.setCenter(fCenterPane);
        fBorderPane.setLeft(fLeftPane);
        fBorderPane.setLeft(fRightPane);

        rootPane.getChildren().addAll(fBorderPane);
        return rootPane;
    }
}
