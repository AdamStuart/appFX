/*
 * This code is completely free of any restrictions on usage.
 *
 * Feel free to study it, modify it, redistribute it and even claim it as your own if you like!
 *
 * Courtesy of Bembrick Software Labs in the interest of promoting JavaFX.
 */
package image.pixelator;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * <code>PixelatorTest</code> is the main program to test image pixelation with JavaFX utilising
 * lambdas.
 *
 * @author Felix Bembrick (@Lucan1d)
 * @version 1.0 September 2013
 */
public class PixelatorTest extends Application {

	/**
	 * The minimum value for the slider.
	 */
	private static final int SLIDER_MIN = 0;

	/**
	 * The maximum value for the slider.
	 */
	private static final int SLIDER_MAX = 30;

	/**
	 * The button label when it is acting as the start button.
	 */
	private static final String START_LABEL = "Start";

	/**
	 * The button label when it is acting as the pause button.
	 */
	private static final String PAUSE_LABEL = "Pause";

	/**
	 * The image to be pixelated.
	 */
	private static final Image IMAGE = new Image(PixelatorTest.class.getResourceAsStream("Quoll.jpg"));

	/**
	 * This is the program's entry point and launches the application.
	 *
	 * @param args An array of arguments (usually specified on the command line).
	 */
	public static void main(final String[] args) {
		Application.launch(args);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(final Stage stage) {
		stage.setTitle("Pixelator Tester");

		// Create a pane to act as the root of the scene graph.
		final BorderPane root = new BorderPane();

		// Create a writable image that we will manipulate to perform the pixelation.
		final WritableImage buffer = new WritableImage(IMAGE.getPixelReader(), (int) IMAGE.getWidth(), (int) IMAGE.getHeight());

		// Create a slider control to adjust the level of pixelation.
		final Slider slider = createSlider(buffer);

		// Create a timeline to automate pixelation.
		final Timeline timeline = createTimeline(slider);

		// Create the panes used in the UI and add them to the root pane.
		root.setTop(createButtonPane(timeline));
		root.setCenter(createImagePane(buffer));
		root.setBottom(createSliderPane(slider));

		// Create a new scene with the border pane as the root of the scene graph.
		final Scene scene = new Scene(root);

		// Show the stage.
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Creates the
	 * <code>Button</code> used to start and pause the animation. The button will either start the
	 * animation or pause it.
	 */
	private Button createButton(final Timeline timeline) {
		final Button result = new Button(START_LABEL);

		// Use lambdas to assign the handler for the "action" event.
		result.setOnAction(e -> {
			if (timeline.getStatus() == Animation.Status.RUNNING) {
				timeline.pause();
				result.setText(START_LABEL);
			} else {
				timeline.play();
				result.setText(PAUSE_LABEL);
			}
		});

		return result;
	}

	/**
	 * Creates a
	 * <code>Slider</code> to allow manual control over the level of pixelation.
	 */
	private Slider createSlider(final WritableImage wi) {
		final Slider result = new Slider();
		result.setMin(SLIDER_MIN);
		result.setMax(SLIDER_MAX);
		result.setMajorTickUnit(5);
		result.setShowTickLabels(true);
		result.setShowTickMarks(true);
		result.setSnapToTicks(true);

		// Use lambdas to bind pixelation of the image to a change in the value of the slider.
		result.valueProperty().addListener((observable, oldValue, newValue) -> {
			Pixelator.pixelate(wi, IMAGE, newValue.intValue());
		});

		return result;
	}

	/**
	 * Creates a
	 * <code>Timeline</code> to control the animation of pixelation. The timeline will oscillate the
	 * value of the slider from minimum to maximum and back again.
	 */
	private Timeline createTimeline(final Slider slider) {
		final Timeline result = new Timeline();
		result.setAutoReverse(true);
		result.setCycleCount(Animation.INDEFINITE);
		result.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(slider.valueProperty(), SLIDER_MIN)),
		 new KeyFrame(new Duration(1000), new KeyValue(slider.valueProperty(), SLIDER_MAX)));

		return result;
	}

	/**
	 * Creates a
	 * <code>Node</code> which is the pane to contain the button.
	 */
	private Node createButtonPane(final Timeline timeline) {
		final TilePane result = new TilePane();
		result.setAlignment(Pos.CENTER);
		result.setPrefColumns(1);
		result.setHgap(12);
		result.setVgap(12);
		result.setPadding(new Insets(12));
		result.getChildren().addAll(createButton(timeline));

		return result;
	}

	/**
	 * Creates a
	 * <code>Node</code> which is the pane to contain the slider.
	 */
	private Node createSliderPane(final Slider slider) {
		final BorderPane result = new BorderPane();
		result.setPadding(new Insets(12, 12, 8, 12));
		result.setCenter(slider);

		return result;
	}

	/**
	 * Creates a
	 * <code>Node</code> which is the pane to contain the image.
	 */
	private Node createImagePane(final WritableImage buffer) {
		return new ImageViewPane(new ImageView(buffer));
	}
}
