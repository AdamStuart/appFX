package chart.timeseries;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AppTimeSeries extends Application 
{
    public static void main(final String[] args) {        Application.launch(args);    }
    static private String FXML = "";

    @Override public void start(Stage stage) throws Exception 
    {
    	me = this;
	    FXMLLoader fxmlLoader = new FXMLLoader();
	    URL url = getClass().getResource(FXML + "TimeSeries.fxml");
	    fxmlLoader.setLocation(url);
	    VBox appPane = fxmlLoader.load();
	    
	    Scene scene = new Scene(appPane, 1000, 800);
	    stage.setScene(scene);
	    stage.show();
    }

	 static public AppTimeSeries getInstance()	{ return me;	}
	 static private AppTimeSeries me;
	 static private Stage theStage;
	 public Stage getStage() { return theStage;  }
     
}