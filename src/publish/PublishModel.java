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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.CSVTableData;
import model.Histogram1D;
import model.OverlaidLineChart;
import model.OverlaidScatterChart;
import util.StringUtil;

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
		HashMap<String, List<Histogram1D>> datasetMap = new HashMap<String, List<Histogram1D>>();
		HashMap<String, LineChart<Number, Number>> chartMap = new HashMap<String, LineChart<Number, Number>>();
		hbox = new HBox(8); hbox.setBorder(Borders.blueBorder1);
		vbox = new VBox(8); vbox.setBorder(Borders.greenBorder);
		container.getChildren().add(hbox);
		hbox.getChildren().add(vbox);

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
		List<Histogram1D> sums = new ArrayList<Histogram1D>();
		int nCols = firstDataset.size();
//		 nCols = 9; // DEBUG
	
		for (int i = 0; i< nCols; i++)		
		{
			if (i < 5) continue;
			Histogram1D histo = firstDataset.get(i);
			Histogram1D sum = new Histogram1D(histo);
			sums.add(sum);
			if (histo == null) continue;
			LineChart<Number, Number> rawChart = histo.makeRawDataChart();	rawChart.setPrefWidth(200);
			LineChart<Number, Number> smoothedChart = histo.makeChart();	smoothedChart.setPrefWidth(200);
			OverlaidLineChart peakFitChart = histo.makePeakFitChart();		//	peakFitChart.setPrefWidth(200);
			peakFitChartMap.put(histo.getName(), peakFitChart);
			chartMap.put(histo.getName(), smoothedChart);
			Label statLabel = new Label(histo.getStatString());
			statLabel.setMinWidth(100);
		
			

			peakFitChart.setOnKeyTyped(ev -> { 	classify();	});
			peakFitChart.setOnMouseClicked(ev -> { 	classify();	});
			peakFitChart.setLegendVisible(true);
			peakFitChart.setLegendSide(Side.RIGHT);
			HBox dimensionBox = new HBox(peakFitChart);		//rawChart, smoothedChart, statLabel, 
			if (i < 13)
				vbox.getChildren().add(dimensionBox);
			
			// important:  we can't add the markers before the chart is shown!
			Thread th = new Thread(() -> Platform.runLater(() -> { histo.addPeakMarkers(peakFitChart);  }) );  
			th.start();
		}
		
	}
	
	public void classify()
	{
		// TODO -- put a semaphore here to make sure this isn't run ahead of scanPeaks
		CSVTableData firstTable = null;
		for (String tablename : tablenames)
		{
			CSVTableData table = tablemap.get(tablename);
			if (firstTable == null) { firstTable = table;  } 
		}
		List<Histogram1D> firstDataset = firstTable.getHistograms();
		Histogram1D cd3 = firstDataset.get(5);
		Histogram1D cd19 = firstDataset.get(8);
		Histogram1D cd27 = firstDataset.get(12);
		Histogram1D cd38 = firstDataset.get(9);
		final CSVTableData table = firstTable;
		table.addPColumnPeakIndex("CD3+", cd3, 1);
		table.addPColumnPeakIndex("CD3-", cd3, 0);
		table.addPColumnAbove("CD19+", cd19, cd19.getPeaks().get(0).getMax());
		table.addPColumnAnd("B", "CD3-", "CD19+");
		table.addPColumnAbove("CD27+", cd27, cd27.getPeaks().get(0).getMax());
		table.addPColumnAnd("CD27+B", "B", "CD27+");
		table.addPColumnAbove("CD38+", cd38, cd38.getPeaks().get(0).getMax());
		table.addPColumnAnd("Bplasma", "CD27+B", "CD38+");
		int nCols = firstDataset.size();
//		 nCols = 9; // DEBUG
		for (int i = 0; i< nCols; i++)		
		{
			if (i < 5) continue;
			Histogram1D histo = firstDataset.get(i);
			OverlaidLineChart peakFitChart = peakFitChartMap.get(histo.getName());
			table.makeGatedHistogramOverlay(histo, peakFitChart, .005, "CD3-", "CD19+", "B", "CD27+B", "CD38+", "Bplasma");
		}
//		
		OverlaidLineChart cd3Neg = table.showGatedHistogram("All", "CD3-", "CD3"); 
		OverlaidLineChart cd19Pos = table.showGatedHistogram("CD3-", "CD19+", "CD19"); 
		OverlaidLineChart cd27Pos = table.showGatedHistogram("B", "CD27+B", "CD27"); 
		OverlaidLineChart cd38Pos = table.showGatedHistogram("B", "CD38+",  "CD38"); 
		OverlaidLineChart bPlasma = table.showGatedHistogram("CD27+B", "Bplasma", "CD38"); 
		
		OverlaidScatterChart<Number, Number> plot27 = table.getGatedScatterChart("CD27+B", "CD38", "CD39"); 
		OverlaidScatterChart<Number, Number> plotPlasma = table.getGatedScatterChart("Bplasma", "CD161", "CD4"); 
		OverlaidScatterChart<Number, Number> plot25 = table.getGatedScatterChart("Bplasma", "CD25", "CD161"); 
		hbox.getChildren().add(new VBox(5, cd3Neg, cd19Pos, cd27Pos, cd38Pos, bPlasma, plot27, plot25, plotPlasma));
		
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
		double yOffset = controller != null && controller.getAddOffset() ?  0.03 : 0;		// draw overlays with a slight offset so all lines are visible
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
