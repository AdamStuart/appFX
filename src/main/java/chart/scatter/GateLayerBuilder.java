//package chart.scatter;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
//
//import javafx.animation.Transition;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.EventHandler;
//import javafx.geometry.Point2D;
//import javafx.geometry.Pos;
//import javafx.geometry.Rectangle2D;
//import javafx.scene.Node;
//import javafx.scene.SnapshotParameters;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.XYChart;
//import javafx.scene.chart.XYChart.Data;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Alert.AlertType;
//import javafx.scene.control.Label;
//import javafx.scene.control.ListView;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.WritableImage;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.StackPane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.shape.Shape;
//import javafx.scene.transform.Scale;
//import javafx.scene.transform.Transform;
//import javafx.util.Duration;
//
//
//public class GateLayerBuilder
//{
///*
// * This class adds a layer on top of a given XY chart. 
// *
// */
//
//	private static final String INFO_LABEL_ID = "zoomInfoLabel";
//	private ListView<Image> gallery;
//	private DrillDownChart container;
//	private XYChart<Number, Number> chart;
//	private NumberAxis xAxis;
//	private NumberAxis yAxis;
//	private Rectangle selectionRectangle;
//	private ObservableList<Rectangle> displayGates;		// these are children of the scene graph, let it manage them
//	private ObservableList<Rectangle> gateDefs;
//	private Label infoLabel;
//
//	private Point2D selRectStart = null;
//	private Point2D selRectEnd = null;
//	private boolean isRectangleSizeTooSmall() {
//		double width = Math.abs(selRectEnd.getX() - selRectStart.getX());
//		double height = Math.abs(selRectEnd.getY() - selRectStart.getY());
//		return width < 10 || height < 10;
//	}
//
//	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";
//
//	/**
//	 * Create a new instance of this class with the given chart and pane instances. The {@link Pane} instance is needed
//	 * as a parent for the rectangle that represents the user selection.
//	 * 
//	 * @param chart
//	 *            the xy chart to which the zoom support should be added
//	 * @param pane
//	 *            the pane on which the selection rectangle will be drawn.
//	 */
//	public void GateLayerBuilder(XYChart<Number, Number> xychart, DrillDownChart pane, ListView<Image> gal) 
//	{
//		container = pane;
//		gallery = gal;
//		chart = xychart;
//		xAxis = (NumberAxis) chart.getXAxis();
//		yAxis = (NumberAxis) chart.getYAxis();
//		makeSelectionRectangle();
//		displayGates = FXCollections.observableArrayList();			// the ones added to the scene
//		gateDefs = FXCollections.observableArrayList();				// normalized to 0-1 range
////		container.getChildren().add(selectionRectangle);
//		addDragSelectionMechanism();
//		addInfoLabel();
//		StackPane.setAlignment(selectionRectangle, Pos.TOP_LEFT);
//		pane.getChildren().add(selectionRectangle);
//		selectionRectangle.toFront();
//	}
//
//	private void makeSelectionRectangle()
//	{
//		selectionRectangle = new Rectangle();  // SelectionRectangle();
//		selectionRectangle.setManaged(false);
////		selectionRectangle.setFill(null);
//		selectionRectangle.setOpacity(0.5);
//		selectionRectangle.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
//
//		selectionRectangle.setStroke(Color.BLACK);
//		selectionRectangle.setStrokeWidth(2f);
//		selectionRectangle.setOnMousePressed(new SelectionMousePressedHandler());
//		selectionRectangle.setOnMouseDragged(new SelectionMouseDraggedHandler());
//		selectionRectangle.setOnMouseReleased(new SelectionMouseReleasedHandler());
//// move to gates
//		selectionRectangle.setOnMouseClicked(ev -> { if (ev.getClickCount() > 1)	gateDoubleClick(ev); });
//
//	}
//	
//	public void imagePeek(Image img)
//	{
////		ImageView view = new ImageView(img);
//		Alert dialog = new Alert(AlertType.CONFIRMATION);
//		dialog.setTitle("This shows you the image");
//		dialog.setContentText(null);
//		dialog.setGraphic(new ImageView(img));
//		dialog.showAndWait();
//
//	}
//	private void gateDoubleClick(MouseEvent ev)
//	{
////		if (gallery.getHeight() < 40) openGallery();
//		Node chartPlotArea = chart.lookup(".chart-plot-background");
//		double w = chartPlotArea.getLayoutBounds().getWidth();
//		double h = chartPlotArea.getLayoutBounds().getHeight();
//		double mult = 1.0;
//		SnapshotParameters parameters = new SnapshotParameters();
//		Transform transform = new Scale(mult, mult);
//		parameters.setTransform(transform);
//		WritableImage wi = new WritableImage((int) (mult * w)+4, (int) (mult * h)+4);
//		double x = chartPlotArea.getLayoutX();
//		double y = chartPlotArea.getLayoutY();
//		Transform t = container.getLocalToSceneTransform();
//		Rectangle2D port = new Rectangle2D(x + t.getTx(), y + t.getTy(), w+4, h+4);
//		parameters.setViewport(port);
//		WritableImage snapshot = container.snapshot(parameters, wi);
//		imagePeek(wi);
//		
//		container.openGallery();
//		ImageView newView = new ImageView(snapshot);
//		newView.setLayoutX(60);
//		newView.setLayoutY(60);
//		container.getChildren().add(newView);
//
//		double startScale = 1.0;
//		double endScale = 0.4;
//
//	Transition shower = new Transition() {
//		int CELLSIZE = 80;
//		double startX = 0;
//		double startY = 0;
//		double endY = -300;
//		int depth = gallery.getItems().size();
//		double endX = -200 + CELLSIZE * depth;
//
//		{ setCycleDuration(Duration.millis(500)); 
//			setOnFinished((a) -> 
//			{ 
//				newView.setFitHeight(CELLSIZE);
//				newView.setFitWidth(CELLSIZE);
//				newView.setScaleX(1.0);
//				newView.setScaleY(1.0);
//				newView.setTranslateX(0);
//				newView.setTranslateY( 0);
//				container.getChildren().remove(newView);
//				gallery.getItems().add(newView.getImage());
//			} );
//		}
//        protected void interpolate(double frac) {
//        	
//       	double scale = startScale + (endScale - startScale) * frac;
////        System.out.println("scale = " + scale);
//        newView.scaleXProperty().set(scale); 
//            newView.scaleYProperty().set(scale); 
//            newView.setTranslateX(startX + (endX - startX) * frac); 
//            newView.setTranslateY(startY + (endY - startY) * frac);
////            newView.rotateProperty().set(-180 * frac);
//        	}
//        };
//        shower.play();
//    
//
//	}
//
//	/**
//	 * The info label shows a short info text that tells the user how to unreset the zoom level.
//	 */
//	private void addInfoLabel() {
//		infoLabel = new Label("Click ESC to reset the zoom level.");
//		infoLabel.setId(INFO_LABEL_ID);
//		container.getChildren().add(infoLabel);
//		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
//		infoLabel.setVisible(false);
//	}
//
//	
//	/**
//	 * Adds a mechanism to select an area in the chart that should be displayed at enlarged scale.
//	 */
//	private void addDragSelectionMechanism() {
//		container.addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedHandler());
//		container.addEventHandler(MouseEvent.MOUSE_DRAGGED, new MouseDraggedHandler());
//		container.addEventHandler(MouseEvent.MOUSE_RELEASED, new MouseReleasedHandler());
//		container.addEventHandler(KeyEvent.KEY_RELEASED, new EscapeKeyHandler());
//	}
//
//	private Point2D computeRectanglePoint(double eventX, double eventY) {
//		double lowerBoundX = computeOffsetInChart(xAxis, false);
//		double upperBoundX = lowerBoundX + xAxis.getWidth();
//		double lowerBoundY = computeOffsetInChart(yAxis, true);
//		double upperBoundY = lowerBoundY + yAxis.getHeight();
//		// make sure the rectangle's end point is in the interval defined by the lower and upper bounds for each
//		// dimension
//		double x = Math.max(lowerBoundX, Math.min(eventX, upperBoundX));
//		double y = Math.max(lowerBoundY, Math.min(eventY, upperBoundY));
//		return new Point2D(x, y);
//	}
//
//	/**
//	 * Computes the pixel offset of the given node inside the chart node.
//	 * 
//	 * @param node
//	 *            the node for which to compute the pixel offset
//	 * @param vertical
//	 *            flag that indicates whether the horizontal or the vertical dimension should be taken into account
//	 * @return the offset inside the chart node
//	 */
//	private double computeOffsetInChart(Node node, boolean vertical) {
//		double offset = 0;
//		do {
//			offset += (vertical) ? node.getLayoutY() : node.getLayoutX();
//			node = node.getParent();
//		} while (node != chart);
//		return offset;
//	}
//	/**-------------------------------------------------------------------------------
//	 */
//	private void drawSelectionRectangle(final double x, final double y, final double width, final double height) {
//		selectionRectangle.setVisible(true);
//		selectionRectangle.setX(x);
//		selectionRectangle.setY(y);
//		selectionRectangle.setWidth(width);
//		selectionRectangle.setHeight(height);
//		selectionRectangle.toFront();
//	}
//	private void drawSelectionRectangleAt(final double x, final double y) {
//		drawSelectionRectangle(x,y,selectionRectangle.getWidth(), selectionRectangle.getHeight());
//	}
//	private void disableAutoRanging() {
//		xAxis.setAutoRanging(false);
//		yAxis.setAutoRanging(false);
//	}
//
//	private void showInfo() {			infoLabel.setVisible(true);		}
//
//
//	private void setAxisBounds() {
//		disableAutoRanging();
//if (selRectStart == null || selRectEnd == null)
//{
//	selRectStart = new Point2D(selectionRectangle.getX(), selectionRectangle.getY()+ selectionRectangle.getHeight());
//	selRectEnd = new Point2D(selectionRectangle.getX()+ selectionRectangle.getWidth(), selectionRectangle.getY());
//}
//		// compute new bounds for the chart's x and y axes
//		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX());
//		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX());
//		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY());
//		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY());
//
//		double xMin = frameToScaleX(selectionMinX);
//		double xMax = frameToScaleX(selectionMaxX);
//		double yMin = frameToScaleY(selectionMaxY);
//		double yMax = frameToScaleY(selectionMinY);
//		
//		double freq = getGateFreq(chart, xMin, xMax, yMin, yMax);
//		NumberFormat fmt = new DecimalFormat("0.00");
//		String s = fmt.format(freq * 100) +  "% for ( " + fmt.format(xMin) + ", " + fmt.format(yMin) + ") ( " + fmt.format(xMax) + ", " + fmt.format(yMax) + ")";
//		infoLabel.setText(s);
//		showInfo();
//
//	}
//	
//	private double getGateFreq(XYChart<Number, Number> chart, double xMin, double xMax, double yMin, double yMax)
//	{
//		int ct = 0;
//		XYChart.Series<Number, Number> data = chart.getData().get(0);
//		for (Data<Number, Number> n : data.getData())
//		{
//			double x = n.getXValue().doubleValue();
//			double y = n.getYValue().doubleValue();
//			if ( (x >= xMin && x < xMax) && (y >= yMin && y < yMax))
//				ct++;
//		}
//		return ct / (double) data.getData().size();
//		
//	}
//	/**-------------------------------------------------------------------------------
//	 *   Mouse handlers for clicks inside the selection rectangle
//	 */
//	boolean resizing = false;
//	private final class SelectionMousePressedHandler implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(final MouseEvent event) {
//
//			// do nothing for a right-click
//			if (event.isSecondaryButtonDown()) {
//				return;
//			}
//			if (inCorner(event, selectionRectangle))
//			{
//				resizing = true;
//				selRectStart = oppositeCorner(event,selectionRectangle);
//			}
//			else
//			{
//				selRectStart = new Point2D(event.getX(), event.getY());
//				offsetX = event.getX() - selectionRectangle.getX();
//				offsetY = event.getY() - selectionRectangle.getY();
//			}
//			
//			
//			// store position of initial click
////			selectionRectangleStart = computeRectanglePoint(event.getX(), event.getY());
////			System.out.println("SelectionMousePressedHandler");
//			event.consume();
//		}
//	}
//	double SLOP = 4;
//	
//	boolean inCorner(MouseEvent ev, Rectangle r)
//	{
//		double x = ev.getX();
//		double y = ev.getY();
//		double left = r.getX();
//		double right = left + r.getWidth();
//		double top = r.getY();
//		double bottom = top + r.getHeight();
//		if (Math.abs(x-left) < SLOP || Math.abs(x-right) < SLOP)
//			if (Math.abs(y-top) < SLOP || Math.abs(y-bottom) < SLOP)
//				return true;
//		return false;
//	}
//	
//	Point2D oppositeCorner(MouseEvent ev, Rectangle r)
//	{
//		double outX = -1, outY = -1;
//		double x = ev.getX();
//		double y = ev.getY();
//		double left = r.getX();
//		double right = left + r.getWidth();
//		double top = r.getY();
//		double bottom = top + r.getHeight();
//		if (Math.abs(x-left) < SLOP)  		outX = right;
//		else if (Math.abs(x-right) < SLOP) 	outX = left;
//		else return null;
//
//		if (Math.abs(y-top) < SLOP)  		outY = bottom;
//		else if (Math.abs(y-bottom) < SLOP) outY = top;
//		else return null;
//		
//		return new Point2D(outX, outY);
//	}
//	double offsetX = 0, offsetY = 0;
//
//	/**
//	 *
//	 */
//	private final class SelectionMouseDraggedHandler implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(final MouseEvent event) {
//
//			
//			if (event.isSecondaryButtonDown()) {	return;		}		// do nothing for a right-click
////System.out.println("SelectionMouseDraggedHandler");
//			if (resizing)
//			{
//				// store current cursor position
//				selRectEnd = computeRectanglePoint(event.getX(), event.getY());
//				if (selRectStart == null)
//					selRectStart = new Point2D(event.getX(), event.getY());
//				double x = Math.min(selRectStart.getX(), selRectEnd.getX());
//				double y = Math.min(selRectStart.getY(), selRectEnd.getY());
//				double width = Math.abs(selRectStart.getX() - selRectEnd.getX());
//				double height = Math.abs(selRectStart.getY() - selRectEnd.getY());
//				drawSelectionRectangle(x, y, width, height);
////				System.out.println("x:" + x + " y:" + y);
//			} else
//			{
//				double oldX = selRectStart.getX();
//				double oldY = selRectStart.getY();
//				double dx = event.getX() - oldX;
//				double dy = event.getY() - oldY;
////				System.out.println("x:" + oldX + " y:" + oldY);
//				offsetRectangle(selectionRectangle, dx, dy);
//				drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY);
//				selRectStart = new Point2D(event.getX(), event.getY());
//			}
//			event.consume();
//		}
//		private void offsetRectangle(Rectangle r, double dx, double dy)
//		{
////			NumberFormat fmt = new DecimalFormat("0.00");
////			System.out.println("dx:" + fmt.format(dx) + "dx:" + fmt.format(dy));
//			selectionRectangle.setX(r.getX() + dx - offsetX);
//			selectionRectangle.setY(r.getY() + dy - offsetY);
//		}
//		}
//	
//
//	/**
//	 *
//	 */
//	private final class SelectionMouseReleasedHandler implements EventHandler<MouseEvent> {
//
//		@Override
//		public void handle(final MouseEvent event) {
////			hideSelectionRectangle();
////			System.out.println("SelectionMouseReleasedHandler");
//			if (resizing && isRectangleSizeTooSmall()) 				return;
//			
//			setAxisBounds();
//			selRectStart =  selRectEnd = null;
//			resizing = false;
//			container.requestFocus();	// needed for the key event handler to receive events
//			event.consume();
//		}
//
//			/**
//		 * Hides the selection rectangle.
//		 */
//		private void hideSelectionRectangle() {
//			selectionRectangle.setVisible(false);
//		}
//
//	}
//	/**-------------------------------------------------------------------------------
//	 *
//	 */
//	private final class MousePressedHandler implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(final MouseEvent event) {
//
//			// do nothing for a right-click
//			if (event.isSecondaryButtonDown()) {
//				return;
//			}
//
//			// store position of initial click
//			selRectStart = computeRectanglePoint(event.getX(), event.getY());
//			event.consume();
//		}
//	}
//
//	/**
//	 *
//	 */
//	private final class MouseDraggedHandler implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(final MouseEvent event) {
//
//			// do nothing for a right-click
//			if (event.isSecondaryButtonDown()) {
//				return;
//			}
//
//			// store current cursor position
//			selRectEnd = computeRectanglePoint(event.getX(), event.getY());
//
//			Rectangle2D r = union(selRectStart, selRectEnd);
//			drawSelectionRectangle(r);
//			event.consume();
//		}
//
//		/**
//		 * Draws a selection box in the view.
//		 */
//		private void drawSelectionRectangle(Rectangle2D r) {
//			selectionRectangle.setVisible(true);
//			selectionRectangle.setX(r.getMinX());
//			selectionRectangle.setY(r.getMinY());
//			selectionRectangle.setWidth(r.getWidth());
//			selectionRectangle.setHeight(r.getHeight());
//		}
//	}
//
//	Rectangle2D union(Point2D a, Point2D b)
//	{
//		double x = Math.min(a.getX(), b.getX());
//		double y = Math.min(a.getY(), b.getY());
//		double width = Math.abs(a.getX() - b.getX());
//		double height = Math.abs(a.getY() - b.getY());
//		return new Rectangle2D(x,y,width,height);
//	}
//	/**
//	 *
//	 */
//	private final class MouseReleasedHandler implements EventHandler<MouseEvent> {
//		@Override
//		public void handle(final MouseEvent event) {
////			hideSelectionRectangle();
//
//			if (selRectStart == null || selRectEnd == null) 		return;
//			if (isRectangleSizeTooSmall()) 		return;
//
//			setAxisBounds();
//			selRectStart = selRectEnd = null;
//
//			// needed for the key event handler to receive events
//			container.requestFocus();
//			event.consume();
//		}
//	}
//	private final class EscapeKeyHandler implements EventHandler<KeyEvent> {
//		@Override
//		public void handle(KeyEvent event) {
//
//			// the ESCAPE key lets the user reset the zoom level
//			if (KeyCode.ESCAPE.equals(event.getCode())) {
//				resetAxisBounds();
//				infoLabel.setVisible(false);
//			}
//		}
//
//		private void resetAxisBounds() {
//			xAxis.setAutoRanging(true);
//			yAxis.setAutoRanging(true);
//		}
//
//	}	
//	
//	private double frameToScaleX(double value)
//	{
//		Node chartPlotArea = chart.lookup(".chart-plot-background");
//		double chartZeroX = chartPlotArea.getLayoutX();
//		double chartWidth = chartPlotArea.getLayoutBounds().getWidth();
//		return computeBound(value, chartZeroX, chartWidth, xAxis.getLowerBound(), xAxis.getUpperBound(), false);
//	}
//
//	private double frameToScaleY(double value)
//	{
//		Node chartPlotArea = chart.lookup(".chart-plot-background");
//		double chartZeroY = chartPlotArea.getLayoutY();
//		double chartHeight = chartPlotArea.getLayoutBounds().getHeight();
//		return computeBound(value, chartZeroY, chartHeight, yAxis.getLowerBound(), yAxis.getUpperBound(),true) + 0.5;
//	}
//	
//	
//	private double computeBound(double pixelPosition, double pixelOffset, double pixelLength, double lowerBound,
//			double upperBound, boolean axisInverted) {
//		double pixelPositionWithoutOffset = pixelPosition - pixelOffset;
//		double relativePosition = pixelPositionWithoutOffset / pixelLength;
//		double axisLength = upperBound - lowerBound;
//
//		// The screen's y axis grows from top to bottom, whereas the chart's y axis goes from bottom to top.
//		// That's why we need to have this distinction here.
//		double offset = 0;
//		int sign = 0;
//		if (axisInverted) {		offset = upperBound;	sign = -1;		} 
//		else 			  {		offset = lowerBound;	sign = 1;		}
//
//		double newBound = offset + sign * relativePosition * axisLength;
//		return newBound;
//
//}
//
//	public void resetGates()
//	{
//		Node chartPlotArea = chart.lookup(".chart-plot-background");
//		for (int i=0; i< gateDefs.size(); i++)
//		{
//			Rectangle def = gateDefs.get(i);
//			Rectangle display = gateDefs.get(i);
//		}
//		
//	
//	}
//	
//	Rectangle getScaleRect(Rectangle def, Rectangle frame)
//	{
//		double frameWidth = frame.getWidth();
//		double frameHeight = frame.getHeight();
//		Rectangle r = new Rectangle(frame.getX() + def.getX() * frameWidth,
//				frame.getY() + def.getY() * frameHeight, 
//				def.getWidth() * frameWidth, def.getHeight() * frameHeight);
//		return r;
//	}
//
//	void rescaleRect(Rectangle def, Rectangle frame, Rectangle display)
//	{
//		double frameWidth = frame.getWidth();
//		double frameHeight = frame.getHeight();
//		display.setX(frame.getX() + def.getX() * frameWidth);
//		display.setY(frame.getY() + def.getY() * frameHeight);
//		display.setWidth(def.getWidth() * frameWidth);
//		display.setHeight(def.getHeight() * frameHeight);
//	}
//
//	Rectangle rectDef(Rectangle child, Rectangle frame)
//	{
//		double frameWidth = frame.getWidth();
//		double frameHeight = frame.getHeight();
//		Rectangle r = new Rectangle((child.getX() - frame.getX()) / frameWidth,
//				(child.getY() - frame.getY()) / frameHeight, 
//				child.getWidth() / frameWidth, child.getHeight() / frameHeight);
//		return r;
//	}
//
//
//}
