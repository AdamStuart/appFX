package diagrams;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;

// java 8 code.
//http://stackoverflow.com/questions/23258605/javafx-how-can-i-best-place-a-label-centered-in-a-shape

public class Circular extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Text   text   = createText("Xyzzy");
        Circle circle = encircle(text);
        StackPane layout = new StackPane();
        layout.getChildren().addAll(circle,  text );
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout));
        stage.show();
    }

    private Text createText(String string) {
        Text text = new Text(string);
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setStyle(
                "-fx-font-family: \"Times New Roman\";" +
                "-fx-font-style: italic;" +
                "-fx-font-size: 48px;"
        );

        return text;
    }

    private Circle encircle(Text text) {
        Circle circle = new Circle();
        circle.setFill(Color.ORCHID);
        final double PADDING = 10;
        circle.setRadius(getWidth(text) / 2 + PADDING);

        return circle;
    }

    private double getWidth(Text text) {
        new Scene(new Group(text));
        text.applyCss();

        return text.getLayoutBounds().getWidth();
    }
}