package image.animation;

import javafx.application.Application;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import util.UtilTransitions.Transition;
import animation.Transitions;

public class AppTransitionTester extends Application
{
    private Pane              pane  = new Pane();
    int i = 0;
	   // ******************** ApplicationStart **********************************
    @Override public void start(Stage stage) {

        Scene scene = new Scene(pane);
        scene.setFill(Color.rgb(50, 50, 50));
        scene.setCamera(new PerspectiveCamera());
        Image front =  new Image(getClass().getResource("front.png").toExternalForm());
        Image back =  new Image(getClass().getResource("back.png").toExternalForm());
        Transitions transit = new Transitions(front, back);
        pane.getChildren().setAll(transit.getTiles());
    	pane.setPrefSize(front.getWidth(), front.getHeight());

        scene.setOnMousePressed(event -> transit.play(i++));
        scene.setOnKeyPressed(event -> transit.play(i++));

        stage.setTitle("Image Transition effect");
        stage.setScene(scene);
        stage.show();
        transit.play(i++);
    	if (i >= Transition.values().length) i = 0;
       
    }

    public static void main(String[] args) {        launch(args);    }


}
