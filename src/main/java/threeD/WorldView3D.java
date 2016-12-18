package threeD;

import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class WorldView3D extends Group
{
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
    Scene scene;
    Xform wholePlate = new Xform();
   //---------------------------------------------------------------------------------------------------
    public WorldView3D(Group root)
    {
    	scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.LIGHTSLATEGREY);
        buildCamera(root);
        handleKeyboard(scene, world);
        handleMouse(scene, world);
        scene.setCamera(camera);
  }
    //---------------------------------------------------------------------------------------------------
    public Scene get3DScene()  { return scene;	}
    public Xform getWholePlate()  { return wholePlate;	}
    //---------------------------------------------------------------------------------------------------
	private void buildCamera(Group root) {
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

    //---------------------------------------------------------------------------------------------------
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
    
   //---------------------------------------------------------------------------------------------------
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

			private void resetData()
			{
				// TODO Auto-generated method stub
				
			}

        });
    }
    //---------------------------------------------------------------------------------------------------
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
}
