package chart.histograms;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import model.FCSFileReader;
import model.Histogram1D;

public class HistogramChartController implements Initializable
{
  @FXML private  LineChart<Number, Number> histogramChart;
  @FXML  private NumberAxis xAxis;
  @FXML  private NumberAxis yAxis;
  @FXML  private StackPane chartContainer;
 
//  // use this if you don't use FXML to define the chart
//  public HistogramChartController(StackPane parent)
//  {
//	  chartContainer = parent;
//	  xAxis = new NumberAxis();
//	  yAxis = new NumberAxis();
//	  histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
//	  initialize(null, null);
//  }
	@Override public void initialize(URL url, ResourceBundle rb)
	{
	    System.out.println("HistogramChartController.initialize");
		assert (histogramChart != null);
	    assert( chartContainer != null);
		buildData();
		new SubRangeLayer(histogramChart, chartContainer);
		// this.barChart.getData().addAll(data);
	}

	public void buildData()
	{
		histogramChart.setTitle("DATA LOAD ERROR!");
		String path = "/Users/adam/Desktop/SBS00045.A04 DEC16 D1.2.fcs";
		Histogram1D h1 = null, h2 = null;
//		if (path == null)
//		{
//			FileChooser choose = new FileChooser();
//			File f = choose.showOpenDialog(AppHistograms.getStage());		// TODO doesn't work in ChrtTabs
//			if (f != null)
//				path = f.getAbsolutePath();
//			System.out.println("Path: " + path);
//		}
		File f = new File(path);
		try
		{
			FCSFileReader reader = new FCSFileReader(f);
			h1 = reader.getHistogram1D(0, 200);
			h2 = reader.getHistogram1D(1, 200);
		} 
		catch (Exception e) { e.printStackTrace(); }
		histogramChart.setTitle("Histogram Chart");
		histogramChart.setCreateSymbols(false);
		histogramChart.getData().add( h1.getDataSeries("Col1"));
		histogramChart.getData().add(h2.getDataSeries("Col2"));	
//		String s = getClass().getResource("no-symbols.css").toExternalForm();
//		histogramChart.getStylesheets().add(s);

//		  for (Node symbol: histogramChart.lookupAll(".chart-area-symbol"))
//		  {
//		      symbol.setVisible(false);
//		      symbol.setManaged(false);
//		    }
	}


}
