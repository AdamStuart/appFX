package threeD;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EarthViewer extends Application {

  private static final double EARTH_RADIUS  = 400;
  private static final double VIEWPORT_SIZE = 800;
  private static final double ROTATE_SECS   = 30;

  private static final double MAP_WIDTH  = 8192 / 2d;
  private static final double MAP_HEIGHT = 4092 / 2d;

  private static final String PREFIX = "http://planetmaker.wthr.us/img/earth_";
  private static final String DIFFUSE_MAP = PREFIX + "gebco8_texture_8192x4096.jpg";
  private static final String NORMAL_MAP = PREFIX + "normalmap_flat_8192x4096.jpg";
  private static final String SPECULAR_MAP = PREFIX + "specularmap_flat_8192x4096.jpg";

  private Group buildScene() {
    Sphere earth = new Sphere(EARTH_RADIUS);
    earth.setTranslateX(VIEWPORT_SIZE / 2d);
    earth.setTranslateY(VIEWPORT_SIZE / 2d);
//    earth.setScaleX(0.1);
//    earth.setScaleY(0.1);
//    earth.setScaleZ(0.1);

    PhongMaterial earthMaterial = new PhongMaterial();
    earthMaterial.setDiffuseMap( new Image( DIFFUSE_MAP,  MAP_WIDTH, MAP_HEIGHT, true,  true  ) );
    earthMaterial.setBumpMap( new Image(   NORMAL_MAP, MAP_WIDTH, MAP_HEIGHT,  true, true  )  );
    earthMaterial.setSpecularMap( new Image( SPECULAR_MAP, MAP_WIDTH, MAP_HEIGHT, true, true  ) );

    earth.setMaterial(  earthMaterial );
    return new Group(earth);
  }

  @Override  public void start(Stage stage) {
    Group group = buildScene();
    StackPane root = new StackPane(group);
    Scene scene = new Scene(root, VIEWPORT_SIZE, VIEWPORT_SIZE, true,SceneAntialiasing.BALANCED );
    scene.setFill(Color.rgb(10, 10, 40));
    PerspectiveCamera cam = new PerspectiveCamera();
    scene.setCamera(cam);
//    cam.setRotationAxis(Rotate.Y_AXIS);
//    cam.setRotate(30);
    stage.setScene(scene);

//    stage.setFullScreen(true);
    rotateAroundYAxis(group).play();
    scaleUp(group).play();
    stage.show();
  }

  private RotateTransition rotateAroundYAxis(Node node) {
    RotateTransition rotate = new RotateTransition( Duration.seconds(ROTATE_SECS),  node );
    rotate.setAxis(Rotate.Y_AXIS);
    rotate.setFromAngle(360);
    rotate.setToAngle(0);
    rotate.setInterpolator(Interpolator.LINEAR);
    rotate.setCycleCount(RotateTransition.INDEFINITE);
    return rotate;
  }

  private ScaleTransition scaleUp(Node node) {
	  ScaleTransition grower = new ScaleTransition( Duration.seconds(1d),  node );
		 grower.setFromX(.1); 	 grower.setFromY(.1); 	 grower.setFromZ(.1); 
		 grower.setToX(0.75); 	 grower.setToY(0.75); 	 grower.setToZ(0.75); 
		 grower.setDuration(Duration.seconds(15));
	 grower.setInterpolator(Interpolator.EASE_BOTH);
	  grower.setCycleCount(1);
    return grower;
  }

  public static void main(String[] args) {
    launch(args);
  }
}