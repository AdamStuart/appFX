package chart.trendlines;

import static thorwin.math.Math.polyfit;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

// http://fxexperience.com/2012/01/curve-fitting-and-styling-areachart/


public class TrendlineController
{
	AppCurveFittedChart application;

	public TrendlineController()
	{
	}

	public VBox createContent()
	{
		VBox container = new VBox(12);
		CurvedFittedAreaChart chart = new CurvedFittedAreaChart(new NumberAxis(), new NumberAxis());
		chart.setLegendVisible(false);
		chart.setHorizontalGridLinesVisible(false);
		chart.setVerticalGridLinesVisible(false);
		chart.setAlternativeColumnFillVisible(false);
		chart.setAlternativeRowFillVisible(false);

		// make a bell curve
		double mean = 0; 
		double stdev = 1;
		double inc = .5;
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		for (double d=-3; d <= 3; d += inc)
			series.getData().add(new  XYChart.Data<Number, Number>(d, gauss(d, mean, stdev)));
		chart.getData().add(series);
		container.getChildren().add(chart);

		//-----------------------
	    // http://thorwin.blogspot.nl/2015/03/trend-curveline-in-javafx-chart.html
	    double[] xs = new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
	    double[] ys =new double[] {0.5, 1.3, 0.4, 5.6, 2.8, 9.1};
		
		XYChart.Series<Number, Number> data = new XYChart.Series<Number, Number>();
		for (int i = 0; i < xs.length; i++)
			data.getData().add(new XYChart.Data<>(i, ys[i]));
	        
	   List<XYChart.Series<Number,Number>> allFits = new ArrayList<XYChart.Series<Number,Number>>();

       for (int i=0; i<ys.length-1;i++)
    	   allFits.add(new XYChart.Series<>());

        // calculate the polynomial coefficients and calculate trend points
       
       for (int i=1; i<allFits.size(); i++)
		{
			double[] coefficients = polyfit(xs, ys, i);
			for (double x = 0; x <= 5.0; x += 0.25)
			{
				double y = thorwin.math.Math.polynomial(x, coefficients);
				allFits.get(i).getData().add(new XYChart.Data<>(x, y));
			}
		}
      
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number,Number> linechart = new LineChart<Number, Number>(xAxis, yAxis);
        linechart.getData().add(data);
        linechart.getData().addAll(allFits);

		//-----------------------
        container.getChildren().add(linechart);
		return container;
	}

	public static double gauss(double x,double mn,double sd)
	{
		double exp = -((x-mn) * (x-mn)) / (2 * sd  * sd);
		double scale = 1 / (sd) * Math.sqrt(2 * Math.PI);
		double val = scale * Math.exp(exp);
		System.out.println(String.format("%.2f",  val));
		return val;
	}

}
