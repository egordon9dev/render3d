package com.ethan.render3D;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
      stage.setTitle("Hello World");
      stage.setScene(new Scene(), 700, 700);
      stage.show();
  }
  public static void main(String[] args) { Application.launch(args); }
}
