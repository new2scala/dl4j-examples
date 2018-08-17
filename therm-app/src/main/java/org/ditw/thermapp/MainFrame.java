package org.ditw.thermapp;

import akka.stream.impl.fusing.Fold;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.ditw.thermapp.onedrive.HttpRespHandler;
import org.ditw.thermapp.onedrive.HttpRespHandlerT;
import org.ditw.thermapp.onedrive.HttpHelper;
import org.ditw.thermapp.onedrive.Requests;
import scala.Option;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class MainFrame extends Application {

    private ImageView _image;
    private BorderPane borderPane;

    private ContextMenu _driveTreeContextMenu;

    private ContextMenu createDriveTreeContextMenu() {
        MenuItem item = new MenuItem("cache");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MenuItem src = (MenuItem)event.getSource();
                FolderItem folder = (FolderItem)src.getUserData();

                if (folder.isRoot()) {
                    System.out.println("Start caching root ...");
                    Requests.reqCacheSharedFolderRootItems();
                }
                else if (folder.isFolder()) {
                    // todo: handle completion
                    System.out.println("Start caching ...");
                    Requests.reqCacheSharedFolderItems(folder.id());
                }
            }
        });
        ContextMenu menu = new ContextMenu(item);
        return menu;
    }

    private Node folderIcon() {
        return new ImageView(
            new Image(getClass().getResourceAsStream("/folder-24.png"))
        );
    }
    private Node fileIcon() {
        return new ImageView(
            new Image(getClass().getResourceAsStream("/file-24.png"))
        );
    }

    @Override
    public void start(Stage primaryStage) {
        initData();

        primaryStage.setScene(
            new Scene(
                layout()
            )
        );
        //updateData(TestDataHelpers.mockDataSource().curr());

        _driveTreeContextMenu = createDriveTreeContextMenu();

        primaryStage.show();
    }

    private BorderPane layout() {
        borderPane = new BorderPane();

        borderPane.setTop(controlBar());
        borderPane.setRight(image());
        borderPane.setCenter(thermoGrid());

        borderPane.setLeft(AuthHelper.createWebClient());

//        _folderNav = new FolderNav(
//            new File("/media/sf_vmshare/Icons64")
//        );
        return borderPane;
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

//    private void createFileList(FolderResp folder) {
//        VBox vb = new VBox();
//        ListView<FolderItem> folderItems = new ListView<>();
//        folderItems.getItems().addAll(folder.value());
//
//        folderItems.getSelectionModel().selectedItemProperty().addListener(
//            new ChangeListener<FolderItem>() {
//                @Override
//                public void changed(ObservableValue<? extends DataHelpers.FolderItem> observable, DataHelpers.FolderItem oldValue, DataHelpers.FolderItem newValue) {
////                    String downloadUrl = newValue.$atmicrosoft$u002Egraph$u002EdownloadUrl();
////                    OneDriveHelpers.download(downloadUrl, _image);
//                }
//            }
//        );
//
//        vb.getChildren().add(folderItems);
//        borderPane.setLeft(vb);
//    }

    private final HttpRespHandlerT<FolderItem[]> folderItemsHandler = new HttpRespHandlerT<FolderItem[]>() {
        public FolderItem[] handle(HttpResponse resp) {
            FolderItem[] items = HttpHelper.handleFolderItems(resp);
            TreeItem<FolderItem> currItem = _driveTree.getSelectionModel().getSelectedItem();
            Set<String> children = new HashSet<>(currItem.getChildren().size());
            for (TreeItem<FolderItem> child : currItem.getChildren()) {
                children.add(child.getValue().name());
            }
            List<FolderItem> filtered = new ArrayList<>(items.length);
            for (FolderItem item : items) {
                if (!children.contains(item.name())) {
                    filtered.add(item);
                }
            }
            populateFolderItems(currItem, filtered);
            return items;
        }
    };

    private void createDriveTree() {
        TreeItem<FolderItem> rootItem = new TreeItem<>(FolderItems.ROOT(), folderIcon());
        _driveTree = new TreeView<>(rootItem);
        _driveTree.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<TreeItem<FolderItem>>() {
                @Override
                public void changed(ObservableValue<? extends TreeItem<FolderItem>> observable, TreeItem<FolderItem> oldValue, TreeItem<FolderItem> newValue) {
                    if (newValue.getValue().isFolder()) {
                        currData = TestDataHelpers.getSelectedDataSource(newValue.getValue());
                        //Requests.reqFolderItems(newValue.getValue().id(), folderItemsHandler);
                        Requests.reqSharedFolderItems(newValue.getValue().id(), folderItemsHandler);
                    }
                }
            }
        );
        borderPane.setLeft(_driveTree);

        _driveTree.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                for (MenuItem item : _driveTreeContextMenu.getItems()) {
                    item.setUserData(_driveTree.getSelectionModel().getSelectedItem().getValue());
                }
                _driveTreeContextMenu.show(_driveTree, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void populateFolderItems(TreeItem<FolderItem> currItem, List<FolderItem> items) {
        //TreeItem<FolderItem> root = _driveTree.getRoot();
        for (FolderItem item : items) {
            TreeItem<FolderItem> trItem = new TreeItem<>(item, item.isFolder() ? folderIcon() : fileIcon());
            currItem.getChildren().add(trItem);
        }
    }
    private final HttpRespHandler rootDriveHandler = new HttpRespHandler() {
        @Override
        public void handle(HttpResponse resp) {
            FolderItem[] items = HttpHelper.handleDriveRoot(resp);
            populateFolderItems(_driveTree.getRoot(), Arrays.asList(items));
        }
    };

    private TreeView<FolderItem> _driveTree;

    private DataHelpers.DataSource currData = null;
    Slider _slider;
    private HBox controlBar() {

        Button loadButton = new Button("Open");

        loadButton.setOnAction(evt -> {
            createDriveTree();
            Requests.reqRootSharedFolder(rootDriveHandler);
            //Requests.reqDriveRoot(rootDriveHandler);
            //DataHelpers.OneDriveFolderResp folder = OneDriveHelpers.testToken(_image);
            //createFileList(folder);
            //folder.value()
        });
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setPrefWidth(600);
        slider.setMax(100);
        slider.setValue(40);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(5);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(10);
        _slider = slider;
        Button leftButton = new Button("<");
        leftButton.setOnAction(evt -> {
            TreeItem<FolderItem> selected = _driveTree.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue().isFolder() && currData != null) {
                Option<DataHelpers.DataUnit> d =
                    currData.prev();
                if (d.isDefined()) {
                    updateData(d.get());
                }
            }
        });
        Button rightButton = new Button(">");
        rightButton.setOnAction(evt -> {
            TreeItem<FolderItem> selected = _driveTree.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue().isFolder() && currData != null) {
                Option<DataHelpers.DataUnit> d =
                    currData.next();
                if (d.isDefined()) {
                    updateData(d.get());
                }
            }

        });
        Button playButton = new Button(">>");
        playButton.setOnAction(evt -> {
            TreeItem<FolderItem> selected = _driveTree.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue().isFolder() && currData != null) {
                currData.play(() -> {
                    updateData(currData.curr());
                });
            }
        });

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
