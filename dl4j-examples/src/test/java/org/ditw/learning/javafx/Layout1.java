package org.ditw.learning.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Layout1 extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane bp = new BorderPane();
        HBox hb = hbox();
        addStackPane2HBox(hb);
        bp.setTop(hb);
        bp.setLeft(vbox());

        primaryStage.setScene(new Scene(bp));
        primaryStage.show();
    }

    private void addStackPane2HBox(HBox hbox) {
        StackPane sp = new StackPane();
        Rectangle rect = new Rectangle(30.0, 25.0);
        rect.setFill(
            new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop[] {
                    new Stop(0, Color.BLUE),
                    new Stop(0.5, Color.WHITE),
                    new Stop(1, Color.CYAN)
                }
            )
        );
        rect.setStroke(Color.WHEAT);
        rect.setArcHeight(3.5);
        rect.setArcWidth(3.5);

        Text help = new Text("?");
        help.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        help.setFill(Color.WHITE);
        help.setStroke(Color.WHEAT);

        sp.getChildren().addAll(rect, help);

        sp.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setMargin(help, new Insets(0, 10, 0,0));

        hbox.getChildren().add(sp);
        HBox.setHgrow(sp, Priority.ALWAYS);

    }

    private HBox hbox() {
        HBox res = new HBox();

        res.setPadding(new Insets(15, 12, 15, 12));
        res.setSpacing(10);

        res.setStyle("-fx-background-color: #336699");

        Button btn = new Button("Btn");
        btn.setPrefSize(100, 20);

        Button btn2 = new Button("Btn 2");
        btn2.setPrefSize(100, 20);

        res.getChildren().addAll(btn, btn2);
        return res;
    }

    private VBox vbox() {
        VBox res = new VBox();

        res.setPadding(new Insets(10));

        res.setSpacing(8);

        Text title = new Text("Layout test");

        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        res.getChildren().add(title);

        Hyperlink options[] = new Hyperlink[] {
            new Hyperlink("Sales"),
            new Hyperlink("Marketing"),
            new Hyperlink("Distribution"),
            new Hyperlink("Costs")};

        res.getChildren().addAll(options);

        return res;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
