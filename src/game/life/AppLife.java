package game.life;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//http://www.codeproject.com/Articles/1043443/The-Game-Of-Life-Advanced-Style-of-Programmation
public class AppLife extends Application {
    public static void main(String[] args)    {        launch(args);    }

    @Override  public void start (Stage stage) {
        GameOfLifeView golv =  new GameOfLifeView();
        StackPane pane = new StackPane();
        pane.getChildren().add(golv);
        int gameSize = GameOfLifeView.getPixelSize();

        stage.setTitle("GameOfLife in JavaFX");
        stage.setScene(new Scene(pane, gameSize+10, gameSize + 100));
        stage.show();
        }
}