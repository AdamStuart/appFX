package image;

import javafx.application.Application;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AppDoodle extends Application {

    private Path path;
    private Group lineGroup;
    private static final Double DEFAULTSTROKE = 3.0, MAXSTROKE = 30.0,  MINSTROKE = 1.0;
    private static final Integer DEFAULTRED = 0, DEFAULTGREEN = 0, DEFAULTBLUE = 255;
    private static final Integer MAXRGB = 255, MINRGB = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        Application.launch(args);    }

    @Override    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Tool");
        final Group root = new Group();

        Scene scene = new Scene(root, 300, 400);

        // A group to hold all the drawn path elements
        lineGroup = new Group();


        // Build the slider, label, and button and their VBox layout container 
        Button btnClear = new Button();
        btnClear.setText("Clear");
        btnClear.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                lineGroup.getChildren().removeAll(lineGroup.getChildren());
            }
        });

        Slider strokeSlider = new Slider(MINSTROKE, MAXSTROKE, DEFAULTSTROKE);
        Label labelStroke = new Label("Stroke Width");
        VBox utilBox = new VBox(10);
        utilBox.setAlignment(Pos.TOP_CENTER);
        utilBox.getChildren().addAll(btnClear, labelStroke, strokeSlider);

        // Build the RGB sliders, labels, and HBox containers
        final Slider redSlider = new Slider(MINRGB, MAXRGB, DEFAULTRED);
        Label labelRed = new Label("R");
        HBox rhbox = new HBox(5);
        rhbox.getChildren().addAll(labelRed, redSlider);
        final Slider greenSlider = new Slider(MINRGB, MAXRGB, DEFAULTGREEN);
        Label labelGreen = new Label("G");
        HBox ghbox = new HBox(5);
        ghbox.getChildren().addAll(labelGreen, greenSlider);
        final Slider blueSlider = new Slider(MINRGB, MAXRGB, DEFAULTBLUE);
        Label labelBlue = new Label("B");
        HBox bhbox = new HBox(5);
        bhbox.getChildren().addAll(labelBlue, blueSlider);

        // Build the VBox container for all the slider containers        
        VBox colorBox = new VBox(10);
        colorBox.setAlignment(Pos.TOP_CENTER);
        colorBox.getChildren().addAll(rhbox, ghbox, bhbox);

        // Put all controls in one HBox
        HBox toolBox = new HBox(10);
        toolBox.setAlignment(Pos.TOP_CENTER);
        toolBox.getChildren().addAll(utilBox, colorBox);

        // Build a Binding object to compute a Paint object from the sliders
        ObjectBinding<Paint> colorBinding = new ObjectBinding<Paint>() {
            {
                super.bind(redSlider.valueProperty(), greenSlider.valueProperty(), blueSlider.valueProperty());
            }
            @Override  protected Paint computeValue() {
                return Color.rgb(redSlider.valueProperty().intValue(),
                        greenSlider.valueProperty().intValue(), blueSlider.valueProperty().intValue());
            }
        };

        // Build the sample line and its layout container
        final Line sampleLine = new Line(0, 0, 140, 0);
        sampleLine.strokeWidthProperty().bind(strokeSlider.valueProperty());
        StackPane stackpane = new StackPane();
        stackpane.setPrefHeight(MAXSTROKE);
        stackpane.getChildren().add(sampleLine);
        // Bind to the Paint Binding object
        sampleLine.strokeProperty().bind(colorBinding);
    

        // Build the canvas
        final Rectangle canvas = new Rectangle(scene.getWidth() - 20, scene.getHeight() - 200);
        canvas.setCursor(Cursor.CROSSHAIR);
        canvas.setFill(Color.LIGHTGRAY);
        canvas.setOnMousePressed(me ->  {
                path = new Path();
                path.setMouseTransparent(true);
                path.setStrokeWidth(sampleLine.getStrokeWidth());
                path.setStroke(sampleLine.getStroke());
                lineGroup.getChildren().add(path);
                path.getElements().add(new MoveTo(me.getSceneX(), me.getSceneY()));
        });

        canvas.setOnMouseReleased(me ->  {   path = null; });

        canvas.setOnMouseDragged(me -> {
                // keep lines within rectangle
                if (canvas.getBoundsInLocal().contains(me.getX(), me.getY())) 
                    path.getElements().add(new LineTo(me.getSceneX(), me.getSceneY()));
        });

        // Build the VBox container for the toolBox, sampleline, and canvas
        VBox vb = new VBox(20);
        vb.setPrefWidth(scene.getWidth() - 20);
        vb.setLayoutY(20);
        vb.setLayoutX(10);
        vb.getChildren().addAll(toolBox, stackpane, canvas);
        root.getChildren().addAll(vb, lineGroup);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}