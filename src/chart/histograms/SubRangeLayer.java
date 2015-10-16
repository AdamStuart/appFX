package chart.histograms;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


public class SubRangeLayer			// a 1D GateLayer
{
/*


/**
 * This class adds a layer on top of a histogram chart. 
 *
 */

	private static final String INFO_LABEL_ID = "zoomInfoLabel";
	private Pane pane;
	public Pane getPane()	{ return pane;	}
	private LineChart<Number, Number> chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	private SubRangeGroup selectionH;
	private Label infoLabel;

	private double selectionStart = -1;
	private double selectionEnd = -1;
	public boolean isSelectionSizeTooSmall() {	return 10 > Math.abs(selectionEnd - selectionStart);	}

	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";

	/**
	 * Create a new instance of this class with the given chart and pane instances. The {@link Pane} instance is needed
	 * as a parent for the group (H) that represents the user selection.
	 * 
	 * @param chart
	 *            the xy chart to which the zoom support should be added
	 * @param pane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public SubRangeLayer(LineChart<Number, Number> chart, Pane pane) 
	{
		this.pane = pane;
		this.chart = chart;
		this.xAxis = (NumberAxis) chart.getXAxis();
		this.yAxis = (NumberAxis) chart.getYAxis();
		selectionH = new SubRangeGroup(chart, this);  // SelectionRectangle();
		selectionH.setManaged(false);
//		selectionRectangle.setFill(null);
		selectionH.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		selectionH.setStyle("-fx-fillcolor: Color.GREEN; -fx-strokewidth: 4;");
		pane.getChildren().add(selectionH);
		addDragSelectionMechanism();
		addInfoLabel();
	}

	/**
	 * The info label shows a short info text that tells the user how to unreset the zoom level.
	 */
	private void addInfoLabel() {
		infoLabel = new Label("Subrange info goes here");
		infoLabel.setId(INFO_LABEL_ID);
		pane.getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
	}
	/**-------------------------------------------------------------------------------
	 *   Mouse handlers for clicks inside the selection rectangle
	 */
//	private void drawSelectionRectangle(final double x, final double y) {
//		selectionH.setVisible(true);
//		selectionH.setLayoutX(x);
//		selectionH.setLayoutY(y);
////		selectionH.setWidth(width);
////		selectionH.setHeight(height);
//	}
//	private void drawSelectionRectangleAt(final double x, final double y) {
//		drawSelectionRectangle(x,y);
//	}
	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	private void showInfo() {			infoLabel.setVisible(true);		}


	public void setAxisBounds() 
	{
		disableAutoRanging();
		if (selectionStart < 0 || selectionEnd < 0)
		{
			selectionStart = selectionH.getLayoutX();
			selectionEnd = selectionH.getLayoutX() + selectionH.getBoundsInLocal().getWidth();
		}
		// compute new bounds for the chart's x and y axes
		double selectionMinX = Math.min(selectionStart, selectionEnd);
		double selectionMaxX = Math.max(selectionStart, selectionEnd);

		double xMin = frameToScaleX(selectionMinX);
		double xMax = frameToScaleX(selectionMaxX);
		
		double freq = getGateFreq(chart, xMin, xMax);
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = fmt.format(freq * 100) +  "% for ( " + fmt.format(xMin) + ", " + fmt.format(xMax) +  ")";
		infoLabel.setText(s);
		showInfo();
	}
	
	private double getGateFreq(XYChart<Number, Number> chart, double xMin, double xMax)
	{
		int ct = 0;
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		for (Data<Number, Number> n : data.getData())
		{
			double x = n.getXValue().doubleValue();
			double y = n.getYValue().doubleValue();
			if ( (x >= xMin && x < xMax))
				ct++;
		}
		return ct / (double) data.getData().size();
		
	}
	/**
	 * Adds a mechanism to select an area in the chart that should be displayed at larged scale.
	 */
	private void addDragSelectionMechanism() {
		pane.setOnMousePressed((event) -> {
			if (event.isSecondaryButtonDown()) 		return;		// do nothing for a right-click

			// store position of initial click
			selectionStart = event.getX();
			selectionH.setVisible(true);
			event.consume();
		});
		pane.setOnMouseDragged((event) -> {
			if (event.isSecondaryButtonDown()) 		return;
			
			// store current cursor position
			selectionEnd = event.getX();
			selectionH.update(selectionStart, selectionEnd, event.getY());
			event.consume();
		});
		pane.setOnMouseReleased((event) -> {
			if (selectionStart < 0 || selectionEnd < 0) 		return;
			if (isSelectionSizeTooSmall()) 		return;

			setAxisBounds();
			selectionStart = selectionEnd = -1;
			pane.requestFocus();		// needed for the key event handler to receive events
			event.consume();
		
		});
		pane.setOnKeyReleased((event) -> {		});
	}

	/**-------------------------------------------------------------------------------
	 *
	 */
	
	
	private double frameToScaleX(double value)
	{
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		double chartZeroX = chartPlotArea.getLayoutX();
		double chartWidth = chartPlotArea.getLayoutBounds().getWidth();
		return computeBound(value, chartZeroX, chartWidth, xAxis.getLowerBound(), xAxis.getUpperBound(), false);
	}
	
	private double computeBound(double pixelPosition, double pixelOffset, double pixelLength, double lowerBound,
			double upperBound, boolean axisInverted) {
		double pixelPositionWithoutOffset = pixelPosition - pixelOffset;
		double relativePosition = pixelPositionWithoutOffset / pixelLength;
		double axisLength = upperBound - lowerBound;

		// The screen's y axis grows from top to bottom, whereas the chart's y axis goes from bottom to top.
		// That's why we need to have this distinction here.
		double offset = 0;
		int sign = 0;
		if (axisInverted) {		offset = upperBound;	sign = -1;		} 
		else 			  {		offset = lowerBound;	sign = 1;		}

		double newBound = offset + sign * relativePosition * axisLength;
		return newBound;
	}

	public double getSelectionMin()		{		return Math.min(selectionStart, selectionEnd);		}
	public double getSelectionMax()		{		return Math.max(selectionStart, selectionEnd);		}

	public double getSelectionStart()		{		return selectionStart;		}
	public double getSelectionEnd()			{		return selectionEnd;		}
	public void setSelectionStart(double i)	{		selectionStart = i;		}
	public void setSelectionEnd(double i)	{		selectionEnd = i;	}

	public void moveSelection(double dx)	{		selectionStart += dx;		selectionEnd += dx;	}


}
