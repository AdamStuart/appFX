package publish;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CSVTableData;
import model.GraphRequest;

public class TestPeakFitter extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	
    
//	String path = "M230210_Test.csv";
    static final String STYLE = "publish.css";
//    /appFX/src/publish/M230210_Test.csv
    VBox col1 = new VBox();
//    VBox col2 = new VBox();
//    VBox col3 = new VBox();
    SplitPane page = new SplitPane(col1);		//,col2, new ScrollPane(col3)
    StackPane stack = new StackPane();
//    URL resource = getClass().getResource(path);
	@Override public void start(Stage stage) throws Exception
	{
		// TODO Auto-generated method stub
//        if (resource == null)
//        	System.out.println("resource == null");
        
        Scene scene = new Scene(page);
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
		model.profileHistograms(col1, items);
		
//		CSVTableData tableData = testSegment.getData();
//		List<XYChart<Number, Number>> results = model.analyze1D(tableData);
//		for (XYChart<Number, Number> c : results)
//			col2.getChildren().add(c);
		
//		List<GraphRequest> requests = model.visualize2D();
//		List<XYChart<Number, Number>> charts = tableData.process(requests);
//		for (XYChart<Number, Number> c : charts)
//			if (c != null)
//				col3.getChildren().add(c);
 	}

}
