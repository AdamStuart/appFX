package chart.histograms;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class SubRangeGroup extends Group
{
	int resizing = 0;
	double dragStart = -1;
	double SLOP = 4;
	SubRangeLayer parent;
	
	public SubRangeGroup(XYChart<Number, Number> inChart, SubRangeLayer layer)
	{
		parent = layer;
		chart = inChart;
		leftBar = new Line(20, 10, 20, 399);
		crossBar = new Line(20, 100, 220, 100);
		rightBar = new Line(220, 10, 220, 399);
		update(0,100, 0);
		
		leftBar.setStrokeWidth(4);		leftBar.setStroke(Color.PURPLE);
		crossBar.setStrokeWidth(2);		crossBar.setStroke(Color.PURPLE);
		rightBar.setStrokeWidth(4);		rightBar.setStroke(Color.PURPLE);
		getChildren().addAll(leftBar, crossBar, rightBar);
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		setOnMousePressed((event) -> {
				double left = leftBar.getStartX();
				double right = rightBar.getStartX();
				if (left > right)
				{
					double temp = right;
					right = left;
					left = temp;
				}
				double x = dragStart = event.getX();
				System.out.println("left: " + left);
				System.out.println("right: " + right);
				System.out.println("dragStart: " + dragStart);

				if (event.isSecondaryButtonDown()) 		return;			// TODO do menu for a right-click
				parent.setSelectionStart(left);	
				parent.setSelectionEnd(right);	
				
				if (Math.abs(left - x) < SLOP)			resizing = 1;		
				else if (Math.abs(right - x) < SLOP)	resizing = 2;	
				else 									resizing = 0;
				System.out.println("resizing: " + resizing);
				event.consume();
			});
		setOnMouseDragged((event) -> {
				if (event.isSecondaryButtonDown()) 			return;
				if (resizing > 0)
				{
					if (resizing == 1) 	parent.setSelectionStart(event.getX());
					else 				parent.setSelectionEnd(event.getX());
					double min = parent.getSelectionMin();
					double max = parent.getSelectionMax();
					update(min, max, event.getY());
				}
				else
				{
					double dx = event.getX() - dragStart;
					parent.moveSelection(dx);
					update(parent.getSelectionMin(), parent.getSelectionMax(), event.getY());
					dragStart = event.getX();
				}
				event.consume();			
			});
		setOnMouseReleased((event) -> {
				if (resizing > 0 && parent.isSelectionSizeTooSmall()) 				return;
				
				parent.setAxisBounds();
				parent.setSelectionStart(-1);		parent.setSelectionEnd(-1);
				resizing = 0;
				parent.getPane().requestFocus();	// needed for the key event handler to receive events
				event.consume();				
			});
	}
	
	XYChart<Number, Number> chart;
	Line leftBar, crossBar, rightBar;
	
	public void update(double selStart, double selEnd, double y)
	{
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		double top = chartPlotArea.getLayoutY() + 30;				//TODO  mystery fudge factor
		double bottom = top + chartPlotArea.getLayoutBounds().getHeight();
				
		leftBar.setStartX(selStart);	leftBar.setStartY(top);		leftBar.setEndX(selStart);		leftBar.setEndY(bottom);
		rightBar.setStartX(selEnd);		rightBar.setStartY(top);	rightBar.setEndX(selEnd);		rightBar.setEndY(bottom);
		crossBar.setStartX(selStart);	crossBar.setStartY(y);		crossBar.setEndX(selEnd);		crossBar.setEndY(y);
			
	}

	public boolean testHit(double x)
	{
		if (leftBar == null || rightBar == null) return false;
		return x > leftBar.getStartX() && x < rightBar.getStartX();
	}
}
