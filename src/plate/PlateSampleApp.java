
package plate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * based on Oracles MoleculeSampleApp
 * @author cmcastil
 */
public class PlateSampleApp extends Application {

    final Group root = new Group();
    final Xform axisGroup = new Xform();
//    final Xform moleculeGroup = new Xform();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -1800;
    private static final double CAMERA_INITIAL_X_ANGLE = 0.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 180.0;
    private static final double CAMERA_SECOND_X_ANGLE = 66.0;
//    private static final double CAMERA_SECOND_Y_ANGLE = 180.0;
    private static final double CAMERA_NEAR_CLIP = 10.;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;
    
    double mousePosX, mousePosY;
    double mouseOldX, mouseOldY;
    double mouseDeltaX, mouseDeltaY;
    
    //---------------------------------------------------------------------------------------------------
     @Override
     public void start(Stage primaryStage) {
         
        // setUserAgentStylesheet(STYLESHEET_MODENA);
         System.out.println("start()");

         root.getChildren().add(world);
         root.setDepthTest(DepthTest.ENABLE);

         // buildScene();
         buildCamera();
//         buildAxes();
         initModel();
         buildPlate();
         buildControlSubscene();

         Scene scene = new Scene(root, 1024, 768, true);
         scene.setFill(Color.LIGHTSLATEGREY);
         handleKeyboard(scene, world);
         handleMouse(scene, world);

         primaryStage.setTitle("3D Plate Editor Prototype");
         primaryStage.setScene(scene);
         primaryStage.show();

         scene.setCamera(camera);
         
     }

    
     private void buildControlSubscene() {
		// TODO put some controls here.  
    	 // List of attribute values
    	 // staging wells
		
	}


	private void buildCamera() {
        System.out.println("buildCamera()");
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
//        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX); 
                mouseDeltaY = (mousePosY - mouseOldY); 
                
                double modifier = 1.0;
                
                if (me.isControlDown()) {                  modifier = CONTROL_MULTIPLIER;                } 
                if (me.isShiftDown()) {                    modifier = SHIFT_MULTIPLIER;                }     
                if (me.isPrimaryButtonDown()) {
                	
                	double yAngle = cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED;		// note the mouse movement is flipped x <-> y
                    cameraXform.ry.setAngle(yAngle);  
                    double xAngle = cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED;
                    cameraXform.rx.setAngle(xAngle);  
//                    dumpCamera();
//                    System.out.println(xAngle + ", " + yAngle);    
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown() || me.isShiftDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);  
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);  
                }
                
            }
        });
    }
    
    private void handleKeyboard(Scene scene, final Node root) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                case Z:
                    cameraXform2.t.setX(0.0);
                    cameraXform2.t.setY(0.0);
                    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                    cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                    cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                    cameraXform.rz.setAngle(0);
                    break;
                case D:
                    resetData();
                    break;
                case B:
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 5);
                    break;
                case C:
                    resetData();
                    break;
                case N:
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 5);
                    break;
//                    case X:
//                        axisGroup.setVisible(!axisGroup.isVisible());
//                        break;
                    case V:
                        wholePlate.setVisible(!wholePlate.isVisible());
                        break;
  
                    case G:
                        RotateTransition rt = new RotateTransition(Duration.millis(3000));
                        rt.setAxis(new Point3D(1,0,0));
                        rt.setNode(wholePlate);
                        rt.setFromAngle(CAMERA_INITIAL_X_ANGLE);
                        rt.setToAngle(CAMERA_SECOND_X_ANGLE);
                        rt.play();
                        break;

                    
                    case A: dumpCamera();                         break;
                    default: break;
                }
            }

        });
    }
    Xform wholePlate = new Xform();
  private void dumpCamera()
  {
    	Camera c = camera;
    	double rxAngle = cameraXform.rx.getAngle();
    	double ryAngle = cameraXform.ry.getAngle();
    	double rzAngle = cameraXform.rz.getAngle();
        double x = cameraXform2.t.getX();
        double y = cameraXform2.t.getY();
        double z = camera.getTranslateZ();

    	System.out.println("Position: [ " + x + ", " + y  + ", " + z + " ]");
    	System.out.println("Direction: [ " + rxAngle + ", " + ryAngle  + ", " + rzAngle + " ]");
	  
  }
//---------------------------------------------------------------------------------------------------
   private void buildPlate() 
   {
 
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.ORANGE);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

        final PhongMaterial greyMaterial = new PhongMaterial();
        greyMaterial.setDiffuseColor(Color.DARKGREY);
        greyMaterial.setSpecularColor(Color.GREY);
        
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        
//        Xform wholePlate = new Xform();
        Xform plate = new Xform();
        Xform[][] wellColumns = new Xform[12][];
        
        int k = 60;
        int HEIGHT = 8;
        int WIDTH = 12;
        Box plateBox = new Box((WIDTH + 1) * k, (HEIGHT + 1) * k, k / 6);
        plateBox.setMaterial(redMaterial);
        plate.getChildren().add(plateBox);
        int halfK = k / 2;
        int halfWidth = 6 * k;
        int halfHeight = 4 * k;
        int fullWidth = WIDTH * k;
        int fullHeight = 8 * k;
        for (int i=0; i < WIDTH; i++)
        {  
        	Xform[] wellColumn = new Xform[HEIGHT];
        	for (int j=0; j<HEIGHT; j++)
              {
        		DoubleProperty heightProperty = getWellHeightProperty(j,i); 
         		Cylinder c = new Cylinder(k / 3, 4);
         		c.heightProperty().bind(heightProperty);
            	c.setMaterial(whiteMaterial);
            	if (2 * i == 3 * j) c.setMaterial(greyMaterial);		// dummy coloring
            
            	
            	c.setRotationAxis(new Point3D(1,0,0));
            	c.setRotate(90.);
            	c.setTranslateX(0 - (k * i - halfWidth + halfK));
            	c.setTranslateY(k * j - halfHeight + halfK);
            	c.translateZProperty().bind(heightProperty.divide(2).add(k/8));   //k/8 + heightProperty.getValue() / 2);
//            	c.setTranslateZ(k/8 + heightProperty.getValue() / 2);
            	Text wellDescription = new Text();
            	
            	wellDescription.setText(wellNotation96(i,j));
            	wellDescription.setRotationAxis(new Point3D(0,1,0));
            	wellDescription.setFont(Font.font(null, FontWeight.BOLD, 14));
            	wellDescription.setTranslateX(0 - (k * i - halfWidth + halfK + k / 4 + (i==8 ? -4 : 0)));  // a hack to make up for I being narrower than the other letters
            	wellDescription.setTranslateY(k * j - halfHeight + halfK);
            	wellDescription.setTranslateZ(0);
            	wellDescription.setRotate(180.);
            	wellDescription.translateZProperty().bind(heightProperty.add(10));
            	wellDescription.setFill(Color.BLACK);
            	wellDescription.setTextOrigin(VPos.CENTER);

//                Text t = new Text();
//                t.setText(wellNotation96(i,j));
//                t.setFill(Color.RED);
//                t.setFont(Font.font(null, FontWeight.BOLD, 48));
//                t.setTranslateX(0 - (k * i - halfWidth + halfK));
//                t.setRotationAxis(new Point3D(1,1,0));
//                t.setRotate(-90);
//                t.setTranslateY(k * j - halfHeight + halfK);
//                t.setTextOrigin(VPos.TOP);

          	
            	wellColumn[j] = new Xform();
            	wellColumn[j].getChildren().add(c);
//            	getWellHeightProperty(i,j).addListener(new ChangeListener());
//            	model.get(i * 12 + j).add
            	Group g = new Group();
//            	g.getChildren().add(t);
             	g.getChildren().add(c);
            	g.getChildren().add(wellDescription);
             	plate.getChildren().add(g);
            }
        }

        wholePlate.getChildren().add(plate);
        world.getChildren().addAll(wholePlate);
    }
 
   // this needs to be replaced with bindings to the model
   List<DoubleProperty> model;
   private void initModel()
   {
	   model =  IntStream.range(0,96)
			   .mapToObj(SimpleDoubleProperty::new)
			   .collect(Collectors.toList());
//	   model.addAll(Arrays.asList(new SimpleDoubleProperty[96]));
	   
//	   Sequence.stream(0,96).mapToOb
//	   System.out.println(
			 
//	   model.addAll(IntStream.range(0,96).mapToObj(i -> new SimpleDoubleProperty(100 + i) ).get()));
//	   resetData() ;
	   
   }
 //---------------------------------------------------------------------------------------------------
 // These have been copied to Well.java, but the streams above don't work without them here.
   
   final static String wellNotation96(int i)
   {
	   return padTo3("" + (char)('A' + (i / 12)) + (int) (1 + (i % 12)));	
   }
   
   final static String wellNotation96(int i, int j)
   {
	   return wellNotation96(i * 12 + j);	
   }
   
   final static int wellToInt96(String s)
   {
	   return (int)(s.charAt(0)-'A') * 12 + Integer.parseInt(s.substring(1));
  }

   final static private String padTo3(String s)
   {
	   return (s.length() == 3 ? s : "" + s.charAt(0) + '0' + s.charAt(1));
   }
 //---------------------------------------------------------------------------------------------------
  
	private void resetData() 
	{	
		
//		IntStream.range(0,96)
//			 .mapToObj(x -> wellNotation96(x))
//			.forEach(System.out::println);
		model.forEach(x -> x.setValue(Math.random() * 100));
	} 
	
	  private DoubleProperty getWellHeightProperty(int i, int j) 	   	{		return	getWellHeightProperty(i * 12 + j);		}	  // 96
	  private DoubleProperty getWellHeightProperty(int i) 	   			{		return	model.get(i); 	}			// TODO dummy


//---------------------------------------------------------------------------------------------------
   
      /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
