package chart;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppChartTabs extends Application
{
    public static void main(String[] args)	    {  launch(args);	    }
	   static final String RESOURCE = "chartTabs.fxml";
	    static final String STYLE = "chartTabs.css";
	    static AppChartTabs instance;
	    public Stage getStage()	{ return stage;	}
	    Stage stage;
	    
	    @Override
	    public void start(Stage primaryStage) throws Exception
	    {
	    	instance = this;
	    	stage = primaryStage;
	        URL resource = getClass().getResource(RESOURCE);
	        Scene scene = new Scene(FXMLLoader.load(resource));
			scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());


	        primaryStage.setTitle("Sample Chart Collection");
	        primaryStage.setResizable(true);
	        primaryStage.setScene(scene);
	        primaryStage.show();
	    }
	    static public AppChartTabs getInstance()	{ return instance;	}


}
