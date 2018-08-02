package org.ditw.learning.javafx

import javafx.application.{Application, Platform}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.stage.Stage

class Qbtn extends Application {

  override def start(stage:Stage):Unit = {
    initUI(stage:Stage)
  }

  val evth:EventHandler[ActionEvent] = new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent): Unit = {
      Platform.exit()
    }
  }

  private def initUI(stage:Stage) = {
    val btn = new Button()
    btn.setText("Quit")
    //btn.setOnAction(evth)

    val hbox = new HBox()
    hbox.setPadding(new Insets(25))
    hbox.getChildren.add(btn)

    stage.setTitle("Qbtn")
    stage.setScene(new Scene(hbox, 280, 200))
    stage.show()
  }

  def main(args:Array[String]):Unit = {
    Application.launch()
  }
}
