package chart;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AppThreadedCharts extends Application
{
	  //----------------------------------------------------------------------------------------
	 @Override public void start(Stage stage) throws IOException 
	 {
	    stage.setTitle("Chart Export Sample");
	    Pane layout = OffScreenOffThreadCharts.makeChartPane();
	    stage.setOnCloseRequest(ev -> { OffScreenOffThreadCharts.cancel(); });
	    stage.setScene(new Scene(layout));
	    stage.show();
	  }
	  
	  @Override public void stop() throws Exception {
		  OffScreenOffThreadCharts.shutdown();
	  }
	  
	  
	  public static void main(String[] args) { launch(args); }


}
