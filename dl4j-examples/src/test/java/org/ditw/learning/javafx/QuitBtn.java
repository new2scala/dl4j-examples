package org.ditw.learning.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class QuitBtn extends Application {

    @Override
    public void start(Stage stage) {
        initUI(stage);
    }

    public void initUI(Stage stage) {
        Button btn = new Button();
        btn.setText("Quite");
        btn.setOnAction( evt -> {
            Platform.exit();
        });

        Image img = new Image("file:///media/sf_vmshare/lena.png");
        ImageView iv = new ImageView(img);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(25));
        hbox.getChildren().add(btn);
        hbox.getChildren().add(iv);

        stage.setTitle("Quit");

        stage.setScene(new Scene(hbox, 600, 400));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
