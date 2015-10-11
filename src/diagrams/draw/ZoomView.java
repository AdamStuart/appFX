package diagrams.draw;

import gui.Borders;

import java.awt.Dimension;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
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
	private Rectangle viewport;				// internal rectangle to represent the window
	private Rectangle zoomClip;				// dynamically resizing clipping to this control
	private AnchorPane zoomAnchor;			// the parent of the zoomImageView and viewport
	private Pane drawPane;					// the window onto the canvas
	private Controller controller;					// the Controller, to can access other components

	static int CANVAS_W = 2000;
	static int CANVAS_H = 2000;
	static Dimension CANVAS_SIZE = new Dimension(CANVAS_W,CANVAS_H);
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
		zoomAnchor.setBorder(Borders.redBorder);
		zoomAnchor.getChildren().addAll(zoomImageView, viewport );

	}
	public void zoomChanged()
	{
		WritableImage img = new WritableImage(CANVAS_W, CANVAS_H);
		Node clip = drawPane.getClip();
		Canvas canvas = controller.getCanvas();
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
		double scaleX = 1;
		double scaleY = 1;
		Rectangle canvasBounds = new Rectangle(0,0,CANVAS_W,CANVAS_H);
		Bounds canvasViewport = drawPane.getBoundsInParent();
		Bounds controlBounds = zoomAnchor.getBoundsInLocal();

		scaleX = drawPane.getScaleX() * canvasBounds.getWidth() / controlBounds.getWidth();
		scaleY = drawPane.getScaleY() * canvasBounds.getHeight() / controlBounds.getHeight();
		
		double offsetX = canvasViewport.getMinX() / scaleX;
		double offsetY = canvasViewport.getMinY() / scaleY;
		double width = canvasViewport.getWidth() / scaleX;
		double hght = canvasViewport.getHeight() / scaleY;
		RectangleUtil.setRect(viewport, -offsetX, -offsetY, width, hght);
	}
	// **-------------------------------------------------------------------------------
	// ZOOM RECT MOUSE HANDLERS

	private Point2D startPoint, currentPoint;
	private boolean dragging = false;
	private boolean resizing = false;
	private double startX = 0, startY = 0;
	
	private void setupViewport()
	{
		viewport = new Rectangle(20, 20, 80, 80);
		viewport.setId("viewport");
		viewport.setFill(Color.TRANSPARENT);			// note:  setFill(null) will disable mouse events!
		viewport.setStroke(Color.DARKORCHID);
		viewport.setStrokeWidth(1);
		viewport.getStrokeDashArray().addAll(5.,10., 8. ,10.);
		
		viewport.setOnMousePressed(event -> {
			startX = event.getX();
			startY = event.getY();
			currentPoint = new Point2D(startX, startY);
			startPoint = currentPoint;
			dragging = resizing = false;
			if (RectangleUtil.inCorner(event))
			{
				resizing = true;
				startPoint = RectangleUtil.oppositeCorner(event);
			}
			else dragging = true;
			event.consume();
		}) ;
		
		viewport.setOnMouseDragged(event -> {
			assert( (event.getTarget() instanceof Rectangle));
			Rectangle viewport = (Rectangle) event.getTarget();
			AnchorPane p = (AnchorPane) viewport.getParent();
			double w = zoomImageView.getFitWidth();
			double h = zoomImageView.getFitHeight();
			Point2D parentSize = new Point2D(w, h);
			
			currentPoint = new Point2D(event.getX(), event.getY());
			if (parentSize.getX() < event.getX()) return;
			if (parentSize.getY() < event.getY()) return;
			if (w < event.getX()) return;
			if (h < event.getY()) return;
			
			if (resizing)
			{
				double aspectRatio = viewport.getWidth() / viewport.getHeight();
				Rectangle r = RectangleUtil.constrainedUnion(startPoint, currentPoint, aspectRatio);
				RectangleUtil.setRect(viewport, r);
			}
			else
			{
				RectangleUtil.moveRect(viewport, RectangleUtil.diff(currentPoint,  startPoint), parentSize);
				startPoint = currentPoint;
			}
			event.consume();
			zoomToRect(viewport);
		});
	
	}

	// **-------------------------------------------------------------------------------
// unused
//	void zoomToRect(Rectangle canvas, Rectangle viewport )
//	{
//		double scaleX = viewport.getWidth() * drawPane.getWidth() / canvas.getWidth();		
//		double scaleY = viewport.getHeight()  * drawPane.getHeight() / canvas.getHeight();	
//		double offsetX = scaleX * (viewport.getX() - canvas.getX());
//		double offsetY = scaleY * (viewport.getY() - canvas.getY());
//		drawPane.setScaleX(scaleX); 
//		drawPane.setScaleY(scaleY); 
//		drawPane.setTranslateX(-offsetX * scaleX); 
//		drawPane.setTranslateY(-offsetY * scaleY); 
////		scale.setValue(scaleX);	
////		translateX.setValue(-offsetX * scaleX);	
////		translateY.setValue(-offsetY * scaleY);	
//	}
	
	// **-------------------------------------------------------------------------------
	// reset the window's scale and translate onto the canvas to match the change in 
	//	our viewport within the ZoomView.
	
	
	void zoomToRect(Rectangle r)
	{
		Rectangle canvasBounds = new Rectangle(0,0,CANVAS_W,CANVAS_H);
		
		double scaleX = canvasBounds.getWidth() / zoomAnchor.getWidth();		
		double scaleY =  canvasBounds.getHeight() / zoomAnchor.getHeight();		
		double dx = -scaleX * r.getX();
		double dy = -scaleY * r.getY();
		drawPane.setTranslateX(drawPane.getTranslateX() + dx); 
		drawPane.setTranslateY(drawPane.getTranslateY() +dy); 
		double oldXscale = drawPane.getScaleX();
		double oldYscale = drawPane.getScaleY();
//		drawPane.setScaleX(scaleX * oldXscale); 
//		drawPane.setScaleY(scaleY * oldYscale); 
	}
	
	// **-------------------------------------------------------------------------------
	// derive a rectangle that expresses how the kid lies within the parent
	
	public static Rectangle ratioRect(Rectangle parent, Rectangle kid)
	{
		assert(parent.getWidth() > 0 && kid.getHeight() > 0);
		double relX = kid.getWidth() / parent.getWidth();
		double relY = kid.getHeight() / parent.getHeight();
		double ratioX = (kid.getX() - parent.getX()) * relX;
		double ratioY = (kid.getY() - parent.getX()) * relX;
		double ratioW = kid.getWidth() * relX;
		double ratioH = kid.getHeight() * relY;
		return new Rectangle(ratioX, ratioY, ratioW, ratioH);
	}
	
	// **-------------------------------------------------------------------------------
	// apply a ratio rectangle to derive the kid from the parent

	public static Rectangle kidRect(Rectangle parent, Rectangle ratio)
	{
		double kidW = parent.getWidth() * ratio.getWidth();
		double kidH = parent.getHeight() * ratio.getHeight();
		double kidX = (parent.getX() + ratio.getX() * kidW);
		double kidY = (parent.getY() + ratio.getY() * kidH);
		return new Rectangle(kidX, kidY, kidW, kidH);
	}


}
