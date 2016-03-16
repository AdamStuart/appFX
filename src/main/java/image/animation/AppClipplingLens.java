package image.animation;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * original class name was "DetectiveGlass"
 * @author Narayan G.M
 */
public class AppClipplingLens extends Application {

    public static void main(String[] args) {
        Application.launch(AppClipplingLens.class, args);
    }

    @Override  public void start(Stage primaryStage) {
        primaryStage.setTitle("Detective Glass");
        Group root = new Group();
      
        ImageView maskView = new ImageView();		  //ImageView
        maskView.setCursor(Cursor.NONE);
        Image image = new Image(AppClipplingLens.class.getResourceAsStream("cain.png"));
        Scene scene = new Scene(root, image.getWidth(), image.getHeight(),Color.GRAY);
       
        maskView.setImage(image);
        final Circle glass = new Circle(100,100,100);		  //Mask Shape
        maskView.setClip(glass);
        maskView.setOnMouseMoved(event ->{ glass.setCenterX(event.getX());   glass.setCenterY(event.getY());     });//Setting X and Y position of mask shape

        root.getChildren().add(maskView);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}