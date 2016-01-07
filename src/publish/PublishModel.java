package publish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gui.Borders;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CSVTableData;
import model.GraphRequest;
import model.Histogram1D;
import model.HistogramRequest;
import model.OverlaidLineChart;
import model.ScatterRequest;

public class PublishModel
{
	List<String> tablenames = new ArrayList<String>();
	Map<String, CSVTableData> tablemap = new HashMap<String, CSVTableData>();
	Map<String, OverlaidLineChart> peakFitChartMap = new HashMap<String, OverlaidLineChart>();
	
	PublishController controller;
	VBox vbox;
	HBox hbox;
	
	public PublishModel(PublishController c)
	{
		controller = c;
	}

	//--------------------------------------------------------------------------------
	// returns true if empty, false if has content

	public boolean  processSegmentTables(ObservableList<Segment> items)
	{
		if (tablemap.isEmpty())
		{
			for (Segment seg : items)
			{	
				tablenames.add(seg.getName());
				tablemap.put(seg.getName(), seg.getData());
			}
		}
		return tablemap.isEmpty();
	}
	//--------------------------------------------------------------------------------
	public void profileHistograms(VBox container, ObservableList<Segment> items)
	{
		if (processSegmentTables(items)) return;	// fill it from cache
		Objects.requireNonNull(tablemap);
		Objects.requireNonNull(tablenames);
		HashMap<String, Map<String, Histogram1D>> datasetMap = new HashMap<String, Map<String, Histogram1D>>();
		HashMap<String, LineChart<Number, Number>> chartMap = new HashMap<String, LineChart<Number, Number>>();
		hbox = new HBox(8); hbox.setBorder(Borders.blueBorder1);
		vbox = new VBox(8); vbox.setBorder(Borders.greenBorder);
		vbox.prefWidthProperty().bind(hbox.widthProperty());
		vbox.prefHeightProperty().bind(hbox.heightProperty());
		container.getChildren().add(hbox);
		hbox.getChildren().add(vbox);

		final CSVTableData firstTable = tablemap.get(tablenames.get(0));
		
		for (String tablename : tablenames)
		{
			CSVTableData table = tablemap.get(tablename);
			if (table == null) { continue;  } 
			Map<String, Histogram1D> histos = table.getHistograms();
			for (String name : histos.keySet())
				if (histos.get(name) != null) 
					histos.get(name).calcDistributionStats();			// calculates mean, area & several percentiles that are never used
			datasetMap.put(table.getName(), histos);
		}
		Map<String, Histogram1D> firstDataset = firstTable.getHistograms();
		List<Histogram1D> sums = new ArrayList<Histogram1D>();
		int nCols = firstDataset.size();
//		 nCols = 9; // DEBUG
	
		for (int i = 0; i< nCols; i++)		
		{
			if (i > 2) continue;			// DEBUG
			String dim = EDLParsingHelper.dims[i];
			Histogram1D histo = firstDataset.get(dim);
			if (histo == null) continue;
			Histogram1D sum = new Histogram1D(histo);
			sums.add(sum);
//			LineChart<Number, Number> rawChart = histo.makeRawDataChart();	rawChart.setPrefWidth(200);
			LineChart<Number, Number> smoothedChart = histo.makeChart();	//smoothedChart.setPrefWidth(200);
			OverlaidLineChart peakFitChart = histo.makePeakFitChart();		//	peakFitChart.setPrefWidth(200);
			chartMap.put(histo.getName(), smoothedChart);
			peakFitChartMap.put(histo.getName(), peakFitChart);
//			Label statLabel = new Label(histo.getStatString());
//			statLabel.setMinWidth(100);
//			statLabel.setPrefWidth(600);
			peakFitChart.getXAxis().setTickLabelsVisible(true);		// use CSS
			peakFitChart.getYAxis().setTickLabelsVisible(false);

//			peakFitChart.setOnKeyTyped(ev -> { 	classify(firstTable);	});		// doesn't work -- ask for focus?
			peakFitChart.setOnMouseClicked(ev -> { 	classify(firstTable);	});
			peakFitChart.setLegendVisible(false);
//			peakFitChart.setLegendSide(Side.RIGHT);
			HBox dimensionBox = new HBox(peakFitChart);		//rawChart, smoothedChart, statLabel, 
			if (i < 13)
				vbox.getChildren().add(dimensionBox);
			
			// important:  we can't add the markers before the chart is shown!
			Thread th = new Thread(() -> Platform.runLater(() -> { histo.addPeakMarkers(peakFitChart);  }) );  
			th.start();
			new Thread(() -> Platform.runLater(() -> { container.layout();	})).start();
		}
		
	}
	public List<XYChart<Number, Number>> analyze1D(CSVTableData table)
	{
		classify(table);
		List<GraphRequest> requests = visualize1D();
		List<XYChart<Number, Number>> charts = table.process(requests);			// THREAD
		return charts;
		
	}
	
	
	
	//@formatter:off
	String ontology = " ( All (CD3- (CD19+ (CD27+ (CD38+ ()))))" + 
							" (CD3+ (CD4+ (CD25+ (CD39+ ())))))";

	String[] gates = new String []
			{	//	parent	gate	child-name
					"All", "CD3-", "CD3-",			
					"CD3-", "CD19+", "B",			
					"B", "CD27+", "CD27+B",			
					"B", "CD38+", "CD38+B" ,			
					"CD3+", "CD4+", "CD4",	
					"CD4", "CD25+", "CD25",	
					"CD25", "CD39+", "Treg",	
					"CD4", "CD161+", "TH17"	
			};

	String[] cmds1D = new String []
			{	//	  x		parent	child
					"CD3", "All", "CD3-",			
					"CD19", "CD3-", "CD19+",			
					"CD27", "CD19+", "CD27+B",			
					"CD38", "CD19+", "CD38+" ,			
					"CD38", "CD27+B", "Bplasma"		
			};

	String[] cmds2D = new String []
			{	//  	x		y	base-layer	overlay
					"CD38", "CD39", "CD27+B", "Bplasma",			
					"CD161", "CD4", "Bplasma", "",			
					"CD25", "CD161", "Bplasma", ""	
			};
	//@formatter:on
	//--------------------------------------------------------------------------------
	public void classify(CSVTableData table)
	{
		for (int i=0; i<gates.length; i+=3)
			table.addPColumn(gates[i], gates[i+1], gates[i+2]);
	}

	public List<GraphRequest> visualize1D()
	{
		List<GraphRequest> requests = new ArrayList<GraphRequest>();
//		for (String name : EDLParsingHelper.dims)		
//			requests.add(new HistogramRequest(name, "All", "CD3-", "CD19+", "B", "CD27+B", "CD38+", "Bplasma"));
		
		for (int i=0; i<cmds1D.length; i+=3)
			requests.add(new HistogramRequest(cmds1D[i], cmds1D[i+1], cmds1D[i+2]));
		return requests;
	}

	public List<GraphRequest> visualize2D()
	{
		List<GraphRequest> requests = new ArrayList<GraphRequest>();
		for (int i=0; i<cmds2D.length; i+=4)
			requests.add(new ScatterRequest(cmds2D[i], cmds2D[i+1], cmds2D[i+2], cmds2D[i+3])); 
		return requests;
	}

	//--------------------------------------------------------------------------------
	// For each table, generate histograms and put them into datasetMap.
	// Then, generate a histogram for each column add overlay another series for 
	// each table and the sum of all tables.
	
	public void processSegmentTables(VBox container, ObservableList<Segment> items)
	{
		if (processSegmentTables(items)) return;	// fill it from cache
		Objects.requireNonNull(tablemap);
		Objects.requireNonNull(tablenames);
		HashMap<String, Map<String, Histogram1D>> datasetMap = new HashMap<String, Map<String, Histogram1D>>();
		HashMap<String, LineChart<Number, Number>> chartMap = new HashMap<String, LineChart<Number, Number>>();
		
		CSVTableData firstTable = null;
		
		for (String tablename : tablenames)
		{
			CSVTableData table = tablemap.get(tablename);
			if (table == null) { continue;  } 
			if (firstTable == null) { firstTable = table;  } 
			datasetMap.put(table.getName(), table.getHistograms());
		}
		
		System.out.println("built histogram map");
		
		// process the first table to build sums and charts
		Map<String, Histogram1D> firstDataset = firstTable.getHistograms();
		System.out.println("adding First DataSet: " + firstTable.getName());
		Map<String, Histogram1D> sums = new HashMap<String, Histogram1D>();
		int nCols = firstDataset.size();
		
		for (String name : firstDataset.keySet())		
		{
//			if (i < 5) continue;
			Histogram1D histo = firstDataset.get(name);
			Histogram1D sum = new Histogram1D(histo);
			sums.put(name, sum);
			if (histo == null) continue;
			LineChart<Number, Number> chart = histo.makeChart();
			chartMap.put(histo.getName(), chart);
			System.out.println("adding chart for: " + histo.getName());
			container.getChildren().add(chart);
		}
		
		System.out.println("built chartMap");
		int tablenum = 0;
		double yOffset = controller != null && controller.getAddOffset() ?  0.03 : 0;		// draw overlays with a slight offset so all lines are visible
		// process the rest of the tables to increments sums, and add another series to the charts
		for (String name : tablenames)
		{
			tablenum++;
			CSVTableData tableData = tablemap.get(name);
			if (tableData == null)	continue;				// error
			if (tableData == firstTable)	continue;		// already processed above
			String tableName = tableData.getName();
			Map<String, Histogram1D> dataset = datasetMap.get(tableName);
			for (int i = 5; i< nCols; i++)		
			{
				Histogram1D sum =sums.get(i-5);
				Histogram1D distr = dataset.get(i);
				if (distr ==  null) continue;
				XYChart.Series series = distr.getDataSeries("all", tablenum  * yOffset);
				sum.add(distr);
				LineChart<Number, Number> chart = chartMap.get(distr.getName());
				if (chart == null) continue;		// error
				chart.getData().add(series);
			}
		}
		// add a data series for the sum of all other series
		if (controller != null && controller.getShowSum())
		for (int i =5; i< nCols; i++)		
		{
			Histogram1D sum =sums.get(i-5);
			XYChart.Series series = sum.getDataSeries(sum.getName());
			LineChart<Number, Number> chart = chartMap.get(sum.getName());
			if (chart == null) continue;		// error
			chart.getData().add(series);
		}
		System.out.println("built overlays");
		
		
	}

}
