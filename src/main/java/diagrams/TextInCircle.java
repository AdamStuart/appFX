package diagrams;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.util.Duration;
//http://stackoverflow.com/questions/17437411/how-to-put-a-text-into-a-circle-object-to-display-it-from-circles-center
public class TextInCircle extends Application {
    public static void main(String[] args) throws Exception { launch(args); }

    private static final int R = 150;
    private static final Color lineColor = Color.FIREBRICK.deriveColor(0, 1, 1, .6);

    @Override
    public void start(final Stage stage) throws Exception {
        final Circle circle = createCircle();
        final Text   text   = createText();

        final Line l1 = createLine(lineColor, 0, R - 0.5, 2 * R, R - 0.5);
        final Line l2 = createLine(lineColor, R - 0.5, 0, R - 0.5, 2 * R);

//        Group group = new Group(circle, text, l1 , l2);
        Group group = new Group(circle, l1 , l2);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(group, text);

        stage.setScene(new Scene(stack));
        stage.show();

        animateText(text);
    }

    private Circle createCircle() {
        final Circle circle = new Circle(R);

        circle.setStroke(Color.FORESTGREEN);
        circle.setStrokeWidth(10);
        circle.setStrokeType(StrokeType.INSIDE);
        circle.setFill(Color.AZURE);
        circle.relocate(0, 0);

        return circle;
    }

    private Line createLine(Color lineColor, double x1, double y1, double x2, double y2) {
        Line l1 = new Line(x1, y1, x2, y2);

        l1.setStroke(lineColor);
        l1.setStrokeWidth(1);

        return l1;
    }

    private Text createText() {
        final Text text = new Text("A");

        text.setFont(new Font(30));
        text.setBoundsType(TextBoundsType.VISUAL);
//        centerText(text);

        return text;
    }

    private void centerText(Text text) {
        double W = text.getBoundsInLocal().getWidth();
        double H = text.getBoundsInLocal().getHeight();
        text.relocate(R - W / 2, R - H / 2);
    }

    private void animateText(final Text text) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                char newValue = (char) ((text.getText().toCharArray()[0] + 1) % 123);
                if (newValue == 0) newValue = 'A';
                text.setText("" + newValue);
//                centerText(text);
            }
        }));
        timeline.setCycleCount(1000);
        timeline.play();
    }
}