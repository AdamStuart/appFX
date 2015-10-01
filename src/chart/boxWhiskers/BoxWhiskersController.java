package chart.boxWhiskers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

public class BoxWhiskersController
{
	public BoxWhiskersController()
	{
		
	}
    // DAY, MIN, 25, 50, 75, MAX
    private static double[][] data = new double[][]{
            {1,  2, 8, 32, 66, 120},
            {2,  3, 10, 33, 62, 125},
            {3,  4, 28, 40, 70, 132},
            {4,  2, 10, 34, 72, 130},
            {5,  5, 16, 40, 64, 132},
            {6,  1, 18, 45, 75, 214},
            {7,  5, 20, 44, 68, 139},
            {8,  3, 18, 36, 96, 131},
            {9,  4, 10, 52, 66, 141},
            {10, 8, 14, 38, 78, 136},
            {11, 4, 12, 30, 88,  132.4},
            {12, 4, 20, 46, 85, 131.6},
            {13, 4, 18, 36, 74, 232.6},
            {14, 5, 30, 40, 56, 250.6},
            {15, 4, 33, 40, 58, 220.6}
    };
    private BoxWhiskerChart chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
           
    XYChart.Data<Number,Number> makeDataPoint(double[] day)
    {
    	return new XYChart.Data<Number,Number>(day[0], 0, new BoxWhiskerDistribution(day));
    }
    
    public void createContent(Pane container) {
        xAxis = new NumberAxis(0,16,1);
        xAxis.setMinorTickCount(0);
        yAxis = new NumberAxis();
        chart = new BoxWhiskerChart(xAxis,yAxis);
        
        xAxis.setLabel("Day");					// setup chart
        yAxis.setLabel("Values");
        XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();    // add starting data
        for (int i=0; i< data.length; i++) 
            series.getData().add(makeDataPoint(data[i]));

        ObservableList<XYChart.Series<Number,Number>> data = chart.getData();
        if (data == null) 
        {
            data = FXCollections.observableArrayList(series);
            chart.setData(data);
        } 
        else chart.getData().add(series);
        
        container.getChildren().add(chart);
    }
    
}
