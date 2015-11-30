package bookclub;

import java.util.ArrayList;
import java.util.List;

import animation.SpringInterpolator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class GeneralTransition
{
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
	};

	protected GeneralTransition(Transition t, int nTilesX, int nTilesY)
	{
		currentTransition = t;
		noOfTilesX = nTilesX;
		noOfTilesY = nTilesY;
		int nTiles = noOfTilesX * noOfTilesY;
		imageViewsFront    = new ArrayList<ImageView>(nTiles);
		imageViewsBack = new ArrayList<ImageView>(nTiles);
		viewPorts = new ArrayList<Rectangle2D>(nTiles);
		timelines = new ArrayList<Timeline>(nTiles);
		tiles              = new ArrayList<StackPane>(nTiles);
	}
	
	protected Transition        currentTransition;
	protected Image             frontImage;
	protected Image             backImage;
	protected int               noOfTilesX = 1;
	protected int               noOfTilesY = 1;
	protected double            stepSizeX;
	protected double            stepSizeY;
	protected Duration          oneSecond  = Duration.millis(1000);
	protected int               delay = 100;
	protected Interpolator      spring = new SpringInterpolator(1.0, 0.1, 1.5, 0.0, false);
	protected Interpolator      spline = Interpolator.SPLINE(0.7, 0, 0.3, 1);
	protected Interpolator      easeBoth = Interpolator.EASE_BOTH;
	protected List<ImageView>   imageViewsFront;
	protected List<ImageView>   imageViewsBack;
	protected List<Rectangle2D> viewPorts;
	protected List<Timeline>    timelines;
	protected List<StackPane>   tiles;
	protected boolean           playing = false;
	protected Pane              pane = new Pane();

	
	  // ******************** Methods for vertical tiles ************************
	  /**
	   * Rotate vertical tiles around x transition between front- and backimage
	   * @param FRONT_IMAGE
	   * @param BACK_IMAGE
	   * @param INTERPOLATOR  spline that is used for the animation
	   * @param DURATION      oneSecond for the transition
	   * @param DELAY         delay in milliseconds between each tile animation
	   */
	  protected void rotateVerticalTilesAroundX(final Image FRONT_IMAGE, final Image BACK_IMAGE, final Interpolator INTERPOLATOR, final Duration DURATION, final int DELAY) {
	      splitImageX(FRONT_IMAGE, BACK_IMAGE);

	      // PreTransform backside imageviews
	      for (int i = 0 ; i < noOfTilesX; i++) {
	          Rotate rotateX = new Rotate(180, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);
	          imageViewsBack.get(i).getTransforms().setAll(rotateX);
	      }

	      for (int i = 0 ; i < noOfTilesX; i++) {
	          // Create the animations
	          Rotate rotateX = new Rotate(0, 0, FRONT_IMAGE.getHeight() * 0.5, 0, Rotate.X_AXIS);

	          checkVisibility(rotateX, i);

	          imageViewsFront.get(i).getTransforms().setAll(rotateX);
	          imageViewsBack.get(i).getTransforms().addAll(rotateX);

	          // Layout the tiles horizontal
	          tiles.get(i).setTranslateX(i * stepSizeX);
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
	   * Toggles the backside image to the frontside.
	   * @param BACK_IMAGE
	   * @param FRONT_IMAGE
	   */
	  private void toggleBackToFront(final Image BACK_IMAGE, final Image FRONT_IMAGE) {
	      playing = false;
	      backImage = FRONT_IMAGE;
	      frontImage = BACK_IMAGE;
	  }

	  /**
	   * Check which side of the tile is visible when rotating around one axis
	   * @param ROTATE
	   * @param INDEX
	   */
	  private void checkVisibility(final Rotate ROTATE, final int INDEX) {
	      ROTATE.angleProperty().addListener((ov, oldAngle, newAngle) -> 
	      {
	        double v = newAngle.doubleValue();
	        if (v > 360)            imageViewsFront.get(INDEX).toFront();
	        else if (v > 270)       imageViewsFront.get(INDEX).toFront();
	        else if (v > 180)       imageViewsBack.get(INDEX).toFront();
	        else if (v > 90)        imageViewsBack.get(INDEX).toFront();
	          
	      });
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

}
