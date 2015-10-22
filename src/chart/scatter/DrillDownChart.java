package chart.scatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import animation.Transitions;
import gui.Borders;
import gui.Cursors;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;
import util.RectangleUtil;


/*
 *   A DrillDownChart is a XYChart that can show a multidimensional data set, two dimensions at a time.
 *   Identifying a region in the XY space will take the subset of events in that region
 *   and display them in the next two dimensions.  A thumbnail of the parent is kept in a list
 *   across the top, called gallery
 */
public class DrillDownChart extends VBox
{
	private final ListView<Image> gallery = new ListView<Image>();
	private final StackPane chartStack = new StackPane();
	private OverlaidScatterChart<Number, Number> scatter;
	private final SynthGenController controller;

	public DrillDownChart(SynthGenController ctlr, ObservableList<SynthGenRecord> observableList)
		{
			this(ctlr);
			addData(observableList);
		}

	private void addLayer(String xName, String yName, int transitionType)
	{
		System.out.println("scatter");
		Image prevImage = (transitionType == 0) ? null : chartSnapshot();
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel(xName);
		xAxis.setOnMouseClicked(ev -> {
			if (ev.isShiftDown()) prevXParm();
			else				nextXParm();
		});
		
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yName);
		yAxis.setOnMouseClicked(ev -> {
			if (ev.isShiftDown()) prevYParm();
			else				nextYParm();
		});
		
		scatter = new OverlaidScatterChart<Number, Number>(xAxis, yAxis);
		// scatter.setTitle("title goes here");
		Node chartPlotArea = scatter.lookup(".chart-plot-background");
		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setBorder(Borders.blueBorder);
		}
		addData(null);
		Image curImage = (transitionType == 0) ? null : chartSnapshot();
		if (transitionType != 0 && curImage != null && prevImage != null)
			new Transitions(prevImage, curImage).play(Transitions.Transition.CUBE);
	
	}
	private Image chartSnapshot()
	{
		try
		{
			WritableImage img = (WritableImage) snapshotChart(scatter.getParent());
			imagePeek(img);
			return img;
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
//		
//		if (scatter == null) return null;
//		Node chartPlotArea = scatter.lookup(".chart-plot-background");
//		double w = chartPlotArea.getLayoutBounds().getWidth();
//		double h = chartPlotArea.getLayoutBounds().getHeight();
//		if (w == 0 || h == 0) return null;
//		double mult = 1.0;
//		SnapshotParameters parameters = new SnapshotParameters();
//		Transform transform = new Scale(mult, mult);
//		parameters.setTransform(transform);
//		WritableImage wi = new WritableImage((int) (mult * w), (int) (mult * h));
//		Transform t = chartPlotArea.getLocalToSceneTransform();
//		Rectangle2D port = new Rectangle2D(t.getTx(), t.getTy(), w, h);
//		parameters.setViewport(port);
//		snapshot(parameters, wi);
//		imagePeek(wi);
//		return wi;
	}
	
	   //----------------------------------------------------------------------------------------
	   private Image snapshotChart(final Parent chartContainer) throws InterruptedException 
	   {
	      final CountDownLatch latch = new CountDownLatch(1);
	      // render the chart in an offscreen scene (scene is used to allow css processing) and snapshot it to an image.
	      // the snapshot is done in runlater as it must occur on the javafx application thread.
	      final SimpleObjectProperty<WritableImage> imageProperty = new SimpleObjectProperty();
	      Platform.runLater(new Runnable() {
	        @Override public void run() {
	          Scene snapshotScene = new Scene(chartContainer);
	          final SnapshotParameters params = new SnapshotParameters();
	          params.setFill(Color.ALICEBLUE);
	          chartContainer.snapshot(
	            new Callback<SnapshotResult, Void>() {
	              @Override public Void call(SnapshotResult result) {
	                imageProperty.set(result.getImage());
	                latch.countDown();
	                return null;
	              }
	            }, params,  null );
	        }
	      });
	 
	      latch.await();
	      
	      return imageProperty.get();
	    }
	  
	
	
	
	int xIndex = 0;
	int yIndex = 1; 
	int nDimensions = 8;
	String[] dims = new String[]{"FS", "SS","FL1", "FL2", "FL3", "FL4", "FL5", "FL6", "FL7", "FL8" };
	private void prevXParm()
	{
		xIndex--;
		if (xIndex < 0) xIndex = nDimensions;
		addLayer(dims[xIndex], dims[yIndex], -1);
	}
	private void nextXParm()
	{
		xIndex++;
		if (xIndex >= nDimensions) xIndex = 0;
		addLayer(dims[xIndex], dims[yIndex], 1);
		
	}
	private void prevYParm()
	{
		yIndex--;
		if (yIndex < 0) yIndex = nDimensions;
		addLayer(dims[xIndex], dims[yIndex], -1);
		
	}
	private void nextYParm()
	{
		yIndex++;
		if (yIndex >= nDimensions) yIndex = 0;
		addLayer(dims[xIndex], dims[yIndex], 1);
		
	}
	//------------------------------------------------------------------------
	public DrillDownChart(SynthGenController ctlr)
		{
			controller = ctlr;
			addLayer("Forward Scatter", "Side Scatter", 0);
			
			gallery.setOrientation(Orientation.HORIZONTAL);
			gallery.setCellFactory(p -> new ImageCell());
			gallery.setPrefHeight(20);
			gallery.setMinHeight(20);
			gallery.setOnMouseClicked(ev -> { openGallery();	});

			VBox pile = new VBox();
			pile.getChildren().addAll(scatter, gallery);
			getChildren().addAll(pile);
			gateLayerBuilder();

		}

	//------------------------------------------------------------------------------
	public void openGallery()
	{
		Transition opener = new Transition()
		{
			double CLOSED = 20;
			double OPEN = 120;
			{
				setCycleDuration(Duration.millis(300));
				setOnFinished((a) -> {
				});
			}
			protected void interpolate(double frac)
			{
				double hite = CLOSED + frac * (OPEN - CLOSED);
				gallery.setPrefHeight(hite);
				gallery.setMinHeight(hite);
			}
		};
		if (gallery.getHeight() < 40)
			opener.play();

	}
	//------------------------------------------------------------------------------
	public static String summaryFileName = "syntheticDefinitions";
	public void addData(ObservableList<SynthGenRecord> observableList)
	{
		if (observableList == null)
		{
			SynthGenMetaFileStream input = new SynthGenMetaFileStream(this);
			observableList = input.readDefFile(summaryFileName);
		}
		for (SynthGenRecord rec : observableList)
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			for (int i = 0; i < rec.getCount(); i++)
			{
				Point2D randPt = SynthFileStream.randomNormal(rec);
				series.getData().add(new XYChart.Data<Number, Number>(randPt.getX(), randPt.getY()));
			}
			scatter.getData().add(series);
		}
	}

	//------------------------------------------------------------------------------
	public class ImageCell extends ListCell<Image>
	{
		private final ImageView imageView = new ImageView();

		@Override
		protected void updateItem(Image item, boolean empty)
		{
			super.updateItem(item, empty);
			imageView.setStyle(".list-cell {   -fx-cell-hover-color: #0093ff;   -fx-background-color: white;");

			if (empty || item == null)
				setGraphic(null);
			else
			{
				imageView.setImage(item);
				imageView.setFitWidth(80);
				imageView.setFitHeight(80);
				setGraphic(imageView);
			}
		}
	}

/*--------------------------------------------------------------------------------------------
 * This adds a layer on top of a the XY chart named "scatter". 
 *
 */

	private static final String INFO_LABEL_ID = "zoomInfoLabel";
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private Rectangle selectionRectangleDef;
	private Rectangle selectionRectangle;
//	private ObservableList<Rectangle> displayGates;		// these are children of the scene graph, let it manage them
	private ObservableList<Rectangle> gateDefs;
	private Label infoLabel;

	private Point2D selRectStart = null;
	private Point2D selRectEnd = null;
	private boolean isRectangleSizeTooSmall() {
		if (selRectStart == null || selRectEnd == null ) return true;
		double width = Math.abs(selRectEnd.getX() - selRectStart.getX());
		double height = Math.abs(selRectEnd.getY() - selRectStart.getY());
		return width < 10 || height < 10;
	}

	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";


	public void gateLayerBuilder() 
	{
		xAxis = (NumberAxis) scatter.getXAxis();
		yAxis = (NumberAxis) scatter.getYAxis();
		makeSelectionRectangle();
//		displayGates = FXCollections.observableArrayList();			// the ones added to the scene
		gateDefs = FXCollections.observableArrayList();				// normalized to 0-1 range
//		container.getChildren().add(selectionRectangle);
		addDragSelectionMechanism();
		addInfoLabel();
		StackPane.setAlignment(selectionRectangle, Pos.TOP_LEFT);
	}

	private void makeSelectionRectangle()
	{
		selectionRectangle = new Rectangle();  // SelectionRectangle();
		selectionRectangleDef = new Rectangle();
		selectionRectangle.setManaged(false);
//		selectionRectangle.setFill(null);
		selectionRectangle.setOpacity(0.2);
		selectionRectangle.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);

		selectionRectangle.setStroke(Color.BLACK);
		selectionRectangle.setStrokeWidth(2f);
		RectangleUtil.setupCursors(selectionRectangle);
		selectionRectangle.setOnMousePressed(event -> {
//			if (event.isSecondaryButtonDown()) 	return;
			Pos pos = RectangleUtil.getPos(event, selectionRectangle);
			resizing = RectangleUtil.inCorner(pos);
			if (resizing)
				selRectStart = RectangleUtil.oppositeCorner(event,selectionRectangle);
			else
			{
				selRectStart = new Point2D(event.getX(), event.getY());
				offsetX = event.getX() - selectionRectangle.getX();
				offsetY = event.getY() - selectionRectangle.getY();
			}
			event.consume();

		});

		selectionRectangle.setOnMouseDragged(event -> {
			if (event.isSecondaryButtonDown()) 	return;

			if (resizing)
			{
				// store current cursor position
				selRectEnd = computeRectanglePoint(event.getX(), event.getY());
				if (selRectStart == null)
					selRectStart = RectangleUtil.oppositeCorner(event,selectionRectangle);
//selRectStart = new Point2D(event.getX(), event.getY());			// ERROR -- will reset instead of resize
				double x = Math.min(selRectStart.getX(), selRectEnd.getX());
				double y = Math.min(selRectStart.getY(), selRectEnd.getY());
				double width = Math.abs(selRectStart.getX() - selRectEnd.getX());
				double height = Math.abs(selRectStart.getY() - selRectEnd.getY());
				drawSelectionRectangle(x, y, width, height);
//				System.out.println("x:" + x + " y:" + y);
			} else
			{
				double oldX = selRectStart.getX();
				double oldY = selRectStart.getY();
				double dx = event.getX() - oldX;
				double dy = event.getY() - oldY;
//				System.out.println("x:" + oldX + " y:" + oldY);
				offsetRectangle(selectionRectangle, dx, dy);
				drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY);
				selRectStart = new Point2D(event.getX(), event.getY());
			}
			event.consume();
		});
		
		
		selectionRectangle.setOnMouseReleased(event -> {
//			if (selRectStart == null || selRectEnd == null) 		return;
//			if (isRectangleSizeTooSmall()) 							return;
			selectionRectangleDef  = rectDef(selectionRectangle, getPlotFrame());
			setAxisBounds();
			selRectStart = selRectEnd = null;
			requestFocus();		// needed for the key event handler to receive events
			event.consume();
		
		});
	}
	
	private void offsetRectangle(Rectangle r, double dx, double dy)
	{
//		NumberFormat fmt = new DecimalFormat("0.00");
//		System.out.println("dx:" + fmt.format(dx) + "dx:" + fmt.format(dy));
		r.setX(r.getX() + dx - offsetX);
		r.setY(r.getY() + dy - offsetY);
	}

	public void imagePeek(Image img)
	{
//		ImageView view = new ImageView(img);
		Alert dialog = new Alert(AlertType.CONFIRMATION);
		dialog.setTitle("This shows you the image");
		dialog.setContentText("");
		dialog.setGraphic(new ImageView(img));
		dialog.showAndWait();

	}
	
	Rectangle getPlotFrame()
	{
		Node chartPlotArea = scatter.lookup(".chart-plot-background");
		if (chartPlotArea == null) return new Rectangle(0,0,0,0);
		double w = chartPlotArea.getLayoutBounds().getWidth();
		double h = chartPlotArea.getLayoutBounds().getHeight();
		return new Rectangle(chartPlotArea.getLayoutX(),chartPlotArea.getLayoutY(),w, h);	

	}
	private void gateDoubleClick(MouseEvent ev)
	{
//		if (gallery.getHeight() < 40) openGallery();
		Image wi = chartSnapshot();
		openGallery();
		ImageView newView = new ImageView(wi);
		newView.setLayoutX(60);
		newView.setLayoutY(60);
		getChildren().add(newView);

		double startScale = 1.0;
		double endScale = 0.4;

	Transition shower = new Transition() {
		int CELLSIZE = 80;
		double startX = 0;
		double startY = 0;
		double endY = 300;
		int depth = gallery.getItems().size();
		double endX = -200 + CELLSIZE * depth;

		{ setCycleDuration(Duration.millis(500)); 
			setOnFinished((a) -> 
			{ 
				newView.setFitHeight(CELLSIZE);
				newView.setFitWidth(CELLSIZE);
				newView.setScaleX(1.0);
				newView.setScaleY(1.0);
				newView.setTranslateX(0);
				newView.setTranslateY( 0);
				getChildren().remove(newView);
				gallery.getItems().add(newView.getImage());
			} );
		}
        protected void interpolate(double frac) {
        	
       	double scale = startScale + (endScale - startScale) * frac;
//        System.out.println("scale = " + scale);
        newView.scaleXProperty().set(scale); 
            newView.scaleYProperty().set(scale); 
            newView.setTranslateX(startX + (endX - startX) * frac); 
            newView.setTranslateY(startY + (endY - startY) * frac);
//            newView.rotateProperty().set(-180 * frac);
        	}
        };
        shower.play();
    

	}

	/**
	 * The info label shows a short info text
	 */
	private void addInfoLabel() {
		infoLabel = new Label("");
		infoLabel.setId(INFO_LABEL_ID);
		getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
	}

	
	/**----------------------------------------------------------------------------------
	 * Adds a mechanism to select an area in the chart 
	 */
	private void addDragSelectionMechanism() 
	{
		setOnMousePressed( ev -> {
			if (ev.isSecondaryButtonDown()) 	return;	
			if (!getChildren().contains(selectionRectangle))
				getChildren().add(selectionRectangle);
			selectionRectangle.toFront();
			selRectStart = computeRectanglePoint(ev.getX(), ev.getY());		// store position of initial click
			ev.consume();
		});
		setOnMouseDragged(ev -> {
			if (ev.isSecondaryButtonDown()) 	return;	
			selRectEnd = computeRectanglePoint(ev.getX(), ev.getY());		// store current cursor position
			Rectangle2D r = union(selRectStart, selRectEnd);
			drawSelectionRectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
			ev.consume();

		});
		setOnMouseReleased(ev -> {
			if (selRectStart == null || selRectEnd == null) 		return;
			if (isRectangleSizeTooSmall()) 							return;
			setAxisBounds();
			selRectStart = selRectEnd = null;
			selectionRectangleDef = rectDef(selectionRectangle, getPlotFrame());
//		if (tool == gater)
	if (ev.isShiftDown())
		addGate(selectionRectangle);
//			getChildren().remove(selectionRectangle);

			requestFocus();		// needed for the key event handler to receive events
			ev.consume();

		});

//		setOnKeyReleased(ev -> {
//			if (KeyCode.ESCAPE.equals(ev.getCode())) {
//				xAxis.setAutoRanging(true);
//				yAxis.setAutoRanging(true);
//				infoLabel.setVisible(false);
//			}
//		});
	}

	private Point2D computeRectanglePoint(double eventX, double eventY) {
		double lowerBoundX = computeOffsetInChart(xAxis, false);
		double upperBoundX = lowerBoundX + xAxis.getWidth();
		double lowerBoundY = computeOffsetInChart(yAxis, true);
		double upperBoundY = lowerBoundY + yAxis.getHeight();
		// make sure the rectangle's end point is in the interval defined by the lower and upper bounds for each
		// dimension
		double x = Math.max(lowerBoundX, Math.min(eventX, upperBoundX));
		double y = Math.max(lowerBoundY, Math.min(eventY, upperBoundY));
		return new Point2D(x, y);
	}

	/**
	 * Computes the pixel offset of the given node inside the chart node.
	 * 
	 * @param node
	 *            the node for which to compute the pixel offset
	 * @param vertical
	 *            flag that indicates whether the horizontal or the vertical dimension should be taken into account
	 * @return the offset inside the chart node
	 */
	private double computeOffsetInChart(Node node, boolean vertical) {
		double offset = 0;
		do {
			offset += (vertical) ? node.getLayoutY() : node.getLayoutX();
			node = node.getParent();
		} while (node != null && node != scatter);
		return offset;
	}
	/**-------------------------------------------------------------------------------
	 */
	private void drawSelectionRectangle(final double x, final double y, final double width, final double height) {
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(x);
		selectionRectangle.setY(y);
		selectionRectangle.setWidth(width);
		selectionRectangle.setHeight(height);
//		selectionRectangle.toFront();
	}
	private void drawSelectionRectangleAt(final double x, final double y) {
		drawSelectionRectangle(x,y,selectionRectangle.getWidth(), selectionRectangle.getHeight());
	}
	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	private void showInfo() {			infoLabel.setVisible(true);		}


	private void setAxisBounds() {
		disableAutoRanging();
		if (selRectStart == null || selRectEnd == null)
		{
			selRectStart = new Point2D(selectionRectangle.getX(),
					selectionRectangle.getY() + selectionRectangle.getHeight());
			selRectEnd = new Point2D(selectionRectangle.getX() + selectionRectangle.getWidth(),
					selectionRectangle.getY());
		}
		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX());
		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX());
		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY());
		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY());

		double xMin = frameToScaleX(selectionMinX);
		double xMax = frameToScaleX(selectionMaxX);
		double yMin = frameToScaleY(selectionMaxY);
		double yMax = frameToScaleY(selectionMinY);
		
		double freq = getGateFreq(scatter, xMin, xMax, yMin, yMax);
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = fmt.format(freq * 100) +  "% for ( " + fmt.format(xMin) + ", " + fmt.format(yMin) + ") ( " + fmt.format(xMax) + ", " + fmt.format(yMax) + ")";
		infoLabel.setText(s);
		showInfo();

	}
	
	private double getGateFreq(XYChart<Number, Number> chart, double xMin, double xMax, double yMin, double yMax)
	{
		Objects.requireNonNull(chart);
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		Objects.requireNonNull(data);
		int ct = 0;
		for (Data<Number, Number> n : data.getData())
		{
			double x = n.getXValue().doubleValue();
			double y = n.getYValue().doubleValue();
			if ( (x >= xMin && x < xMax) && (y >= yMin && y < yMax))
				ct++;
		}
		return ct / (double) data.getData().size();
		
	}
	/**-------------------------------------------------------------------------------
	 *   Mouse handlers for clicks inside the selection rectangle
	 */
	boolean resizing = false;
//	double SLOP = 4;
	double offsetX = 0, offsetY = 0;

	/**
	 *
	 */
//		private void offsetRectangle(Rectangle r, double dx, double dy)
//		{
////			NumberFormat fmt = new DecimalFormat("0.00");
////			System.out.println("dx:" + fmt.format(dx) + "dx:" + fmt.format(dy));
//			selectionRectangle.setX(r.getX() + dx - offsetX);
//			selectionRectangle.setY(r.getY() + dy - offsetY);
//		}
//		
	
	private void moveGate(Rectangle r)
	{
//		gateDefs.add(r);
//		System.out.println("Moved Gate, change the def" + r.toString());
	}
	
	private void addGate(Rectangle r)
	{
		Rectangle frame = getPlotFrame();
		Rectangle def = rectDef(r, frame);  
		gateDefs.add(def);
		Rectangle displayGate = getScaleRect(def, frame);
		displayGate.setX(displayGate.getX() - frame.getX());
		displayGate.setY(displayGate.getY() - frame.getY());
		makeGate(displayGate);
	}
	
	private void makeGate(Rectangle displayGate)
	{
		displayGate.getStyleClass().add("gate");
		displayGate.setOpacity(0.3);
		displayGate.setFill(Color.CYAN);
		scatter.addRectangleOverlay(displayGate);
		// scatter.add
//		System.out.println("Added Gate: " + displayGate.toString());
		displayGate.setOnMouseDragged(event -> {
			if (resizing && isRectangleSizeTooSmall())
				return;

			// store current cursor position
			selRectEnd = computeRectanglePoint(event.getX(), event.getY());
			Rectangle2D union = union(selRectStart, selRectEnd);
			drawSelectionRectangle(union);
			event.consume();
		});
		displayGate.setOnMouseReleased(event -> {
			if (resizing && isRectangleSizeTooSmall())
				return;
			setAxisBounds();
			selRectStart = selRectEnd = null;
			moveGate(rectDef(displayGate, getPlotFrame()));
			resizing = false;
			requestFocus(); // needed for the key event handler to receive events
			event.consume();
		});

		displayGate.setOnMouseClicked(ev -> { if (ev.getClickCount() > 1)	gateDoubleClick(ev); });

		displayGate.setOnMouseEntered(event -> {
			displayGate.setCursor(Cursors.getResizeCursor(RectangleUtil.getPos(event, displayGate)));
		});
		
		displayGate.setOnMouseMoved(event -> {
			displayGate.setCursor(Cursors.getResizeCursor(RectangleUtil.getPos(event, displayGate)));
		});
		
		displayGate.setOnMouseExited(event -> {		
			displayGate.setCursor(Cursor.DEFAULT);
		});
		
		displayGate.setOnMousePressed(event -> {
//			if (event.isSecondaryButtonDown()) 	return;
			Pos pos = RectangleUtil.getPos(event, displayGate);
			resizing = RectangleUtil.inCorner(pos);
			if (resizing)
				selRectStart = RectangleUtil.oppositeCorner(event,displayGate);
			else
			{
				selRectStart = new Point2D(event.getX(), event.getY());
				offsetX = event.getX() - displayGate.getX();
				offsetY = event.getY() - displayGate.getY();
			}
			event.consume();

		});

		displayGate.setOnMouseDragged(event -> {
			if (event.isSecondaryButtonDown()) 	return;

			if (resizing)
			{
				// store current cursor position
				selRectEnd = computeRectanglePoint(event.getX(), event.getY());
				if (selRectStart == null)
					selRectStart = RectangleUtil.oppositeCorner(event,displayGate);
				if (selRectEnd == null)
					selRectEnd = new Point2D(event.getX(), event.getY());
//selRectStart = new Point2D(event.getX(), event.getY());			// ERROR -- will reset instead of resize
				displayGate.setX(Math.min(selRectStart.getX(), selRectEnd.getX()));
				displayGate.setY(Math.min(selRectStart.getY(), selRectEnd.getY()));
				displayGate.setWidth(Math.abs(selRectStart.getX() - selRectEnd.getX()));
				displayGate.setHeight(Math.abs(selRectStart.getY() - selRectEnd.getY()));
//				System.out.println("x:" + x + " y:" + y);
			} else
			{
				double oldX = selRectStart.getX();
				double oldY = selRectStart.getY();
				double dx = event.getX() - oldX - offsetX;
				double dy = event.getY() - oldY - offsetY;
//				System.out.println("x:" + oldX + " y:" + oldY);
				displayGate.setX(oldX + dx);
				displayGate.setY(oldY + dy);
//				offsetRectangle(displayGate, dx, dy);
//				drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY);
				selRectStart = new Point2D(event.getX(), event.getY());
			}
			event.consume();
		});
		
		
		displayGate.setOnMouseReleased(event -> {
//			if (selRectStart == null || selRectEnd == null) 		return;
//			if (isRectangleSizeTooSmall()) 							return;
//			gateDef  = rectDef(displayGate, getPlotFrame());
			setAxisBounds();
			selRectStart = selRectEnd = null;
			requestFocus();		// needed for the key event handler to receive events
			event.consume();
		
		});
		
	}

		/**
		 * Draws a selection box in the view.
		 */
	private void drawSelectionRectangle(Rectangle2D r)
	{
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(r.getMinX());
		selectionRectangle.setY(r.getMinY());
		selectionRectangle.setWidth(r.getWidth());
		selectionRectangle.setHeight(r.getHeight());
	}

	Rectangle2D union(Point2D a, Point2D b)
	{
		if (a == null || b == null) return Rectangle2D.EMPTY;
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		return new Rectangle2D(x,y,width,height);
	}
	
	private double frameToScaleX(double value)
	{
		Rectangle frame = getPlotFrame();
		double chartZeroX = frame.getX();
		double chartWidth = frame.getWidth();
		return computeBound(value, chartZeroX, chartWidth, xAxis.getLowerBound(), xAxis.getUpperBound(), false);
	}

	private double frameToScaleY(double value)
	{
		Rectangle frame = getPlotFrame();
		double chartZeroY = frame.getY();
		double chartHeight = frame.getHeight();
		return computeBound(value, chartZeroY, chartHeight, yAxis.getLowerBound(), yAxis.getUpperBound(),true) + 0.5;
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

//	public void resetGates()
//	{
//		Node chartPlotArea = scatter.lookup(".chart-plot-background");
//		for (int i=0; i< gateDefs.size(); i++)
//		{
//			Rectangle def = gateDefs.get(i);
//			Rectangle display = gateDefs.get(i);
//		}
//		
//	
//	}

	Rectangle getScaleRect(Rectangle def, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle(frame.getX() + def.getX() * frameWidth,
				frame.getY() + def.getY() * frameHeight, 
				def.getWidth() * frameWidth, def.getHeight() * frameHeight);
		return r;
	}

	void rescaleRect(Rectangle def, Rectangle frame, Rectangle display)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		display.setX(frame.getX() + def.getX() * frameWidth);
		display.setY(frame.getY() + def.getY() * frameHeight);
		display.setWidth(def.getWidth() * frameWidth);
		display.setHeight(def.getHeight() * frameHeight);
	}

	Rectangle rectDef(Rectangle child, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle((child.getX() - frame.getX()) / frameWidth,
				(child.getY() - frame.getY()) / frameHeight, 
				child.getWidth() / frameWidth, child.getHeight() / frameHeight);
		return r;
	}



}
