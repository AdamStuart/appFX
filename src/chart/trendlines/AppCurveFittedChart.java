package chart.trendlines;  

import java.net.URL;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AppCurveFittedChart extends Application 
{
    public static void main(String[] args) {        launch(args);    }
    
    @Override public void start(Stage primaryStage) {
    	
    	TrendlineController controller = new TrendlineController();
    	VBox container = controller.createContent();
	    
    	Scene scene = new Scene(container, 500, 800);
	    URL url = getClass().getResource("CurveFittedChart.css");		// this gets the fxml file from the same directory as this class
	    scene.getStylesheets().add(url.toExternalForm());
	    primaryStage.setScene(scene);
	    primaryStage.show();
    }
}
