
package threeD;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.stage.Stage;

/**
 * based on Oracles MoleculeSampleApp
 * @author cmcastil
 */
public class PlateSampleApp extends Application {

    final Group root = new Group();

     @Override
     public void start(Stage primaryStage) 
     {
         
        // setUserAgentStylesheet(STYLESHEET_MODENA);
         System.out.println("start()");
         WorldView3D world = new WorldView3D(root);
         root.getChildren().add(world);
         root.setDepthTest(DepthTest.ENABLE);
         initModel();
         Xform wholePlate = new PlateXForm(model);
         world.getChildren().add(wholePlate);
         buildControlSubscene();

         primaryStage.setTitle("3D Plate Editor Prototype");
         primaryStage.setScene(world.get3DScene());
         primaryStage.show();
     }

    
     private void buildControlSubscene() {
		// TODO put some controls here.  
    	 // List of attribute values
    	 // staging wells
		
	}


//---------------------------------------------------------------------------------------------------
 
 
   // this needs to be replaced with bindings to the model
   List<DoubleProperty> model;
   private void initModel()
   {
	   model =  IntStream.range(0,96)
			   .mapToObj(SimpleDoubleProperty::new)
			   .collect(Collectors.toList());
   }


//---------------------------------------------------------------------------------------------------
     public static void main(String[] args) {       launch(args);    }
}
