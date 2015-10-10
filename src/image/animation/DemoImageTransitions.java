package image.animation;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.SpringInterpolator;

/**
 * Created by
 * User: hansolo
 * Date: 18.06.13
 * Time: 16:59
 */
public class DemoImageTransitions extends Application {
    public static enum Transition {
        VERTICAL_AROUND_X,
        VERTICAL_AROUND_Y,
        VERTICAL_AROUND_X_AND_Y,
        HORIZONTAL_AROUND_X,
        HORIZONTAL_AROUND_Y,
        HORIZONTAL_AROUND_X_AND_Y,
        RECTANGULAR_AROUND_X,
        RECTANGULAR_AROUND_Y,
        RECTANGULAR_AROUND_X_AND_Y,
        DISOLVING_BLOCKS,
        CUBE,
        FLIP_HORIZONTAL,
        FLIP_VERTICAL
    }
    private Transition        currentTransition;
    private Image             frontImage;
    private Image             backImage;
    private int               noOfTilesX;
    private int               noOfTilesY;
    private double            stepSizeX;
    private double            stepSizeY;
    private Duration          oneSecond;
    private Duration          twoSecond;
    private Duration          threeSecond;
    private int               delay;
    private Interpolator      spring;
    private Interpolator      spline;
    private Interpolator      easeBoth;
    private List<ImageView>   imageViewsFront;
    private List<ImageView>   imageViewsBack;
    private List<Rectangle2D> viewPorts;
    private List<Timeline>    timelines;
    private List<StackPane>   tiles;
    private boolean           playing;
    private Pane              pane;


    // ******************** Initialization ************************************
    @Override public void init() {
        currentTransition = Transition.VERTICAL_AROUND_X;
        frontImage         = new Image(getClass().getResource("front.png").toExternalForm());
        backImage          = new Image(getClass().getResource("back.png").toExternalForm());
        noOfTilesX         = 8;
        noOfTilesY         = 6;
        stepSizeX          = frontImage.getWidth() / noOfTilesX;
        stepSizeY          = frontImage.getHeight() / noOfTilesY;
        oneSecond          = Duration.millis(1000);
        twoSecond          = Duration.millis(2000);
        threeSecond        = Duration.millis(3000);
        delay              = 100;
        spring             = new SpringInterpolator(1.0, 0.1, 1.5, 0.0, false);
        spline             = Interpolator.SPLINE(0.7, 0, 0.3, 1);
        easeBoth           = Interpolator.EASE_BOTH;
        imageViewsFront    = new ArrayList<>(noOfTilesX * noOfTilesY);
        imageViewsBack     = new ArrayList<>(noOfTilesX * noOfTilesY);
        viewPorts          = new ArrayList<>(noOfTilesX * noOfTilesY);
        timelines          = new ArrayList<>(noOfTilesX * noOfTilesY);
        tiles              = new ArrayList<>(noOfTilesX * noOfTilesY);
        playing            = false;
        pane               = new Pane();

        // init the lists
        int count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX ; x++) {
                imageViewsFront.add(new ImageView());
                imageViewsBack.add(new ImageView());

                timelines.add(new Timeline());

                tiles.add(new StackPane(imageViewsBack.get(count), imageViewsFront.get(count)));
                count++;
            }
        }

        rotateVerticalTilesAroundX(frontImage, backImage, spring, oneSecond, delay);
        //rotateVerticalTilesAroundY(frontImage, backImage, spline, oneSecond, delay);
        //rotateVerticalTilesAroundXandY(frontImage, backImage, spline, oneSecond, delay);

        //rotateHorizontalTilesAroundX(frontImage, backImage, spline, oneSecond, delay);
        //rotateHorizontalTilesAroundY(frontImage, backImage, spline, oneSecond, delay);
        //rotateHorizontalTilesAroundXandY(frontImage, backImage, spline, oneSecond, delay);

        //rotateRectangularTilesAroundX(frontImage, backImage, spline, oneSecond, delay);
        //rotateRectangularTilesAroundY(frontImage, backImage, spline, oneSecond, delay);
        //rotateRectangularTilesAroundXandY(frontImage, backImage, spline, oneSecond, delay);

        //disolvingBlocks(frontImage, backImage, spline, oneSecond, delay);

        //cube(frontImage, backImage, spline, oneSecond, delay);

        //flipHorizontal(frontImage, backImage, spline, twoSecond, delay);
    }


    // ******************** ApplicationStart **********************************
    @Override public void start(Stage stage) {
        pane.setPrefSize(frontImage.getWidth(), frontImage.getHeight());
        pane.getChildren().setAll(tiles);

        Scene scene = new Scene(pane);
        scene.setFill(Color.rgb(50, 50, 50));
        scene.setCamera(new PerspectiveCamera());

        scene.setOnMousePressed(event -> play());
        scene.setOnKeyPressed(event -> play());

        stage.setTitle("Image Transition effect");
        stage.setScene(scene);
        stage.show();

        for (Timeline timeline : timelines) {
            timeline.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** Methods *******************************************
    private void play() {
        if (playing) return;
        playing = true;
        boolean backToFront = imageViewsFront.get(0).getImage().equals(frontImage) ? true : false;

        Image a = backToFront ? backImage : frontImage;
        Image b = backToFront ? frontImage : backImage;
        
        int ord = currentTransition.ordinal() + 1;
        if (ord >= Transition.values().length) ord = 0;
        currentTransition = Transition.values()[ord];
        
        switch(currentTransition) {
            case VERTICAL_AROUND_X:             rotateVerticalTilesAroundY(a, b, easeBoth, oneSecond, delay);         	break;
            case VERTICAL_AROUND_Y:             rotateVerticalTilesAroundXandY(a, b, spline, oneSecond, delay);        	break;
            case VERTICAL_AROUND_X_AND_Y:       rotateHorizontalTilesAroundX(a, b,spring, oneSecond, delay);           	break;
            case HORIZONTAL_AROUND_X:           rotateHorizontalTilesAroundY(a, b,easeBoth, oneSecond, delay);         	break;
            case HORIZONTAL_AROUND_Y:           rotateHorizontalTilesAroundXandY(a, b, spline, oneSecond, delay);       break;
            case HORIZONTAL_AROUND_X_AND_Y:     rotateRectangularTilesAroundX(a, b,spring, oneSecond, delay);          	break;
            case RECTANGULAR_AROUND_X:          rotateRectangularTilesAroundY(a, b, spring, oneSecond, delay);         	break;
            case RECTANGULAR_AROUND_Y:          rotateRectangularTilesAroundXandY(a, b, spline, oneSecond, delay);      break;
            case RECTANGULAR_AROUND_X_AND_Y:    disolvingBlocks(a, b,spline, oneSecond, delay);                			break;
            case DISOLVING_BLOCKS:              cube(a, b,spline, oneSecond, delay);                					break;
            case CUBE:                			flipHorizontal(a, b,spring, oneSecond, delay);                			break;
            case FLIP_HORIZONTAL:               rotateVerticalTilesAroundX(a, b, easeBoth, twoSecond, delay);           break;
        }

        for (Timeline timeline : timelines) 
            timeline.play();
    }

    /**
     * Split the given images into vertical tiles defined by noOfTilesX
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     */
    private void splitImageX(final Image FRONT_IMAGE, final Image BACK_IMAGE) {
        viewPorts.clear();
        for (int i = 0 ; i < noOfTilesX; i++) {
            // Create the viewports
            viewPorts.add(new Rectangle2D(i * stepSizeX, 0, stepSizeX, FRONT_IMAGE.getHeight()));

            // Update the frontside imageviews
            imageViewsFront.get(i).getTransforms().clear();
            imageViewsFront.get(i).toFront();
            imageViewsFront.get(i).setImage(FRONT_IMAGE);
            imageViewsFront.get(i).setViewport(viewPorts.get(i));

            // Update the backside imageviews
            imageViewsBack.get(i).getTransforms().clear();
            imageViewsBack.get(i).setImage(BACK_IMAGE);
            imageViewsBack.get(i).setViewport(viewPorts.get(i));
        }
    }

    /**
     * Split the given images into horizontal tiles defined by noOfTilesY
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     */
    private void splitImageY(final Image FRONT_IMAGE, final Image BACK_IMAGE) {
        viewPorts.clear();
        for (int i = 0 ; i < noOfTilesY; i++) {
            // Create the viewports
            viewPorts.add(new Rectangle2D(0, i * stepSizeY, FRONT_IMAGE.getWidth(), stepSizeY));

            // Update the frontside imageviews
            imageViewsFront.get(i).getTransforms().clear();
            imageViewsFront.get(i).toFront();
            imageViewsFront.get(i).setImage(FRONT_IMAGE);
            imageViewsFront.get(i).setViewport(viewPorts.get(i));

            // Update the backside imageviews
            imageViewsBack.get(i).getTransforms().clear();
            imageViewsBack.get(i).setImage(BACK_IMAGE);
            imageViewsBack.get(i).setViewport(viewPorts.get(i));
        }
    }

    /**
     * Split the given images into rectangular tiles defined by noOfTilesX and noOfTilesY
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     */
    private void splitImageXY(final Image FRONT_IMAGE, final Image BACK_IMAGE) {
        int count = 0;
        viewPorts.clear();
        for (int y = 0 ; y < noOfTilesY; y++) {
            for (int x = 0 ; x < noOfTilesX; x++) {
                // Create the viewports
                viewPorts.add(new Rectangle2D(x * stepSizeX, y * stepSizeY, stepSizeX, stepSizeY));
                ImageView front = imageViewsFront.get(count);
                ImageView back = imageViewsBack.get(count);
                // Update the frontside imageviews
                front.getTransforms().clear();
                front.toFront();
                front.setImage(FRONT_IMAGE);
                front.setViewport(viewPorts.get(count));

                // Update the backside imageviews
                back.getTransforms().clear();
                back.setImage(BACK_IMAGE);
                back.setViewport(viewPorts.get(count));
                count++;
            }
        }
    }

    /**
     * All tiles with an index larger than VISIBLE_UP_TO will be set invisible
     * @param VISIBLE_UP_TO
     */
    private void adjustTilesVisibility(final int VISIBLE_UP_TO) {
        for (int i = 0 ; i < (noOfTilesX * noOfTilesY) ; i++) {
            tiles.get(i).setVisible(i >= VISIBLE_UP_TO ? false : true);
            tiles.get(i).getTransforms().clear();

            imageViewsFront.get(i).setOpacity(1);
            imageViewsFront.get(i).setTranslateX(0);
            imageViewsFront.get(i).setTranslateY(0);
            imageViewsFront.get(i).setTranslateZ(0);
            imageViewsFront.get(i).setRotate(0);
            imageViewsFront.get(i).setScaleX(1);
            imageViewsFront.get(i).setScaleY(1);

            imageViewsBack.get(i).setOpacity(1);
            imageViewsBack.get(i).setTranslateX(0);
            imageViewsBack.get(i).setTranslateY(0);
            imageViewsBack.get(i).setTranslateZ(0);
            imageViewsBack.get(i).setRotate(0);
            imageViewsBack.get(i).setScaleX(1);
            imageViewsBack.get(i).setScaleY(1);
        }
    }


    // ******************** Methods for vertical tiles ************************
    /**
     * Rotate vertical tiles around x transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateVerticalTilesAroundX(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageX(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        for (int i = 0 ; i < noOfTilesX; i++) {
            Rotate rotateX = new Rotate(180, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
            imageViewsBack.get(i).getTransforms().setAll(rotateX);
        }

        for (int i = 0 ; i < noOfTilesX; i++) {			 // Create the animations
            Rotate rotateX = new Rotate(0, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);

            checkVisibility(rotateX, i);
            imageViewsFront.get(i).getTransforms().setAll(rotateX);
            imageViewsBack.get(i).getTransforms().addAll(rotateX);

            tiles.get(i).setTranslateX(i * stepSizeX);		   // Layout the tiles
            tiles.get(i).setTranslateY(0);

            KeyValue kvXBegin = new KeyValue(rotateX.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvXEnd   = new KeyValue(rotateX.angleProperty(), 180, INTERPOLATOR);

            KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin);
            KeyFrame kf1      = new KeyFrame(DURATION, kvXEnd);
            
            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1);
        }
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));
        adjustTilesVisibility(noOfTilesX);
    }

    /**
     * Rotate vertical tiles around y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateVerticalTilesAroundY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageX(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        for (int i = 0 ; i < noOfTilesX; i++) {
            Rotate rotateY = new Rotate(180, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);
            imageViewsBack.get(i).getTransforms().setAll(rotateY);
        }

        for (int i = 0 ; i < noOfTilesX; i++) {
            // Create the animations
            Rotate    rotateY     = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);
            Translate translateX1 = new Translate(0, 0, 0);
            Translate translateX2 = new Translate(0, 0, 0);
            checkVisibility(rotateY, i);

            imageViewsFront.get(i).getTransforms().setAll(rotateY, translateX1);
            imageViewsBack.get(i).getTransforms().addAll(rotateY, translateX2);

            // Layout the tiles horizontal
            tiles.get(i).setTranslateX(i * stepSizeX);
            tiles.get(i).setTranslateY(0);

            KeyValue kvRotateBegin      = new KeyValue(rotateY.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvRotateEnd        = new KeyValue(rotateY.angleProperty(), 180, INTERPOLATOR);
            KeyValue kvTranslate1Begin  = new KeyValue(translateX1.xProperty(), 0, INTERPOLATOR);
            KeyValue kvTranslate2Begin  = new KeyValue(translateX2.xProperty(), 0, INTERPOLATOR);

            KeyValue kvTranslate1Middle = new KeyValue(translateX1.xProperty(), -stepSizeX * 0.5, INTERPOLATOR);
            KeyValue kvTranslate2Middle = new KeyValue(translateX2.xProperty(), stepSizeX * 0.5, INTERPOLATOR);

            KeyValue kvTranslate1End    = new KeyValue(translateX1.xProperty(), 0, INTERPOLATOR);
            KeyValue kvTranslate2End    = new KeyValue(translateX2.xProperty(), 0, INTERPOLATOR);

            KeyFrame kf0                = new KeyFrame(Duration.ZERO, kvRotateBegin, kvTranslate1Begin, kvTranslate2Begin);
            KeyFrame kf1                = new KeyFrame(DURATION.multiply(0.5), kvTranslate1Middle, kvTranslate2Middle);
            KeyFrame kf2                = new KeyFrame(DURATION, kvRotateEnd, kvTranslate1End, kvTranslate2End);

            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1, kf2);
        }
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));
        adjustTilesVisibility(noOfTilesX);
    }

    /**
     * Rotate vertical tiles around x and y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateVerticalTilesAroundXandY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageX(FRONT_IMAGE, BACK_IMAGE);

        for (int i = 0 ; i < noOfTilesX; i++) {
            // Create the rotation objects
            Rotate rotateXFront = new Rotate(0, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
            Rotate rotateYFront = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);

            Rotate rotateXBack  = new Rotate(180, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
            Rotate rotateYBack  = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);

            // Add a listener to the rotation objects
            checkVisibility(rotateXFront, rotateYFront, i);

            // Add the rotations to the image views
            imageViewsFront.get(i).getTransforms().setAll(rotateXFront, rotateYFront);
            imageViewsBack.get(i).getTransforms().setAll(rotateXBack, rotateYBack);

            // Layout the tiles horizontal
            tiles.get(i).setTranslateX(i * stepSizeX);
            tiles.get(i).setTranslateY(0);

            // Create the key-values and key-frames and add them to the timelines
            KeyValue kvXFrontBegin = new KeyValue(rotateXFront.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvXFrontEnd   = new KeyValue(rotateXFront.angleProperty(), 180, INTERPOLATOR);
            KeyValue kvXBackBegin  = new KeyValue(rotateXBack.angleProperty(), -180, INTERPOLATOR);
            KeyValue kvXBackEnd    = new KeyValue(rotateXBack.angleProperty(), 0, INTERPOLATOR);

            KeyValue kvYFrontBegin = new KeyValue(rotateYFront.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvYFrontEnd   = new KeyValue(rotateYFront.angleProperty(), 360, INTERPOLATOR);
            KeyValue kvYBackBegin  = new KeyValue(rotateYBack.angleProperty(), 360, INTERPOLATOR);
            KeyValue kvYBackEnd    = new KeyValue(rotateYBack.angleProperty(), 0, INTERPOLATOR);

            KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvXFrontBegin, kvXBackBegin, kvYFrontBegin, kvYBackBegin);
            KeyFrame kf1 = new KeyFrame(DURATION, kvXFrontEnd, kvXBackEnd, kvYFrontEnd, kvYBackEnd);

            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1);
        }
        // Listen for the last timeline to finish and switch the images
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));
        adjustTilesVisibility(noOfTilesX);
    }


    // ******************** Methods for horizontal tiles **********************
    /**
     * Rotate horizontal tiles around x transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateHorizontalTilesAroundX(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageY(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        for (int i = 0 ; i < noOfTilesY; i++) {
            Rotate rotateX = new Rotate(180, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
            imageViewsBack.get(i).getTransforms().setAll(rotateX);
        }

        for (int i = 0 ; i < noOfTilesY; i++) {
            // Create the animations
            Rotate rotateX = new Rotate(0, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
            Translate translateY1 = new Translate(0, 0, 0);
            Translate translateY2 = new Translate(0, 0, 0);

            checkVisibility(rotateX, i);

            imageViewsFront.get(i).getTransforms().setAll(rotateX, translateY1);
            imageViewsBack.get(i).getTransforms().addAll(rotateX, translateY2);

            // Layout the tiles vertical
            tiles.get(i).setTranslateX(0);
            tiles.get(i).setTranslateY(i * stepSizeY);

            KeyValue kvXBegin           = new KeyValue(rotateX.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvXEnd             = new KeyValue(rotateX.angleProperty(), 180, INTERPOLATOR);
            KeyValue kvTranslate1Begin  = new KeyValue(translateY1.yProperty(), 0, INTERPOLATOR);
            KeyValue kvTranslate2Begin  = new KeyValue(translateY2.yProperty(), 0, INTERPOLATOR);

            KeyValue kvTranslate1Middle = new KeyValue(translateY1.yProperty(), -stepSizeY * 0.25, INTERPOLATOR);
            KeyValue kvTranslate2Middle = new KeyValue(translateY2.yProperty(), stepSizeY * 0.25, INTERPOLATOR);

            KeyValue kvTranslate1End    = new KeyValue(translateY1.yProperty(), 0, INTERPOLATOR);
            KeyValue kvTranslate2End    = new KeyValue(translateY2.yProperty(), 0, INTERPOLATOR);

            KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin, kvTranslate1Begin, kvTranslate2Begin);
            KeyFrame kf1      = new KeyFrame(DURATION.multiply(0.25), kvTranslate1Middle, kvTranslate2Middle);
            KeyFrame kf2      = new KeyFrame(DURATION, kvXEnd, kvTranslate1End, kvTranslate2End);

            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1, kf2);
        }
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesY);
    }

    /**
     * Rotate horizontal tiles around y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateHorizontalTilesAroundY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageY(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        for (int i = 0 ; i < noOfTilesY; i++) {
            Rotate rotateY = new Rotate(180, FRONT_IMAGE.getWidth() * 0.5, 0, 0, Rotate.Y_AXIS);
            imageViewsBack.get(i).getTransforms().setAll(rotateY);
        }

        for (int i = 0 ; i < noOfTilesX; i++) {
            // Create the animations
            Rotate rotateY = new Rotate(0, FRONT_IMAGE.getWidth() * 0.5, 0, 0, Rotate.Y_AXIS);
            checkVisibility(rotateY, i);

            imageViewsFront.get(i).getTransforms().setAll(rotateY);
            imageViewsBack.get(i).getTransforms().addAll(rotateY);

            // Layout the tiles vertical
            tiles.get(i).setTranslateX(0);
            tiles.get(i).setTranslateY(i * stepSizeY);

            KeyValue kvXBegin = new KeyValue(rotateY.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvXEnd   = new KeyValue(rotateY.angleProperty(), 180, INTERPOLATOR);

            KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin);
            KeyFrame kf1      = new KeyFrame(DURATION, kvXEnd);

            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1);
        }
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesY);
    }

    /**
     * Rotate horizontal tiles around x and y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateHorizontalTilesAroundXandY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageY(FRONT_IMAGE, BACK_IMAGE);

        for (int i = 0 ; i < noOfTilesY; i++) {
            // Create the rotation objects
            Rotate rotateXFront = new Rotate(0, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
            Rotate rotateYFront = new Rotate(0, FRONT_IMAGE.getWidth() * 0.5, 0, 0, Rotate.Y_AXIS);

            Rotate rotateXBack = new Rotate(180, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
            Rotate rotateYBack = new Rotate(0, FRONT_IMAGE.getWidth() * 0.5, 0, 0, Rotate.Y_AXIS);

            // Add a listener to the rotation objects
            checkVisibility(rotateXFront, rotateYFront, i);

            // Add the rotations to the image views
            imageViewsFront.get(i).getTransforms().setAll(rotateXFront, rotateYFront);
            imageViewsBack.get(i).getTransforms().setAll(rotateXBack, rotateYBack);

            // Layout the tiles vertical
            tiles.get(i).setTranslateX(0);
            tiles.get(i).setTranslateY(i * stepSizeY);

            // Create the key-values and key-frames and add them to the timelines
            KeyValue kvXFrontBegin = new KeyValue(rotateXFront.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvXFrontEnd   = new KeyValue(rotateXFront.angleProperty(), 180, INTERPOLATOR);
            KeyValue kvXBackBegin  = new KeyValue(rotateXBack.angleProperty(), -180, INTERPOLATOR);
            KeyValue kvXBackEnd    = new KeyValue(rotateXBack.angleProperty(), 0, INTERPOLATOR);

            KeyValue kvYFrontBegin = new KeyValue(rotateYFront.angleProperty(), 0, INTERPOLATOR);
            KeyValue kvYFrontEnd   = new KeyValue(rotateYFront.angleProperty(), 360, INTERPOLATOR);
            KeyValue kvYBackBegin  = new KeyValue(rotateYBack.angleProperty(), 360, INTERPOLATOR);
            KeyValue kvYBackEnd    = new KeyValue(rotateYBack.angleProperty(), 0, INTERPOLATOR);

            KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvXFrontBegin, kvXBackBegin, kvYFrontBegin, kvYBackBegin);
            KeyFrame kf1 = new KeyFrame(DURATION, kvXFrontEnd, kvXBackEnd, kvYFrontEnd, kvYBackEnd);

            timelines.get(i).setDelay(Duration.millis(DELAY * i));
            timelines.get(i).getKeyFrames().setAll(kf0, kf1);
        }
        // Listen for the last timeline to finish and switch the images
        timelines.get(noOfTilesX - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesY);
    }


    // ******************** Methods for rectangular tiles *********************
    /**
     * Rotating tiles around x transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateRectangularTilesAroundX(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageXY(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        int count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX ; x++) {
                Rotate rotateX = new Rotate(180, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
                imageViewsBack.get(count).getTransforms().setAll(rotateX);
                count++;
            }
        }

        count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX ; x++) {
                // Create the animations
                Rotate rotateX = new Rotate(0, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
                checkVisibility(rotateX, count);

                imageViewsFront.get(count).getTransforms().setAll(rotateX);
                imageViewsBack.get(count).getTransforms().addAll(rotateX);

                // Layout the tiles in grid
                tiles.get(count).setTranslateX(x * stepSizeX);
                tiles.get(count).setTranslateY(y * stepSizeY);

                KeyValue kvXBegin = new KeyValue(rotateX.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvXEnd   = new KeyValue(rotateX.angleProperty(), 180, INTERPOLATOR);

                KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin);
                KeyFrame kf1      = new KeyFrame(DURATION, kvXEnd);

                timelines.get(count).setDelay(Duration.millis(DELAY * (2 * x + y)));
                timelines.get(count).getKeyFrames().setAll(kf0, kf1);

                count++;
            }
        }
        timelines.get((noOfTilesX * noOfTilesY) - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesX * noOfTilesY);
    }

    /**
     * Rotating tiles around y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateRectangularTilesAroundY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageXY(FRONT_IMAGE, BACK_IMAGE);

        // PreTransform backside imageviews
        int count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX ; x++) {
                Rotate rotateY = new Rotate(180, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);
                imageViewsBack.get(count).getTransforms().setAll(rotateY);
                count++;
            }
        }

        count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX ; x++) {
                // Create the animations
                Rotate rotateY = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);

                checkVisibility(rotateY, count);

                imageViewsFront.get(count).getTransforms().setAll(rotateY);
                imageViewsBack.get(count).getTransforms().addAll(rotateY);

                // Layout the tiles in grid
                tiles.get(count).setTranslateX(x * stepSizeX);
                tiles.get(count).setTranslateY(y * stepSizeY);

                KeyValue kvXBegin = new KeyValue(rotateY.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvXEnd   = new KeyValue(rotateY.angleProperty(), 180, INTERPOLATOR);

                KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin);
                KeyFrame kf1      = new KeyFrame(DURATION, kvXEnd);

                timelines.get(count).setDelay(Duration.millis(DELAY * (x + 2 * y)));
                timelines.get(count).getKeyFrames().setAll(kf0, kf1);

                count++;
            }
        }
        timelines.get((noOfTilesX * noOfTilesY) - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesX * noOfTilesY);
    }

    /**
     * Rotating tiles in x and y transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void rotateRectangularTilesAroundXandY(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageXY(FRONT_IMAGE, BACK_IMAGE);

        int count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX; x++) {
                // Create the rotation objects
                Rotate rotateXFront = new Rotate(0, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
                Rotate rotateYFront = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);

                Rotate rotateXBack = new Rotate(180, 0, stepSizeY * 0.5, 0, Rotate.X_AXIS);
                Rotate rotateYBack = new Rotate(0, stepSizeX * 0.5, 0, 0, Rotate.Y_AXIS);

                Translate translateZFront = new Translate();
                Translate translateZBack  = new Translate();

                // Add a listener to the rotation objects
                checkVisibility(rotateXFront, rotateYFront, count);

                // Add the rotations to the image views
                imageViewsFront.get(count).getTransforms().setAll(rotateXFront, rotateYFront, translateZFront);
                imageViewsBack.get(count).getTransforms().setAll(rotateXBack, rotateYBack, translateZBack);

                // Layout the tiles in grid
                tiles.get(count).setTranslateX(x * stepSizeX);
                tiles.get(count).setTranslateY(y * stepSizeY);

                // Create the key-values and key-frames and add them to the timelines
                KeyValue kvXFrontBegin  = new KeyValue(rotateXFront.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvXFrontEnd    = new KeyValue(rotateXFront.angleProperty(), 180, INTERPOLATOR);
                KeyValue kvXBackBegin   = new KeyValue(rotateXBack.angleProperty(), -180, INTERPOLATOR);
                KeyValue kvXBackEnd     = new KeyValue(rotateXBack.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvZFrontBegin  = new KeyValue(translateZFront.zProperty(), 0, INTERPOLATOR);
                KeyValue kvzBackBegin   = new KeyValue(translateZBack.zProperty(), 0, INTERPOLATOR);

                KeyValue kvZFrontMiddle = new KeyValue(translateZFront.zProperty(), 50, INTERPOLATOR);
                KeyValue kvZBackMiddle  = new KeyValue(translateZBack.zProperty(), -50, INTERPOLATOR);

                KeyValue kvYFrontBegin  = new KeyValue(rotateYFront.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvYFrontEnd    = new KeyValue(rotateYFront.angleProperty(), 360, INTERPOLATOR);
                KeyValue kvYBackBegin   = new KeyValue(rotateYBack.angleProperty(), 360, INTERPOLATOR);
                KeyValue kvYBackEnd     = new KeyValue(rotateYBack.angleProperty(), 0, INTERPOLATOR);
                KeyValue kvZFrontEnd    = new KeyValue(translateZFront.zProperty(), 0, INTERPOLATOR);
                KeyValue kvZBackEnd     = new KeyValue(translateZBack.zProperty(), 0, INTERPOLATOR);

                KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvXFrontBegin, kvXBackBegin, kvYFrontBegin, kvYBackBegin, kvZFrontBegin, kvzBackBegin);
                KeyFrame kf1 = new KeyFrame(DURATION.multiply(0.5), kvZFrontMiddle, kvZBackMiddle);
                KeyFrame kf2 = new KeyFrame(DURATION, kvXFrontEnd, kvXBackEnd, kvYFrontEnd, kvYBackEnd, kvZFrontEnd, kvZBackEnd);

                timelines.get(count).setDelay(Duration.millis(DELAY * (x + y)));
                timelines.get(count).getKeyFrames().setAll(kf0, kf1, kf2);

                count++;
            }
        }
        // Listen for the last timeline to finish and switch the images
        timelines.get((noOfTilesX * noOfTilesY) - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesX * noOfTilesY);
    }


    // ******************** Other transitions *********************************
    /**
     * Disolving tiles transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void disolvingBlocks(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        splitImageXY(FRONT_IMAGE, BACK_IMAGE);

        int count = 0;
        for (int y = 0 ; y < noOfTilesY ; y++) {
            for (int x = 0 ; x < noOfTilesX; x++) {
               
                ImageView front = imageViewsFront.get(count);
                ImageView back = imageViewsBack.get(count);
                
                tiles.get(count).setTranslateX(x * stepSizeX);			 // Layout the tiles in grid
                tiles.get(count).setTranslateY(y * stepSizeY);

                tiles.get(count).getTransforms().clear();
                front.getTransforms().clear();
                back.getTransforms().clear();

                // Create the key-values and key-frames and add them to the timelines
                KeyValue kvFrontOpacityBegin = new KeyValue(front.opacityProperty(), 1, INTERPOLATOR);
                KeyValue kvFrontOpacityEnd   = new KeyValue(front.opacityProperty(), 0, INTERPOLATOR);

                KeyValue kvFrontScaleXBegin  = new KeyValue(front.scaleXProperty(), 1, INTERPOLATOR);
                KeyValue kvFrontScaleXEnd    = new KeyValue(front.scaleXProperty(), 0, INTERPOLATOR);

                KeyValue kvFrontScaleYBegin  = new KeyValue(front.scaleYProperty(), 1, INTERPOLATOR);
                KeyValue kvFrontScaleYEnd    = new KeyValue(front.scaleYProperty(), 0, INTERPOLATOR);

                KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvFrontOpacityBegin, kvFrontScaleXBegin, kvFrontScaleYBegin);
                KeyFrame kf2 = new KeyFrame(DURATION, kvFrontOpacityEnd, kvFrontScaleXEnd, kvFrontScaleYEnd);

                timelines.get(count).setDelay(Duration.millis(DELAY * (x + y)));
                timelines.get(count).getKeyFrames().setAll(kf0, kf2);

                count++;
            }
        }
        // Listen for the last timeline to finish and switch the images
        timelines.get((noOfTilesX * noOfTilesY) - 1).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));

        adjustTilesVisibility(noOfTilesX * noOfTilesY);
    }

    /**
     * Cube transition between front- and backimage
     * @param FRONT_IMAGE
     * @param BACK_IMAGE
     * @param INTERPOLATOR  spline that is used for the animation
     * @param DURATION      oneSecond for the transition
     * @param DELAY         delay in milliseconds between each tile animation
     */
    private void cube(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        adjustTilesVisibility(1);
        viewPorts.clear();

        final Rectangle2D VIEW_PORT = new Rectangle2D(0, 0, FRONT_IMAGE.getWidth(), FRONT_IMAGE.getHeight());
        for (int i = 0 ; i < (noOfTilesX * noOfTilesY) ; i++) {
            imageViewsFront.get(i).setViewport(VIEW_PORT);
            imageViewsBack.get(i).setViewport(VIEW_PORT);
        }

        imageViewsFront.get(0).setImage(FRONT_IMAGE);
        imageViewsBack.get(0).setImage(BACK_IMAGE);

        imageViewsFront.get(0).setTranslateZ(-0.5 * FRONT_IMAGE.getWidth());

        imageViewsBack.get(0).setTranslateX(0.5 * FRONT_IMAGE.getWidth());
        imageViewsBack.get(0).setRotationAxis(Rotate.Y_AXIS);
        imageViewsBack.get(0).setRotate(90);

        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        rotateY.setPivotX(FRONT_IMAGE.getWidth() * 0.5);
        rotateY.setPivotZ(FRONT_IMAGE.getWidth() * 0.5);
        rotateY.angleProperty().addListener((ov, oldAngle, newAngle) -> {
            if (73 < newAngle.intValue()) {
                imageViewsBack.get(0).toFront();
            }
        });

        Translate translateZ = new Translate(0, 0, FRONT_IMAGE.getWidth() * 0.5);
        tiles.get(0).getTransforms().setAll(rotateY, translateZ);

        KeyValue kvRotateBegin        = new KeyValue(rotateY.angleProperty(), 0, INTERPOLATOR);
        KeyValue kvRotateEnd          = new KeyValue(rotateY.angleProperty(), 90, INTERPOLATOR);

        KeyValue kvOpacityFrontBegin  = new KeyValue(imageViewsFront.get(0).opacityProperty(), 1.0, INTERPOLATOR);
        KeyValue kvOpacityBackBegin   = new KeyValue(imageViewsBack.get(0).opacityProperty(), 0.0, INTERPOLATOR);

        KeyValue kvScaleXBegin        = new KeyValue(tiles.get(0).scaleXProperty(), 1.0, INTERPOLATOR);
        KeyValue kvScaleYBegin        = new KeyValue(tiles.get(0).scaleYProperty(), 1.0, INTERPOLATOR);

        KeyValue kvScaleXMiddle       = new KeyValue(tiles.get(0).scaleXProperty(), 0.85, INTERPOLATOR);
        KeyValue kvScaleYMiddle       = new KeyValue(tiles.get(0).scaleYProperty(), 0.85, INTERPOLATOR);

        KeyValue kvOpacityFrontMiddle = new KeyValue(imageViewsFront.get(0).opacityProperty(), 1.0, INTERPOLATOR);
        KeyValue kvOpacityBackMiddle  = new KeyValue(imageViewsBack.get(0).opacityProperty(), 1.0, INTERPOLATOR);

        KeyValue kvScaleXEnd          = new KeyValue(tiles.get(0).scaleXProperty(), 1.0, INTERPOLATOR);
        KeyValue kvScaleYEnd          = new KeyValue(tiles.get(0).scaleYProperty(), 1.0, INTERPOLATOR);

        KeyValue kvOpacityFrontEnd    = new KeyValue(imageViewsFront.get(0).opacityProperty(), 0.0, INTERPOLATOR);
        KeyValue kvOpacityBackEnd     = new KeyValue(imageViewsBack.get(0).opacityProperty(), 1.0, INTERPOLATOR);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvRotateBegin, kvScaleXBegin, kvScaleYBegin, kvOpacityFrontBegin, kvOpacityBackBegin);
        KeyFrame kf1 = new KeyFrame(DURATION.multiply(0.5), kvScaleXMiddle, kvScaleYMiddle, kvOpacityFrontMiddle, kvOpacityBackMiddle);
        KeyFrame kf2 = new KeyFrame(DURATION, kvRotateEnd, kvScaleXEnd, kvScaleYEnd, kvOpacityFrontEnd, kvOpacityBackEnd);

        timelines.get(0).setDelay(Duration.millis(DELAY));
        timelines.get(0).getKeyFrames().setAll(kf0, kf1, kf2);

        // Listen for the last timeline to finish and switch the images
        timelines.get(0).setOnFinished(observable -> toggleBackToFront(backImage, frontImage) );
    }

    private void flipHorizontal(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
        viewPorts.clear();

        // PreTransform backside imageview
        Rotate preRotateX = new Rotate(180, 0, -FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
        imageViewsBack.get(0).getTransforms().setAll(preRotateX);

        // Create the animations
        Rotate rotateX = new Rotate(0, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
        checkVisibility(rotateX, 0);

        imageViewsFront.get(0).getTransforms().setAll(rotateX);
        imageViewsBack.get(0).getTransforms().addAll(rotateX);

        KeyValue kvXBegin = new KeyValue(rotateX.angleProperty(), 0, INTERPOLATOR);
        KeyValue kvXEnd   = new KeyValue(rotateX.angleProperty(), 180, INTERPOLATOR);

        KeyFrame kf0      = new KeyFrame(Duration.ZERO, kvXBegin);
        KeyFrame kf1      = new KeyFrame(DURATION, kvXEnd);

        timelines.get(0).getKeyFrames().setAll(kf0, kf1);

        timelines.get(0).setOnFinished(observable -> toggleBackToFront(backImage, frontImage));        

        adjustTilesVisibility(1);
    }


    /**
     * Check which side of the tile is visible when rotating around x and y axis
     * @param ROTATE_X
     * @param ROTATE_Y
     * @param INDEX
     */
    private void checkVisibility(final Rotate ROTATE_X, final Rotate ROTATE_Y, final int INDEX) {
        ROTATE_X.angleProperty().addListener(observable -> {
            int angleX = (int) ROTATE_X.getAngle();
            int angleY = (int) ROTATE_Y.getAngle();
            if (inRange(angleX,90, 270))
            	show(inRange(angleY, 90, 270), INDEX);
            else show(!inRange(angleY, 90, 270), INDEX);            
        });
    }

    /**
     * Check which side of the tile is visible when rotating around one axis
     * @param ROTATE
     * @param INDEX
     */
    private void checkVisibility(final Rotate ROTATE, final int INDEX) {
        ROTATE.angleProperty().addListener((ov, oldAngle, newAngle) -> {
        	show(newAngle.doubleValue() > 270, INDEX);
        });
    }
    private static boolean inRange(int val, int min, int max)	{ return val > min && val <= max;	}
    private void show(boolean showFront, int idx)	
    {  if (showFront)   	imageViewsFront.get(idx).toFront(); 
    else 				   	imageViewsBack.get(idx).toFront();
    }
    
    /**
     * Toggles the backside image to the frontside.
     * @param BACK_IMAGE
     * @param FRONT_IMAGE
     */
    private void toggleBackToFront(final Image BACK_IMAGE, final Image FRONT_IMAGE) {
        playing = false;
    }
}