package chart.flexiPie;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class AppFlexiPie extends Application 
{

   public static void main(final String[] args) {   launch(args);   }


    @Override public  void start(Stage stage) throws Exception 
    {
//    	FlexiPieController ctl = new FlexiPieController();
//    	VBox content = ctl.createContent();
 		String name = "chart/flexiPie/FlexiPie.fxml";
		URL res = AppFlexiPie.class.getClassLoader().getResource(name);
		try
		{
		    String s = AppFlexiPie.class.getResource("chart.css").toExternalForm();
			AnchorPane flexiPieRoot = (AnchorPane) FXMLLoader.load(res);
			Scene scene = new Scene(flexiPieRoot, 900, 600);
		    scene.getStylesheets().add(s);	    
		    stage.setScene(scene);
		    stage.show();
		}
		catch (Exception e) {e.printStackTrace();}
		
		
    }

      //---------------------------------------------------------------------------
 }