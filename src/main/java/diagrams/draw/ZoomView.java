package diagrams.draw;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.RectangleUtil;
/*
 *  A zoomview is a control that shows your canvas in a thumbnail view, with
 *  a rectangle to describe the viewport's bounds.  This accepts mouse events
 *  to pan or zoom the main canvas.
 */
public class ZoomView
{
	private ImageView zoomImageView;		// this holds the snapshot taken of the drawPane
	private Rectangle viewport;				// internal rectangle to represent the window with a purple dash
	private Rectangle zoomClip;				// dynamically resizing clipping to this control
	private AnchorPane zoomAnchor;			// the parent of the zoomImageView and viewport
	private Pane drawPane;					// the window onto the canvas
	private Controller controller;			// the Controller, to can access other components

//	static int CANVAS_W = 2000;
//	static int CANVAS_H = 2000;
////	static Dimension CANVAS_SIZE = new Dimension(CANVAS_W,CANVAS_H);
	// **-------------------------------------------------------------------------------
	
	public ZoomView(AnchorPane parent, Pane inPane, Controller ctrl)
	{
		zoomAnchor = parent;
		drawPane = inPane;
		controller = ctrl;
		zoomImageView = new ImageView();
		zoomImageView.setFitHeight(200);
		zoomImageView.setFitWidth(200);
		zoomImageView.setPreserveRatio(true);
		
		setupViewport();
		zoomClip = new Rectangle();
		zoomClip.widthProperty().bind(zoomImageView.fitWidthProperty());
		zoomClip.heightProperty().bind(zoomImageView.fitHeightProperty());
		zoomAnchor.setClip(zoomClip);
//		zoomAnchor.setBorder(Borders.redBorder);
		zoomAnchor.getChildren().addAll(zoomImageView, viewport );

	}
	public void zoomChanged()
	{
		Pasteboard canvas = controller.getPasteboard();
		WritableImage img = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
		Node clip = drawPane.getClip();
		boolean showGrid = canvas.isGridVisible();
		canvas.showGrid(false);
		Border b = drawPane.getBorder();
		drawPane.setBorder(null);
		drawPane.setClip(null);
		img = drawPane.snapshot(null,img);
		zoomImageView.setImage(img);
		drawPane.setClip(clip);
		drawPane.setBorder(b);
		canvas.showGrid(showGrid);
		setViewPortToPaneTransform();
	}
	// **-------------------------------------------------------------------------------
	//  make sure the rectangle (viewport) in the ZoomView corresponds 
	// 	to the window onto the canvas
	
	private void setViewPortToPaneTransform()
	{
		Pasteboard canvas = controller.getPasteboard();
		Rectangle canvasBounds = new Rectangle(0,0,canvas.getWidth(),canvas.getHeight());
		Bounds canvasViewport = drawPane.getBoundsInLocal();  // was getBoundsInParent
	
		Parent parent = drawPane.getParent();
		while (parent != null && !(parent instanceof ScrollPane))
			parent = parent.getParent();
		
		if (parent instanceof ScrollPane)
		{
			canvasViewport = ((ScrollPane) parent).getViewportBounds();
		}
		Bounds controlBounds = zoomAnchor.getBoundsInLocal();

		double scaleX = drawPane.getScaleX() * canvasBounds.getWidth() / controlBounds.getWidth();
		double scaleY = drawPane.getScaleY() * canvasBounds.getHeight() / controlBounds.getHeight();
		
		double offsetX = canvasViewport.getMinX() / scaleX;
		double offsetY = canvasViewport.getMinY() / scaleY;
		double width = canvasViewport.getWidth() / scaleX;
		double hght = canvasViewport.getHeight() / scaleY;
		RectangleUtil.setRect(viewport, -offsetX, -offsetY, width, hght);
	}
	// **-------------------------------------------------------------------------------
	// ZOOM RECT MOUSE HANDLERS

	private Point2D startPoint, currentPoint;
//	private boolean dragging = false;
	private boolean resizing = false;
	private double startX = 0, startY = 0;
	
	private void setupViewport()
	{
		viewport = new Rectangle(20, 20, 80, 80);
		viewport.setId("viewport");
		viewport.setFill(Color.TRANSPARENT);			// note:  setFill(null) will disable mouse events!
		viewport.setStroke(Color.DARKORCHID);
		viewport.setStrokeWidth(1.618);
		viewport.getStrokeDashArray().addAll(5.,10., 8. ,10., 2., 10., 8. ,10.);
		
		viewport.setOnMousePressed(event -> {
			startX = event.getX();
			startY = event.getY();
			currentPoint = new Point2D(startX, startY);
			startPoint = currentPoint;
			resizing = false;		//dragging = 
			if (RectangleUtil.inCorner(event))
			{
				resizing = true;
				startPoint = RectangleUtil.oppositeCorner(event);
			}
//			else dragging = true;
			event.consume();
		}) ;
		
		viewport.setOnMouseDragged(event -> {
			assert( (event.getTarget() instanceof Rectangle));
			Rectangle vp = (Rectangle) event.getTarget();
			assert( (vp == viewport));
			AnchorPane p = (AnchorPane) vp.getParent();
//			double w = zoomImageView.getFitWidth();
//			double h = zoomImageView.getFitHeight();
//			Point2D parentSize = new Point2D(w, h);
			
			currentPoint = new Point2D(event.getX(), event.getY());
//			if (parentSize.getX() < event.getX()) return;
//			if (parentSize.getY() < event.getY()) return;
//			if (w < event.getX()) return;
//			if (h < event.getY()) return;
			
			if (resizing)
			{
				double aspectRatio = vp.getWidth() / vp.getHeight();
				Rectangle r = RectangleUtil.constrainedUnion(startPoint, currentPoint, aspectRatio);
				RectangleUtil.setRect(vp, r);
			}
			else
			{
				Point2D delta = RectangleUtil.diff(currentPoint,  startPoint);
				System.out.println("Delta: " + delta);
				RectangleUtil.moveRect(vp, delta, null);//parentSize
				viewport.setX(vp.getX() + currentPoint.getX() - startPoint.getX());
				viewport.setY(vp.getY() + currentPoint.getY() - startPoint.getY());
				startPoint = currentPoint;
			}
			event.consume();
			setDrawPaneToViewPort();
		});
	}
	
	String rectToStr(final Rectangle r)
	{
		return String.format("%s: [%4.1f, %4.1f, %4.1f, %4.1f]",
			r.getId(), r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	// **-------------------------------------------------------------------------------
	// reset the window's scale and translate onto the canvas to match the change in 
	//	our viewport within the ZoomView.
	
	
	void setDrawPaneToViewPort()	//Rectangle r
	{
		Pasteboard board = controller.getPasteboard();
		double scaleX = board.getWidth() / zoomAnchor.getWidth();		
		double scaleY =  board.getHeight() / zoomAnchor.getHeight();		
		double dx = scaleX * viewport.getX();		//
		double dy = scaleY * viewport.getY();  //
		double x = drawPane.getTranslateX() + dx;
		double y = drawPane.getTranslateY() + dy;
		drawPane.setTranslateX(x); 
		drawPane.setTranslateY(y); 
//		viewport.setX(x);
//		viewport.setY(y);
//		drawPane.setScaleX(scaleX); 
//		drawPane.setScaleY(scaleY); 
		
		
		String rstatus = rectToStr(viewport);
		String status = String.format("%4.1f,\t %4.1f", x, y);
		String dxdy = String.format("%4.1f,\t %4.1f", dx, dy);
		controller.reportStatus(dxdy);
		controller.setStatus2(status);
		controller.setStatus3(rstatus);
//		double oldXscale = drawPane.getScaleX();
//		double oldYscale = drawPane.getScaleY();
//		drawPane.setScaleX(scaleX * oldXscale); 
//		drawPane.setScaleY(scaleY * oldYscale); 
	}
	

}
