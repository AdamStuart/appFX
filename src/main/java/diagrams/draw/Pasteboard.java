package diagrams.draw;

import java.io.File;
import java.util.List;

import animation.NodeVisAnimator;
import diagrams.draw.Action.ActionType;
import diagrams.draw.App.Tool;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import model.AttributeMap;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;

// Pasteboard 
/*
 *  The primary role of the Pasteboard is to handle mouse events that don't hit any node,
 *  and drop events that will create new nodes depending on the files / data that 
 *  are in the drop.  Key events are also caught.
 *  
 *  The Pasteboard also remembers the state of which tool is active and what default
 *  attributes will be assigned to new nodes.
 */
public class Pasteboard
{
	//@formatter:off
	private static final String INFO_LABEL_ID = "infoLabel";
	private static final String ELEMENT_NAME = "Pasteboard";

	
	private Controller controller;
	public Controller getController()		{ return controller; }
	private NodeFactory factory;
	private EdgeFactory edgeFactory;
	public NodeFactory getNodeFactory()		{ return factory; }
	public EdgeFactory getEdgeFactory()		{ return edgeFactory; }
	private ShapeFactory shapeFactory;
	public ShapeFactory getShapeFactory()	{ return shapeFactory; }
	private Selection selectionMgr;
	public Selection getSelectionMgr()		{ return selectionMgr; }
	private Pane drawPane;
	public Pane getPane()					{ return drawPane;	}
	private Rectangle marquee;
	public Rectangle getMarquee()			{ return marquee;	}
	private Label infoLabel;

	SimpleDoubleProperty widthProperty = new SimpleDoubleProperty();
	SimpleDoubleProperty heightProperty = new SimpleDoubleProperty();
	public double getWidth()	{ return widthProperty.get();	}
	public double getHeight()	{ return heightProperty.get();	}
	public  void setWidth(double d)	{  widthProperty.set(d);	}
	public void setHeight(double d)	{  heightProperty.set(d);	}
	public SimpleDoubleProperty widthProperty()	{  return widthProperty;	}
	public SimpleDoubleProperty heightProperty()	{  return heightProperty;	}

	private Shape activeShape;
	public Shape getActiveShape()		{ return activeShape;	}
	public void setActiveShape(Shape s)	{ activeShape = s;	}
	//@formatter:on
	/**-------------------------------------------------------------------------------
	/**Canvas (Pane pane, Controller ctrl
	 * @param ctrl	-- the Controller that is parent to this object
	 * @param pane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public Pasteboard(Pane pane, Controller ctrl) 
	{
		drawPane = pane;
		setWidth(2000);
		setHeight(2000);
		controller = ctrl;
		factory = new NodeFactory(this);
		edgeFactory = new EdgeFactory(this);
		shapeFactory = factory.getShapeFactory();
		marquee = shapeFactory.makeMarquee();
		selectionMgr = new Selection(this);
//		pane.getChildren().add(marquee);
		setupMouseKeyHandlers(drawPane);
		setupPasteboardDrops(drawPane);
		infoLabel = new Label("");
		infoLabel.setId(INFO_LABEL_ID);
		drawPane.getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
//		turnOnClipping();
	}
	
	private Rectangle clipRect = new Rectangle();
	private void turnOnClipping()
	{
		drawPane.setClip(clipRect);
		drawPane.heightProperty().addListener((o, oldVal, newVal) -> { clipRect.heightProperty().set((double) newVal);    });
		drawPane.widthProperty().addListener((o, oldVal, newVal) -> { clipRect.widthProperty().set((double) newVal);    });
	}
	/*
	 * // Handle highlighting the canvas as mouse enters, and resetting as it leaves
	 */
	
	private void setupPasteboardDrops(Pane drawPane)
	{
		drawPane.setOnDragEntered(e -> 	{  	highlightPasteboard(true);					e.consume();	});
		drawPane.setOnDragExited(e -> 	{	highlightPasteboard(false);					e.consume();	});
		drawPane.setOnDragOver(e -> 	{	e.acceptTransferModes(TransferMode.ANY);	e.consume();  	});
		drawPane.setOnDragDropped(e ->	{ 	handlePasteboardDrop(e);					e.consume();  	});
	}	

	private void highlightPasteboard(boolean isHighlighted)
	{
		InnerShadow shadow = null;
		if (isHighlighted)
		{
			shadow = new InnerShadow();
			shadow.setOffsetX(2.0);
			shadow.setColor(Color.web("#9F46AF"));
			shadow.setOffsetY(2.0);
		}
		drawPane.setEffect(shadow);
	}
	
	private void handlePasteboardDrop(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		e.acceptTransferModes(TransferMode.ANY);
//		Set<DataFormat> formats = db.getContentTypes();
//		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasFiles())
		{
			List<File> files = db.getFiles();
			if (files != null)
			{
				controller.getUndoStack().push(ActionType.Add, " file");
				int offset = 0;
				for (File f : files)
				{
					if (FileUtil.isCSS(f))		// css files are added to the Scene
						controller.addStylesheet(f);
					else
					{
						offset += 20;
						StackPane border = controller.getNodeFactory().handleFileDrop(f, e.getX()+offset, e.getY()+offset);
						if (border != null)
							controller.add(border);
					}
				}
			}
		}
		e.consume();
	}
	
	//-------------------------------------------------------------------------------
	public String getState()		
	{
		AttributeMap map = new AttributeMap();
		map.put("", "");
		map.put("tool", curTool.toString());
		map.putBool("infoShown", infoLabel.isVisible());
		map.put("fill", defaultFill.toString());
		map.put("stroke", defaultStroke.toString());
		return map.makeElementString(ELEMENT_NAME);
	}
	
	//-------------------------------------------------------------------------------
	public void setState(String s)		
	{
		AttributeMap map = new AttributeMap(s);
		setTool(Tool.fromString(map.get("tool")));
		infoLabel.setVisible(map.getBool("infoShown")); 
		defaultFill = map.getPaint("fill");
		defaultStroke = map.getPaint("stroke");
	}
	
	//-------------------------------------------------------------------------------
	private Group grid;
	public Group getGrid()	{  return grid;  }
	
	public void makeGrid(Button toggler, ScrollPane scrlPane)
	{
		grid = new Group();
		grid.setId("grid");
		double res = Screen.getPrimary().getDpi();			// assumes inches
		Parent p  = getPane().getParent();
		if (scrlPane != null)
		{
//			Pane drawPane = (Pane) scrlPane.getContent();
//			ReadOnlyObjectProperty<Bounds> bds = drawPane.boundsInParentProperty();
//			DoubleBinding leftProp = Bindings.selectDouble(bds, "minX");
//			DoubleBinding rightProp = Bindings.selectDouble(bds, "maxX");
//			drawPane.layoutXProperty();
//			DoubleProperty yProp = drawPane.layoutYProperty();
//			ReadOnlyDoubleProperty widthProp = drawPane.widthProperty();
//			ReadOnlyDoubleProperty heightProp = drawPane.heightProperty();
			
			double canvasWidth = getWidth();
			double canvasHeight = getHeight();
			double nLines = canvasWidth / res;
			for (int i = 0; i< nLines; i++)
			{
				Line vert = new Line(res * i, 0, res * i, canvasHeight);
				vert.setStrokeWidth(0.25);
//				drawPane.heightProperty().addListener((o, oldVal, newVal) -> { clipRect.heightProperty().set((double) newVal);  });
//				drawPane.layoutYProperty().addListener((o, oldVal, newVal) -> { vert.layoutYProperty().set((double) newVal); 	});
//				drawPane.heightProperty().addListener((o, oldVal, newVal) -> { System.out.println("height changed");  vert.endYProperty().set((double) newVal);  });

				
				
//				vert.startYProperty().bind(drawPane.layoutYProperty());
//				vert.endYProperty().bind(drawPane.heightProperty());
				Line horz = new Line(0, res * i, canvasWidth, res * i);
				horz.setStrokeWidth(0.25);
//				horz.startXProperty().bind(leftProp);
//				horz.endXProperty().bind(rightProp);
				grid.getChildren().addAll(vert, horz);
			}		
			grid.setMouseTransparent(true);
			getController().add(grid);
		}
		new NodeVisAnimator(grid, toggler);

	}
	public void showGrid(boolean vis)	{	grid.setVisible(vis);	}
	public boolean isGridVisible()		{	return	grid.isVisible();	}
	
	//-------------------------------------------------------------------------------
	public void showInfo(String s) {	infoLabel.setText(s);	infoLabel.setVisible(true);		}
	
/**-------------------------------------------------------------------------------
 *		These are the mouse handlers for the canvas layer 
 *		Nodes will intercept their own events, so this is just to create new nodes
 *		based on the current tool, and draw the marquee if the selection arrow is active
 *		
 */
	private void setupMouseKeyHandlers(Pane drawPane2) {
		drawPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedHandler());
		drawPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new MouseClickHandler());
		drawPane.addEventHandler(MouseEvent.MOUSE_MOVED, new MouseMovedHandler());
		drawPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, new MouseDraggedHandler());
		drawPane.addEventHandler(MouseEvent.MOUSE_RELEASED, new MouseReleasedHandler());
		drawPane.addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());
		
	}

	private Point2D startPoint = null;		// remember where the mouse was pressed
//	private Point2D offset = null;			// distance from startPoint to the origin of the target
	private Point2D curPoint = null;		// mouse location in current event
	private Line dragLine = null;			// a polyline edge

	static boolean verbose = false;

//
	public void startDragLine(double x, double y) {
		if (dragLine == null)
		{
			dragLine = new Line();
			drawPane.getChildren().add(dragLine);	
		}
		dragLine.setStartX(x);
		dragLine.setStartY(y);
		dragLine.setEndX(x);
		dragLine.setEndY(y);
	}
	public void removeDragLine() {
		if (dragLine != null)
		{
			drawPane.getChildren().remove(dragLine);	
			dragLine = null;
		}
	}
//	public void setLastClick(double x, double y) {
//		dragLine.setEndX(x);
//		dragLine.setEndY(y);
//	}



	private final class MousePressedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{
			if (verbose)
				System.out.println("MousePressedHandler, activeShape: " + activeShape);
			// do nothing for a right-click
			if (event.isSecondaryButtonDown()) 
			{ 
				// TODO popup canvas commands
				event.consume();
				return;	
			}
			if (curTool == Tool.Polyline)
			{
				if (verbose) System.out.println("MousePressedHandler, Polyline: " );
				if (activeShape == null)
				{
					activeShape = shapeFactory.makeNewNode(new AttributeMap("ShapeType:" + curTool.name()));
 					drawPane.getChildren().add(activeShape);

				}
				Polyline p = (Polyline) activeShape;
				if (event.getClickCount() > 1)
				{
					activeShape = null;
					removeDragLine();
				}
				else if (LineUtil.onVertex(startPoint, p) == 0)
				{
					activeShape = null;
					removeDragLine();
				}
				else p.getPoints().addAll(event.getX(), event.getY());
				startDragLine(event.getX(), event.getY());
				p.setFill(null);
				event.consume();
				return;
			}
			
			startPoint = new Point2D(event.getX(), event.getY());
			if (activeShape instanceof Polygon)
			{
				if (verbose) System.out.println("MousePressedHandler, Polygon: " );
				Polygon p = (Polygon) activeShape;
				if (event.getClickCount() > 1)							activeShape = null;
				else if (LineUtil.onVertex(startPoint, p) >= 0)		activeShape = null;
				else p.getPoints().addAll(event.getX(), event.getY());
				event.consume();
				return;
			}
			if (activeShape instanceof Line)
			{
				if (verbose) System.out.println("MousePressedHandler, Line: " );
				Line line = (Line) activeShape;
				line.setVisible(false);
				event.consume();
				return;
			}
//			controller.getUndoStack().push(ActionType.Select);
			getSelectionMgr().clear();  
	
			activeShape = shapeFactory.makeNewNode(new AttributeMap("ShapeType:" + curTool.name()));
			if (activeShape instanceof Polygon)
			{
				Polygon p = (Polygon) activeShape;
				p.getPoints().addAll(event.getX(), event.getY());
			}
			if (activeShape instanceof Polyline)
			{
				Polyline p = (Polyline) activeShape;
				p.getPoints().addAll(event.getX(), event.getY());
			}
			if (activeShape instanceof Line)
			{
				Line p = (Line) activeShape;
				p.setStartX(event.getX());
				p.setStartY(event.getY());
				p.setEndX(event.getX());
				p.setEndY(event.getY());
			}
			if (activeShape instanceof Shape1)
			{
			}
//			if (activeShape instanceof Shape2)
//			{
//				Shape2 shape = (Shape2) activeShape;
//			}
			if (activeShape == null) 
			{
				activeShape = marquee;
				RectangleUtil.setRect(marquee, event.getX(), event.getY(), 8,8);
				marquee.setVisible(true);
				if (marquee.getParent() == null)
					controller.add(marquee);
				if (event.getClickCount() > 2)
				{
					TextArea newText = new TextArea("Comments: ");
					newText.setBackground(null);
					newText.setLayoutX(event.getX());
					newText.setLayoutY(event.getY());
					newText.setPrefColumnCount(20);
					newText.setPrefRowCount(10);
					newText.selectAll();
					newText.requestFocus();
					controller.add(newText);
		
					newText.skinProperty().addListener(new ChangeListener<Skin<?>>() {			// doesn't work!

				        @Override
				        public void changed(
				          ObservableValue<? extends Skin<?>> ov, Skin<?> t, Skin<?> t1) {
				            if (t1 != null && t1.getNode() instanceof Region) {
				                Region r = (Region) t1.getNode();
				                r.setBackground(Background.EMPTY);

				                r.getChildrenUnmodifiable().stream().
				                        filter(n -> n instanceof Region).
				                        map(n -> (Region) n).
				                        forEach(n -> n.setBackground(Background.EMPTY));

				                r.getChildrenUnmodifiable().stream().
				                        filter(n -> n instanceof Control).
				                        map(n -> (Control) n).
				                        forEach(c -> c.skinProperty().addListener(this)); // *
				            }
				        }
				    });
					newText.setBackground(Background.EMPTY);
			    
//				    
				}
			}
			else
			{
				controller.getUndoStack().push(ActionType.New, " " + curTool.name());
				drawPane.getChildren().add(activeShape);
				getSelectionMgr().select(activeShape);
			}
			
//			factory.setStartPoint(new Point2D(event.getX(), event.getY()));
			event.consume();
		}
	}

	/** 
	 *  MouseDraggedHandler
	 */
	private final class MouseDraggedHandler implements EventHandler<MouseEvent> {
		@Override public void handle(final MouseEvent event) {

			if (event.isSecondaryButtonDown())  return;		// do nothing for a right-click drag
			if (verbose)	
				System.out.println("MouseDraggedHandler, activeShape: " + activeShape);

			// store current cursor position
			curPoint = new Point2D(event.getX(), event.getY());
			if (startPoint == null) 
				startPoint = curPoint;
			if (curTool == Tool.Arrow)	
			{
				Rectangle r = RectangleUtil.union(startPoint, curPoint);
				getSelectionMgr().clear();
				getSelectionMgr().select(r);
//				marquee.setVisible(true);
				RectangleUtil.setRect(marquee, r);
			}
			else setActiveShapeBounds();
				
			event.consume();
		}
		//---------------------------------------------------------------------------
		private void setActiveShapeBounds()
		{
			double left = Math.min(curPoint.getX(),  startPoint.getX());
			double top = Math.min(curPoint.getY(),  startPoint.getY());
			double w = Math.abs(curPoint.getX() - startPoint.getX());
			double h = Math.abs(curPoint.getY() - startPoint.getY());
			if (verbose)
				System.out.println("setActiveShapeBounds, activeShape: " + activeShape);
			if (activeShape instanceof Rectangle)
			{
				Rectangle r = (Rectangle) activeShape;
//				r.setVisible(true);
				RectangleUtil.setRect(r, left, top ,w, h);
			}
			if (activeShape instanceof Circle)
			{
				Circle c = (Circle) activeShape;
//				c.setVisible(true);
				c.setCenterX(startPoint.getX());
				c.setCenterY(startPoint.getY());
				double rad = Math.sqrt(w * w + h * h);
				c.setRadius(rad);
			}
			
			if (activeShape instanceof Polygon)
			{
				Polygon p = (Polygon) activeShape;
//				p.setVisible(true);
				int nPts = p.getPoints().size();
				if (nPts > 1)
				{
					p.getPoints().set(nPts-2, curPoint.getX());
					p.getPoints().set(nPts-1, curPoint.getY());
				}
			}
			
			if (activeShape instanceof Polyline)
			{
				Polyline p = (Polyline) activeShape;
//				p.setVisible(true);
				p.setFill(null);
				int nPts = p.getPoints().size();
				if (nPts > 1)
				{
					p.getPoints().set(nPts-2, curPoint.getX());
					p.getPoints().set(nPts-1, curPoint.getY());
				}
			}
			
			if (activeShape instanceof Line)
			{
				Line p = (Line) activeShape;
				p.setEndX(curPoint.getX());
				p.setEndY(curPoint.getY());
			}
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseReleasedHandler
	 */

	private final class MouseReleasedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) {
			curPoint = new Point2D(event.getX(), event.getY());
			controller.remove(marquee);
//			if (RectangleUtil.isRectangleSizeTooSmall(startPoint, curPoint)) 		return;
//
//			if (startPoint.distance(curPoint) < 10)
//			{
//				TextArea newText = new TextArea("Comments: ");
//				newText.setBackground(null);
//				newText.setLayoutX(event.getX());
//				newText.setLayoutY(event.getY());
//				controller.add(newText);
//				newText.selectAll();
//				newText.requestFocus();
//				
//			}
			startPoint = curPoint = null;
			drawPane.requestFocus();		// needed for the key event handler to receive events
			if (!isPoly(getTool()))
				resetTool();
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseClickHandler
	 */

	private final class MouseClickHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{			
			controller.remove(marquee);
			startPoint = curPoint = null;
			drawPane.requestFocus();		// needed for the key event handler to receive events
			resetTool();
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseMovedHandler
	 */

	private final class MouseMovedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{			
			if (dragLine != null)
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  KeyHandler
	 *  
	 *  unmodified keys switch the tool
	 *  backspace = delete
	 *  escape or space bar to terminate polyline
	 */
	
	private final class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {

 			KeyCode key = event.getCode();
 			
 			if (key.isArrowKey())	getSelectionMgr().translate(key);
			else if (KeyCode.DELETE.equals(key)) 		getSelectionMgr().deleteSelection();		// create an undoable action
			else if (KeyCode.BACK_SPACE.equals(key)) 	getSelectionMgr().deleteSelection();
			
 			else if (KeyCode.R.equals(key)) 	setTool(Tool.Rectangle);
			else if (KeyCode.C.equals(key)) 	setTool(Tool.Circle);
			else if (KeyCode.P.equals(key)) 	setTool(Tool.Polygon);
			else if (KeyCode.L.equals(key)) 	setTool(Tool.Line);
			else if (KeyCode.W.equals(key)) 	setTool(Tool.Polyline);
			
//			else if (KeyCode.X.equals(key)) 	setTool(Tool.Xhair);
			else if (KeyCode.ESCAPE.equals(key)) 	terminatePoly();
			else if (KeyCode.SPACE.equals(key)) 	terminatePoly();
		}

		private void terminatePoly() {
			if (getActiveShape() instanceof Polyline)
			{
				Polyline p = (Polyline) getActiveShape();
				terminatePoly(p.getPoints());
			}
				
			if (getActiveShape() instanceof Polygon)		{
				Polygon p = (Polygon) getActiveShape();
					terminatePoly(p.getPoints());
			}
		}
		private void terminatePoly(ObservableList<Double> pts) {
			pts.addAll(pts.get(0),pts.get(1));
			setActiveShape(null);
			resetPoly();
		}
	}	
	//---------------------------------------------------------------------------
	Tool curTool = Tool.Arrow;
	boolean sticky = false;
	public Tool getTool()	{ return curTool;	}
	public void resetTool()		
	{		
		if (sticky) return;
		if (isPoly(curTool)) 	return;
		if (dragLine != null && dragLine.isVisible()) 
			removeDragLine();
		setTool(Tool.Arrow);	
	}
	public void resetPoly()		
	{		
		if (dragLine != null && dragLine.isVisible()) 
			removeDragLine();
		if (sticky) return;
		setTool(Tool.Arrow);	
	}
	
	boolean isPoly(Tool t)	{		return (Tool.Polyline == t || Tool.Polygon == t);	}
	
	public void setTool(Tool inTool)
	{
		if (curTool == inTool) sticky = true;
		if (curTool == Tool.Arrow) sticky = false;
		curTool = inTool;
		activeShape = null;
		ToggleGroup group = controller.getToolGroup();
		for (Toggle t : group.getToggles())
		{
			if (t instanceof ToggleButton)
			{
				ToggleButton b = (ToggleButton) t;
				if (b.getId().equals(curTool.name()))
				{
					group.selectToggle(t);
					break;
				}
			}
		}
		if (verbose) 
			System.out.println("Tool set to: " + inTool.toString() + (sticky ? "!!" : ""));
	}
	//---------------------------------------------------------------------------
	Paint defaultStroke = Color.BLACK;			// TODO  pref
	Paint defaultFill = Color.WHITESMOKE;
	
	public Paint getDefaultFill()		{		return 	defaultFill;	}
	public Paint getDefaultStroke()		{		return defaultStroke;	}
	public void setDefaultFill(Paint p)	{		defaultFill = p;	}
	public void setDefaultStroke(Paint p){		defaultStroke = p;	}
	
}

