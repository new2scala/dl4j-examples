package org.ditw.learning.javafx.thermoapp;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.*;

public class MainFrame extends Application {

    private ImageView _image;

    @Override
    public void start(Stage primaryStage) {
        initData();

        primaryStage.setScene(
            new Scene(
                layout()
            )
        );

        primaryStage.show();
    }

    private BorderPane layout() {
        BorderPane bp = new BorderPane();

        bp.setTop(controlBar());
        bp.setRight(image());
        bp.setLeft(thermoGrid());

        bp.setCenter(AuthHelper.createWebClient());
        return bp;
    }

    private StackPane image() {
        _image = new ImageView(
            new Image("file:///media/sf_vmshare/lena.png")
        );
        StackPane sp = new StackPane();
        sp.setPadding(new Insets(10));
        sp.getChildren().add(_image);
        return sp;
    }

    private Double[][] _thermoGridData;
    private int _thermoGridWidth;
    private int _thermoGridHeight;
    private void initData() {
        _thermoGridWidth = 8;
        _thermoGridHeight = 8;
        _thermoGridData = new Double[_thermoGridHeight][];

        for (int i = 0; i < _thermoGridHeight; i ++) {
            _thermoGridData[i] = new Double[_thermoGridWidth];
            for (int j = 0; j < _thermoGridWidth; j++) {
                _thermoGridData[i][j] = -30.0;
            }
        }
    }

    private final static int _ThermoGridCellSize = 24;

    private GridPane thermoGrid() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setVgap(10);
        gp.setHgap(10);

        for (int i = 0; i < _thermoGridHeight; i ++) {
            for (int j = 0; j < _thermoGridWidth; j++) {
                Rectangle rect =
                    new Rectangle(_ThermoGridCellSize, _ThermoGridCellSize);
                String color = ThermoColorHelper.thermoVal2Color(_thermoGridData[i][j]);
                rect.setFill(Color.web(color));
                gp.add(rect, j, i);
            }
        }

        return gp;
    }

    private HBox controlBar() {

        Button loadButton = new Button("Open");

        loadButton.setOnAction(evt -> {
            OneDriveHelpers.testToken();
        });
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(40);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(5);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(10);
        Button leftButton = new Button("<");
        Button rightButton = new Button(">");
        Button playButton = new Button(">>");

        HBox res = new HBox();

        res.setSpacing(10);
        res.setPadding(new Insets(10));
        res.getChildren().addAll(
            loadButton,
            slider,
            leftButton,
            rightButton,
            playButton
        );
        return res;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
