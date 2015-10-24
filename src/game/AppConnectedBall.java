package game;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
// a separate small app (gist) to show binding two shapes together with a line

//https://gist.github.com/skrb/4335101

class Ball extends Circle {
    private double dragBaseX, dragBaseY;
 
    public Ball(double centerX, double centerY, double radius) {
        super(centerX, centerY, radius);
        setFill(Color.RED);
        setOnMousePressed(event -> {
                dragBaseX = event.getSceneX() - getCenterX();
                dragBaseY = event.getSceneY() - getCenterY();
        });
        setOnMouseDragged(event -> {
                setCenterX(event.getSceneX() - dragBaseX);
                setCenterY(event.getSceneY() - dragBaseY);
        });
    }
}
 
class Connection extends Line 
{
    public Connection(Ball startBall, Ball endBall) {
        startXProperty().bind(startBall.centerXProperty());
        startYProperty().bind(startBall.centerYProperty());        
        endXProperty().bind(endBall.centerXProperty());
        endYProperty().bind(endBall.centerYProperty());        
    }
}
 
public class AppConnectedBall extends Application 
{
    @Override public void start(Stage stage) throws Exception {
        Group root = new Group();
 
        Ball ball1 = new Ball(100, 200, 20); 	
        Ball ball2 = new Ball(300, 200, 20); 
 
        Connection connection = new Connection(ball1, ball2);
        connection.setStroke(Color.CYAN);
        connection.setStrokeWidth(5);
        root.getChildren().addAll(connection, ball1, ball2);
 
        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String... args) {
        launch(args);
    }
}