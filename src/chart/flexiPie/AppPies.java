package chart.flexiPie;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AppPies extends Application 
{

   public static void main(final String[] args) {   launch(args);   }


    @Override public  void start(Stage stage) throws Exception 
    {
    	FlexiPieController ctl = new FlexiPieController();
    	VBox content = ctl.createContent();
	    Scene scene = new Scene(content, 1000, 800);
	    stage.setScene(scene);
	    stage.show();
    }

      //---------------------------------------------------------------------------
 }