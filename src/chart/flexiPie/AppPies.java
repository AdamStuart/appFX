package chart.flexiPie;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AppPies extends Application 
{

   public static void main(final String[] args) {   launch(args);   }


    @Override public  void start(Stage stage) throws Exception 
    {
//    	FlexiPieController ctl = new FlexiPieController();
//    	VBox content = ctl.createContent();
		String name = "chart/flexiPie/FlexiPie.fxml";
		URL res = AppPies.class.getClassLoader().getResource(name);
		try
		{
			AnchorPane flexiPieRoot = (AnchorPane) FXMLLoader.load(res);
			Scene scene = new Scene(flexiPieRoot, 1000, 800);
		    stage.setScene(scene);
		    String s = AppPies.class.getResource("chart.css").toExternalForm();
		    scene.getStylesheets().add(s);	    
		    stage.show();
		}
		catch (Exception e) {}
		
    }

      //---------------------------------------------------------------------------
 }