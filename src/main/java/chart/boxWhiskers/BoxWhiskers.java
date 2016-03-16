// comes from Oracles Candlestick example   http://docs.oracle.com/javafx/2/charts/chart-overview.htm
package chart.boxWhiskers;

import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import util.LineUtil;
import util.RectangleUtil;

/** BoxWhiskers is a group node used for drawing a distribution of values 
 * with lines and boxes. 
 * 
 * Also know as Box Plots
 * This variation shows the minimun, maximum, and 25th, 50th, and 75th percentiles values
 * 
 * http://en.wikipedia.org/wiki/Box_plot
 *  */
public class BoxWhiskers extends Group {
	Line maxLine, minLine, median, axis;
	Rectangle box;
    private String seriesStyleClass;
    private String dataStyleClass;
    private Tooltip tooltip = new Tooltip();

    BoxWhiskers(String seriesStyleClass, String dataStyleClass) {
        setAutoSizeChildren(false);
        this.seriesStyleClass = seriesStyleClass;
        this.dataStyleClass = dataStyleClass;
        updateStyleClasses();
        tooltip.setGraphic(new BoxWhiskerTooltipContent());
		maxLine = new Line();
		minLine = new Line();
		median = new Line();
		axis = new Line();
		box = new Rectangle();
		box.setStroke(Color.YELLOW);
		box.setFill(Color.GREEN);
        Tooltip.install(box, tooltip);
		LineUtil.set(maxLine, 0, 0, 1000, 1000);
        maxLine.setStrokeWidth(2);
        minLine.setStrokeWidth(2);
        median.setStrokeWidth(2);
//        setLayoutX(30);
//        setLayoutY(30);
		getChildren().addAll(maxLine, minLine, axis, box, median);
//		box.setVisible(true);
//		minLine.setVisible(true);
//		median.setVisible(true);
//		axis.setVisible(true);
//		maxLine.setVisible(true);
    }

	
	
	public void update(double[] distrib, double x, double w)
	{
		double left, max, min, right;
		double pt25, pt50, pt75;
		left = x - w /2 ;
		right = left + w;
		assert(distrib.length == 6);
		
		min =   distrib[1];
		pt25 =   distrib[2];
		pt50 =   distrib[3];
		pt75 =   distrib[4];
		max =   distrib[5];
		
		LineUtil.set(axis, x, max, x, min);
		LineUtil.set(maxLine, left, max, right, max);
		LineUtil.set(minLine, left, min, right, min);
		LineUtil.set(median, left, pt50, right, pt50);
		RectangleUtil.setRect(box,  left, pt75, Math.abs(right - left), Math.abs(pt75 - pt25));
	}
	    
	public void setSeriesAndDataStyleClasses(String seriesStyleClass, String dataStyleClass) {
        this.seriesStyleClass = seriesStyleClass;
        this.dataStyleClass = dataStyleClass;
        updateStyleClasses();
    }

    
    public void updateTooltip(double[] vals) {
        BoxWhiskerTooltipContent tooltipContent = (BoxWhiskerTooltipContent) tooltip.getGraphic();
        tooltipContent.update(vals);
    }

    private void updateStyleClasses() {
        getStyleClass().setAll("box-body", seriesStyleClass, dataStyleClass);
//        
//        String desc = openAboveClose ? "open-above-close" : "close-above-open";
//        highLowLine.getStyleClass().setAll("candlestick-line", seriesStyleClass, dataStyleClass,desc);
//        bar.getStyleClass().setAll("candlestick-bar", seriesStyleClass, dataStyleClass,	desc);
//   
    	}
}