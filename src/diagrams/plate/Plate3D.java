package diagrams.plate;

import javafx.animation.RotateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import threeD.Xform;

public class Plate3D extends SubScene
{
    private Xform root;
    private Xform wholePlate;
    private PlateModel model;
    
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    
    public static final double CAMERA_INITIAL_DISTANCE = -1200;	
    private static final double CAMERA_INITIAL_X_ANGLE = 150.0;
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

    public Plate3D(PlateModel inModel, Xform top, double w, double h)
	{
		super(top, w, h, true, SceneAntialiasing.DISABLED);
		model = inModel;
		root = top;
//		SubScene subscene = new SubScene(world, w, h, true, SceneAntialiasing.DISABLED);
//        world.getChildren().add(subscene);
	    cameraSetup();
	    setFill(Color.CORNSILK);
	    buildPlate3D(model);
	    handleKeyboard(this, root);
        handleMouse(this, root);
	}

	private Xform cameraSetup() 
	{
	    setCamera(camera);
	    cameraXform.getChildren().add(camera);
        cameraXform2.getChildren().add(cameraXform);
        cameraXform3.getChildren().add(cameraXform2);
        cameraXform3.setRotateZ(180.0);
        setCamera(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);		
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
        return cameraXform;
    }
	
	public void setXAngle(double deg)
	{
        cameraXform.rx.setAngle(deg);
        cameraXform.t.setY(deg/2-50);
	}

	public void setDistance(double d)
	{
        camera.setTranslateZ(d);
	}

    private void handleMouse(SubScene scene, final Node root) {
        scene.setOnMousePressed(me ->    {
                mousePosX =  mouseOldX = me.getSceneX();
                mousePosY = mouseOldY = me.getSceneY();
            } );
        
        scene.setOnMouseDragged(me ->   {
                double modifier = 1.0;
                boolean altDown = me.isAltDown();
                if (me.isControlDown())     modifier = CONTROL_MULTIPLIER;        
                if (me.isShiftDown())       modifier = SHIFT_MULTIPLIER;          

                
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = altDown ?  (mousePosX - mouseOldX) :  0;  //; 			DEBUG or if modifier down
                mouseDeltaY = (mousePosY - mouseOldY); 
                
                if (me.isPrimaryButtonDown()) {
                	
                	double yAngle = cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED;		// note the mouse movement is flipped x <-> y
                    cameraXform.ry.setAngle(yAngle);  
                    double xAngle = cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED;
                    cameraXform.rx.setAngle(xAngle);  
                    double yVal = cameraXform.t.getY() + mouseDeltaY/2;
                    cameraXform.t.setY(yVal);
//                    dumpCamera();
//                    System.out.println("yVal  " + yVal);    
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
                
        });
    }
    
    
    private void handleKeyboard(SubScene scene, final Node root) {
        scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                case Z:
                    cameraXform2.t.setX(0.0);
                    cameraXform2.t.setY(0.0);
                    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                    cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                    cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                    cameraXform.rz.setAngle(0);
                    break;
                case A: 		dumpCamera();                         break;
                case D:        resetData();                    break;
                case B:        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 5);            break;
                case C:       resetData();                    break;
                case N:       cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 5);              break;
//                    case X:
//                        axisGroup.setVisible(!axisGroup.isVisible());
//                        break;
               case V:     wholePlate.setVisible(!wholePlate.isVisible());                break;
  
               case G:
                        RotateTransition rt = new RotateTransition(Duration.millis(3000));
                        rt.setAxis(new Point3D(1,0,0));
                        rt.setNode(wholePlate);
                        rt.setFromAngle(CAMERA_INITIAL_X_ANGLE);
                        rt.setToAngle(CAMERA_SECOND_X_ANGLE);
                        rt.play();
                        break;

                  
               default: break;
            }
        });

    }
	private void resetData(){		 model.resetData();		
	}
	public void setScalar(double d)	{		scalar.set(d);	}

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
 SimpleDoubleProperty scalar = new SimpleDoubleProperty(0);

//---------------------------------------------------------------------------------------------------
 private void buildPlate3D(PlateModel model) 
 {

//     final PhongMaterial redMaterial = new PhongMaterial();
//     redMaterial.setDiffuseColor(Color.ORANGE);
//     redMaterial.setSpecularColor(Color.RED);
	 wholePlate = new Xform();
     final PhongMaterial boxMaterial = new PhongMaterial();
     boxMaterial.setDiffuseColor(Color.CORNSILK.brighter().brighter());
     boxMaterial.setSpecularColor(Color.WHITE);
     boxMaterial.setSpecularPower(120000000);
     
     final PhongMaterial whiteMaterial = new PhongMaterial();
     whiteMaterial.setDiffuseColor(Color.WHITE);
     whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

     final PhongMaterial magentaMaterial = new PhongMaterial();
     Color c = new Color(249/ 256.0, 155/ 256.0, 240/ 256.0, 1.0);
     magentaMaterial.setDiffuseColor(c);
     magentaMaterial.setSpecularColor(Color.LIGHTCORAL);
     magentaMaterial.setSpecularPower(120000000);

      final PhongMaterial greyMaterial = new PhongMaterial();
      greyMaterial.setDiffuseColor(Color.DARKGREY);
      greyMaterial.setSpecularColor(Color.GREY);
      greyMaterial.setSpecularPower(120000000);
     
//      final PhongMaterial greenMaterial = new PhongMaterial();
//      greenMaterial.setDiffuseColor(Color.DARKGREEN);
//      greenMaterial.setSpecularColor(Color.GREEN);

      
//      Xform wholePlate = new Xform();
      Xform plate = new Xform();
//      Xform[][] wellColumns = new Xform[12][];
      
      int k = 77;
      int HEIGHT = 8;
      int WIDTH = 12;
      Box plateBox = new Box((WIDTH + 2) * k, (HEIGHT + 2) * k, k / 6);
      plateBox.setMaterial(boxMaterial);
      plate.getChildren().add(plateBox);
      int halfK = k / 2;
      int halfWidth = 6 * k;
      int halfHeight = 4 * k;
//      int fullWidth = WIDTH * k;
//      int fullHeight = 8 * k;
      for (int i=0; i < WIDTH; i++)
      {  
//      	Xform[] wellColumn = new Xform[HEIGHT];
      	for (int j=0; j<HEIGHT; j++)
            {
      		String wellId = Well.wellNotation96(i + 12 * j);
      		final Well well =  model.getWell(i,j);
      		DoubleProperty heightProperty = model.getWell(i,j).heightProperty(); 
      		Cylinder cyl = new Cylinder(k / 3, 10);
      		cyl.heightProperty().bind(heightProperty.multiply(scalar).add(10));
//      		cyl.setHeight(60);
          	cyl.setMaterial(magentaMaterial);
//          	if (2 * i == 3 * j) cyl.setMaterial(greyMaterial);		// dummy coloring
          
          	cyl.setOnMouseClicked(ev -> { well.doCircleClick(ev);	});
     	
          	cyl.setRotationAxis(new Point3D(1,0,0));
          	cyl.setRotate(90.);
          	cyl.setTranslateX(0 - (k * i - halfWidth + halfK));
          	cyl.setTranslateY(k * j - halfHeight + halfK);
          	cyl.setTranslateZ(5 + k / 8);   //k/8 + heightProperty.getValue() / 2);
          	cyl.translateZProperty().bind(heightProperty.multiply(scalar).divide(2).add(k/8));
          	Tooltip.install(cyl, new Tooltip(wellId));
          	//k/8 + heightProperty.getValue() / 2);
//       
    
     		Box block = new Box(k * 0.7, k * 0.1, k * 0.7);
//  		cyl.heightProperty().bind(heightProperty);
//     		block.setHeight(5 );
     		block.heightProperty().bind(heightProperty.multiply(scalar).add(5));
     		block.setMaterial(greyMaterial);
      
      	
     		block.setRotationAxis(new Point3D(1,0,0));
     		block.setRotate(90.);
     		block.setTranslateX(0 - (k * i - halfWidth + halfK));
     		block.setTranslateY(k * j - halfHeight + halfK);
     		block.setTranslateZ(5 + k / 8);   //k/8 + heightProperty.getValue() / 2);
     		block.translateZProperty().bind(heightProperty.multiply(scalar).divide(2).add(k/8));   //k/8 + heightProperty.getValue() / 2);

          	
          	
//          	c.setTranslateZ(k/8 + heightProperty.getValue() / 2);
          	Text wellDescription = new Text();
          	
          	wellDescription.setText(Well.wellNotation96(i + 12 * j));
          	wellDescription.setRotationAxis(new Point3D(0,1,0));
          	wellDescription.setFont(Font.font(null, FontWeight.LIGHT, 16));
          	wellDescription.setTranslateX(0 - (k * i - halfWidth + halfK + k / 4)); 
          	wellDescription.setTranslateY(k * j - halfHeight + halfK);
//          	wellDescription.setTranslateZ(- halfHeight );
          	wellDescription.setRotate(180.);
          	wellDescription.translateZProperty().bind(heightProperty.multiply(scalar).add(k/8+10));
//          	wellDescription.setTranslateZ(15 + k / 8);
          	wellDescription.setFill(Color.BLACK);
          	wellDescription.setTextOrigin(VPos.CENTER);
          	wellDescription.setMouseTransparent(true);
          	Group g = new Group();
           g.getChildren().addAll(cyl, block, wellDescription);
           	plate.getChildren().add(g);
          }
      }
      for (int i=0; i < WIDTH; i++)
      {  
       	Text header = new Text();
      	
       	header.setText("" + (i + 1));
       	header.setRotationAxis(new Point3D(0,1,0));
       	header.setFont(Font.font(null, FontWeight.LIGHT, 14));
       	header.setTranslateX(10 - (k * i - halfWidth + halfK + k / 4));  
       	header.setTranslateY( -4 * k - 20);
//      	wellDescription.setTranslateZ(- halfHeight );
       	header.setRotate(180.);
//      	wellDescription.translateZProperty().bind(heightProperty.add(10));
       	header.setTranslateZ(k / 8);
       	header.setFill(Color.BLACK);
       	header.setTextOrigin(VPos.CENTER);
 		
       	Text footer = new Text();
  		footer.setText("" + (i + 1));
  		footer.setRotationAxis(new Point3D(0,1,0));
  		footer.setFont(Font.font(null, FontWeight.LIGHT, 14));
  		footer.setTranslateX(10 - (k * i - halfWidth + halfK + k / 4));  
  		footer.setTranslateY( 4 * k + 40);
	//  	wellDescription.setTranslateZ(- halfHeight );
  		footer.setRotate(180.);
	//  	wellDescription.translateZProperty().bind(heightProperty.add(10));
  		footer.setTranslateZ(k / 8);
  		footer.setFill(Color.BLACK);
  		footer.setTextOrigin(VPos.CENTER);
  		
       	wholePlate.getChildren().addAll(header, footer);
      }
  	for (int j=0; j<HEIGHT; j++)
  	{       	
  		Text header = new Text();
	   	header.setText("" + (char)('A' + j));
	   	header.setRotationAxis(new Point3D(0,1,0));
	   	header.setFont(Font.font(null, FontWeight.LIGHT, 14));
	   	header.setTranslateX( 6 * k + 20);  				// move from center to left margin
	   	header.setTranslateY(k * j - halfHeight + halfK);
	//  	wellDescription.setTranslateZ(- halfHeight );
	   	header.setRotate(180.);
	//  	wellDescription.translateZProperty().bind(heightProperty.add(10));
	   	header.setTranslateZ(k / 8);
	   	header.setFill(Color.BLACK);
	   	header.setTextOrigin(VPos.CENTER);
	   	
  		Text footer = new Text();
  		footer.setText("" + (char)('A' + j));
  		footer.setRotationAxis(new Point3D(0,1,0));
  		footer.setFont(Font.font(null, FontWeight.LIGHT, 14));
  		footer.setTranslateX( -6 * k - 20);  				// move from center to left margin
  		footer.setTranslateY(k * j - halfHeight + halfK);
	//  	wellDescription.setTranslateZ(- halfHeight );
  		footer.setRotate(180.);
	//  	wellDescription.translateZProperty().bind(heightProperty.add(10));
  		footer.setTranslateZ(k / 8);
  		footer.setFill(Color.BLACK);
  		footer.setTextOrigin(VPos.CENTER);
  		
	   	wholePlate.getChildren().addAll(header, footer);
	}
  	wholePlate.getChildren().add(plate);
      root.getChildren().addAll(wholePlate);
      root.setRotateY(180);
      root.setRotateX(30);
      root.setTranslateY(30);
      root.setTranslateX(0);
  }

}
