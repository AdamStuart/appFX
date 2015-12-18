package publish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.stanford.nlp.time.GenericTimeExpressionPatterns;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CSVTableData;
import model.Histogram1D;

public class PublishModel
{
	List<String> tablenames = new ArrayList<String>();
	Map<String, CSVTableData> tablemap = new HashMap<String, CSVTableData>();
	PublishController controller;
	
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
		HashMap<String, List<Histogram1D>> datasetMap = new HashMap<String, List<Histogram1D>>();
		HashMap<String, LineChart<Number, Number>> chartMap = new HashMap<String, LineChart<Number, Number>>();
		
		CSVTableData firstTable = null;
		
		for (String tablename : tablenames)
		{
			CSVTableData table = tablemap.get(tablename);
			if (table == null) { continue;  } 
			if (firstTable == null) { firstTable = table;  } 
			List<Histogram1D> histos = table.getHistograms();
			for (Histogram1D h : histos)
				if (h != null) 
					h.calcDistributionStats();
			datasetMap.put(table.getName(), histos);
		}
		List<Histogram1D> firstDataset = firstTable.getHistograms();
		System.out.println("adding First DataSet: " + firstTable.getName());
		List<Histogram1D> sums = new ArrayList<Histogram1D>();
		int nCols = firstDataset.size();
	
		for (int i = 0; i< nCols; i++)		
		{
			if (i < 5) continue;
			Histogram1D histo = firstDataset.get(i);
			Histogram1D sum = new Histogram1D(histo);
			sums.add(sum);
			if (histo == null) continue;
			LineChart<Number, Number> rawChart = histo.makeRawDataChart();
			LineChart<Number, Number> smoothedChart = histo.makeChart();
			LineChart<Number, Number> peakFitChart = histo.makePeakFitChart();
			chartMap.put(histo.getName(), smoothedChart);
			System.out.println("adding chart for: " + histo.getName());
			Label statLabel = new Label(String.format("Stats: \nMode: %.2f\nMedian: %.2f\n", histo.getModePosition(), histo.getMedian()));
			statLabel.setMinWidth(150);
			HBox dimensionBox = new HBox(statLabel, rawChart, smoothedChart, peakFitChart);
			container.getChildren().add(dimensionBox);
		}
				
	}
	//--------------------------------------------------------------------------------
	public void processSegmentTables(VBox container, ObservableList<Segment> items)
	{
		if (processSegmentTables(items)) return;	// fill it from cache
		Objects.requireNonNull(tablemap);
		Objects.requireNonNull(tablenames);
		HashMap<String, List<Histogram1D>> datasetMap = new HashMap<String, List<Histogram1D>>();
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
		List<Histogram1D> firstDataset = firstTable.getHistograms();
		System.out.println("adding First DataSet: " + firstTable.getName());
		List<Histogram1D> sums = new ArrayList<Histogram1D>();
		int nCols = firstDataset.size();
		
		for (int i = 0; i< nCols; i++)		
		{
			if (i < 5) continue;
			Histogram1D histo = firstDataset.get(i);
			Histogram1D sum = new Histogram1D(histo);
			sums.add(sum);
			if (histo == null) continue;
			LineChart<Number, Number> chart = histo.makeChart();
			chartMap.put(histo.getName(), chart);
			System.out.println("adding chart for: " + histo.getName());
			container.getChildren().add(chart);
		}
		
		System.out.println("built chartMap");
		int tablenum = 0;
		double yOffset = controller.getAddOffset() ?  0.03 : 0;
		// process the rest of the tables to increments sums, and add another series to the charts
		for (String name : tablenames)
		{
			tablenum++;
			CSVTableData tableData = tablemap.get(name);
			if (tableData == null)	continue;				// error
			if (tableData == firstTable)	continue;		// already processed above
			String tableName = tableData.getName();
			List<Histogram1D> dataset = datasetMap.get(tableName);
			for (int i = 5; i< nCols; i++)		
			{
				Histogram1D sum =sums.get(i-5);
				Histogram1D distr = dataset.get(i);
				if (distr ==  null) continue;
				XYChart.Series series = distr.getDataSeries(tablenum  * yOffset);
				sum.add(distr);
				LineChart<Number, Number> chart = chartMap.get(distr.getName());
				if (chart == null) continue;		// error
				chart.getData().add(series);
			}
		}
		// add a data series for the sum of all other series
		if (controller.getShowSum())
		for (int i =5; i< nCols; i++)		
		{
			Histogram1D sum =sums.get(i-5);
			XYChart.Series series = sum.getDataSeries();
			LineChart<Number, Number> chart = chartMap.get(sum.getName());
			if (chart == null) continue;		// error
			chart.getData().add(series);
		}
		System.out.println("built overlays");
		
		
	}

}
