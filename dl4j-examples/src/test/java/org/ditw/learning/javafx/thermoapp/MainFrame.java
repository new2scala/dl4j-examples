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
import org.bytedeco.javacv.FrameFilter;
import org.ditw.learning.akkastr.FolderNav;
import org.ditw.learning.thermoapp.DataHelpers;
import org.ditw.learning.thermoapp.TestDataHelpers;
import scala.Option;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
        updateData(TestDataHelpers.mockDataSource().curr());

        primaryStage.show();
    }

    private BorderPane layout() {
        BorderPane bp = new BorderPane();

        bp.setTop(controlBar());
        bp.setRight(image());
        bp.setLeft(thermoGrid());

        bp.setCenter(AuthHelper.createWebClient());

//        _folderNav = new FolderNav(
//            new File("/media/sf_vmshare/Icons64")
//        );
        return bp;
    }

    private StackPane image() {
        _image = new ImageView(
            //new Image("file:///media/sf_vmshare/lena.png")
        );
        StackPane sp = new StackPane();
        sp.setPadding(new Insets(10));
        sp.getChildren().add(_image);
        return sp;
    }

    private double[][] _thermoGridData;
    private int _thermoGridWidth;
    private int _thermoGridHeight;
    private void initData() {
        _thermoGridWidth = 8;
        _thermoGridHeight = 8;
//        _thermoGridData = new double[_thermoGridHeight][];
//
//        for (int i = 0; i < _thermoGridHeight; i ++) {
//            _thermoGridData[i] = new double[_thermoGridWidth];
//            for (int j = 0; j < _thermoGridWidth; j++) {
//                _thermoGridData[i][j] = 0.0;
//            }
//        }
    }

    private final static int _ThermoGridCellSize = 36;

    private Rectangle[][] rects;

    private GridPane thermoGrid() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setVgap(10);
        gp.setHgap(10);

        rects = new Rectangle[_thermoGridHeight][];
        for (int i = 0; i < _thermoGridHeight; i ++) {
            rects[i] = new Rectangle[_thermoGridWidth];
            for (int j = 0; j < _thermoGridWidth; j++) {
                Rectangle rect =
                    new Rectangle(_ThermoGridCellSize, _ThermoGridCellSize);
//                String color = ThermoColorHelper.thermoVal2Color(_thermoGridData[i][j]);
//                rect.setFill(Color.web(color));
                gp.add(rect, j, i);
                rects[i][j] = rect;
            }
        }

        return gp;
    }

    //private FolderNav _folderNav;

    private void updateData(DataHelpers.DataUnit d) {
        double[][] thermoData = d.thermoGrid();
        for (int i = 0; i < thermoData.length; i++) {
            for (int j = 0; j < thermoData[i].length; j++) {
                String color = ThermoColorHelper.thermoVal2Color(thermoData[i][j]);
                rects[i][j].setFill(Color.web(color));
            }
        }

        byte[] imageRaw = d.imageRaw();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageRaw);
        _image.setImage(new Image(bis));
        try {
            bis.close();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private HBox controlBar() {

        Button loadButton = new Button("Open");

        loadButton.setOnAction(evt -> {
            //OneDriveHelpers.testToken();
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
        leftButton.setOnAction(evt -> {
            Option<DataHelpers.DataUnit> d = TestDataHelpers.mockDataSource().prev();
            if (d.isDefined()) {
                updateData(d.get());
            }
//            _image.setImage(
//                new Image("file://" + _folderNav.currUrl())
//            );
        });
        Button rightButton = new Button(">");
        rightButton.setOnAction(evt -> {
            Option<DataHelpers.DataUnit> d = TestDataHelpers.mockDataSource().next();
            if (d.isDefined()) {
                updateData(d.get());
            }
//            _folderNav.next();
//            _image.setImage(
//                new Image("file://" + _folderNav.currUrl())
//            );
        });
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
