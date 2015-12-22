package publish;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CSVTableData;
import model.Histogram1D;

public class TestPeakFitter extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	
    
//	String path = "M230210_Test.csv";
    static final String STYLE = "publish.css";
//    /appFX/src/publish/M230210_Test.csv
    VBox vbox = new VBox();
    StackPane stack = new StackPane();
//    URL resource = getClass().getResource(path);
	@Override public void start(Stage stage) throws Exception
	{
		// TODO Auto-generated method stub
//        if (resource == null)
//        	System.out.println("resource == null");
        
        Scene scene = new Scene(vbox);
		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
        stage.setTitle("Testing Curve Fitting");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(1150);
		createContent();
		stage.setScene(scene);
		stage.show();

	}
	private void createContent()
	{
		PublishModel model = new PublishModel(null);
		ObservableList<Segment> items = FXCollections.observableArrayList();
		Segment testSegment = new Segment("M230210", new File("/Users/adam/git/appFX/bin/publish/M230210_Test.csv"));
		items.add(testSegment);
		model.profileHistograms(vbox, items);
		
		
 	}

}
