package diagrams.draw;

import gui.Effects;

import java.io.File;
import java.util.List;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.FileUtil;
import util.RectangleUtil;
import util.StringUtil;
import diagrams.draw.Action.ActionType;
import diagrams.draw.App.Tool;

public class ShapeFactory
{
	private Model model;
	private Canvas drawLayer;
	private UndoStack undoStack;
	static double EPSILON = 4;
	/*
	 *  The ShapeFactory is responsible for create new nodes that are shapes.  
	 *  Largely this is about defining the mouse event and drop handlers for the shapes.
	 *  
	 *  makeMarquee creates the selection rectangle that is added on canvas drags.
	 */
	public ShapeFactory(Canvas l, UndoStack u)
	{
		drawLayer = l;
		undoStack = u;
		model = drawLayer.getController().getDrawModel();
	}
	//-----------------------------------------------------------------------
	public Rectangle makeMarquee()
	{
		 AttributeMap attrMap = new AttributeMap();
		 attrMap.putAll("id", "Marquee", "-fx-stroke", "Green", "-fx-fill", "TRANSPARENT", "-fx-stroke-width", "1");
		 Rectangle marquee = (Rectangle) makeNewShape("Rectangle", attrMap); 
//		marquee.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		marquee.getStrokeDashArray().addAll(3.0,7.0,3.0,7.0);
//		marquee.setOpacity(0.5);
		return marquee;
	}
	public static boolean isMarquee(Node node)  { return node != null && "Marquee".equals(node.getId()); }

	//-----------------------------------------------------------------------

	public Shape makeNewNode(Tool type, AttributeMap attrMap)
	{
		if (type == Tool.Circle)	return makeNewShape("Circle", attrMap);	
		if (type == Tool.Rectangle)	return makeNewShape("Rectangle", attrMap);	
		if (type == Tool.Polygon)	return makeNewShape("Polygon", attrMap);	
		return null;
	}
	// **-------------------------------------------------------------------------------

	public Shape makeNewShape(String s, AttributeMap attrMap)
	{
		Shape newShape;
		if ("Circle".equals(s))					newShape = new Circle();
		else if ("Rectangle".equals(s))			newShape = new Rectangle();
		else if ("Polygon".equals(s))			newShape = new Polygon();
		else return null;
		
		makeHandlers(newShape);
		newShape.setId( model.gensym("" + s.charAt(0)));
		newShape.setFill(drawLayer.getDefaultFill());
		newShape.setStroke(Color.BLUE);
		newShape.setStrokeWidth(5f);
		newShape.setManaged(false);
		setAttributes(newShape, attrMap);
		return newShape;
	}

	//-----------------------------------------------------------------------
	public Shape parseNode(AttributeMap attrMap)
	{
		Shape shape = makeNewShape(attrMap.get("type"), attrMap);
//		if (shape != null)
//			setAttributes(shape, attrMap);
		return shape;
	}
	
	private void setAttributes(Shape shape, AttributeMap map)
	{
//		if (verbose>0) System.out.println(map.toString());
		for (String k : map.keySet())
		{
			if (k.equals("stroke"))  k = "-fx-stroke";
			if (k.equals("strokeWidth"))  k = "-fx-stroke-weight";
			String val = map.get(k);
			if (k.equals("id"))			shape.setId(val);
			double d = StringUtil.toDouble(val);			// exception safe:  comes back NaN if val is not a number
			if (shape instanceof Rectangle)
			{
				Rectangle r = (Rectangle) shape;
				if (k.equals("x"))				r.setX(d);
				else if (k.equals("y"))			r.setY(d);
				else if (k.equals("width"))		r.setWidth(d);
				else if (k.equals("height"))	r.setHeight(d);
			}
			if (shape instanceof Circle)
			{
				Circle circ = (Circle) shape;
				if (k.equals("centerX"))		circ.setCenterX(d);
				else if (k.equals("centerY"))	circ.setCenterY(d);
				else if (k.equals("radius"))	circ.setRadius(d);
			}
			if (shape instanceof Polygon)
			{
				Polygon poly = (Polygon) shape;
				if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
			}
			if (shape instanceof Shape)
			try
			{
				Shape sh = shape;
				if (k.equals("fill") || k.equals("-fx-fill"))				
				{
					sh.setFill(Color.web(val));
//					String lastTwoChars = val.substring(val.length()-2);
//					int opac = Integer.parseInt(lastTwoChars, 16);
//					shape.setOpacity(opac / 255.);
				}
				else if (k.equals("-fx-stroke"))		sh.setStroke(Color.web(val));
				else if (k.equals("-fx-stroke-weight"))	sh.setStrokeWidth(d);
//				else if (k.equals("selected"))		shape.setSelected(val);
			}
			catch (Exception e) { System.err.println("Parse errors: " + k); }
		}	
	}
	
	private void parsePolygonPoints(Polygon poly, String string)
	{
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		for (String d : doubles)
			poly.getPoints().add(Double.parseDouble(d));
	}
	// **-------------------------------------------------------------------------------
	// MouseEvents and DragEvents
	public void makeHandlers(Shape s)
	{
		if (s == null) return;
		if (s instanceof Circle)			new CircleMouseHandler((Circle)s, drawLayer);
		if (s instanceof Rectangle)			new RectMouseHandler((Rectangle)s, drawLayer);
		if (s instanceof Polygon)			new PolygonMouseHandler((Polygon) s, drawLayer);

		//		s.addEventHandler(MouseEvent.MOUSE_ENTERED, new NodeMouseEnteredHandler());

		s.setOnDragEntered(e -> {	s.setEffect(Effects.sepia);	e.consume();	});
		s.setOnDragExited(e -> 	{	s.setEffect(null);				e.consume();	});
		s.setOnDragOver(e -> 	{	e.acceptTransferModes(TransferMode.ANY);	e.consume();		});
		s.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);	handleDrop(s, e);	e.consume();	});	

	}
	// **-------------------------------------------------------------------------------
	private void handleDrop(Shape s, DragEvent e)
	{	
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		Shape shape = (Shape) e.getTarget();
		if (db.hasString())
		{
			String q = db.getString();
			if (q.contains("-fx-"))
			{
				AttributeMap attrMap = new AttributeMap(q);
				setAttributes(shape, attrMap);
				Selection sel = drawLayer.getController().getSelectionManager();
				if (sel.isSelected(shape))
					sel.setAttributes(attrMap);
			}
				System.out.println(q);
		}
		
		if (db.hasFiles())
		{
			List<File> files = db.getFiles();
			if (files != null)
			{
//				controller.getUndoStack().push(ActionType.Add, " file");
				int offset = 0;
				for (File f : files)
				{
					offset += 20;
					System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f))
					{
						StringBuilder buff = new StringBuilder();
						FileUtil.readFileIntoBuffer(f, buff);
						String styl = buff.toString();
						shape.getStyleClass().add(styl);
						System.out.println("S: " + styl);
				
					}
				}
			}
		}

	}

	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// SHAPE MOUSE HANDLERS

	private class RectMouseHandler extends NodeMouseHandler
	{
		public RectMouseHandler(Rectangle r, Canvas d)
		{
			super(d);
			r.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			r.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			r.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			r.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}
		
		@Override
		protected void handleMousePressed(final MouseEvent event)
		{
			if (((Node)event.getTarget()).getParent() instanceof Group) return;
			super.handleMousePressed(event);
			startPoint = currentPoint;
			
			if (RectangleUtil.inCorner(event))
			{
				resizing = true;
				dragging = false;
				startPoint = RectangleUtil.oppositeCorner(event);
				undoStack.push(ActionType.Resize);
				System.out.println("Pressed: " + currentPoint + " is opposite "+ startPoint.toString());
			}
		}
	
		@Override
		protected void handleMouseDragged(final MouseEvent event)
		{
			if (((Node)event.getTarget()).getParent() instanceof Group) return;
		if (verbose>3)	System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);
	
			if (resizing)
			{
				System.out.println("startPoint: " + startPoint.toString());
				System.out.println("CurrentPoint: " + currentPoint.toString());
				double x,y,width, height;
				x = Math.min(startPoint.getX(), currentPoint.getX());
				y = Math.min(startPoint.getY(), currentPoint.getY());
				width = Math.abs(startPoint.getX() - currentPoint.getX());
				height = Math.abs(startPoint.getY() - currentPoint.getY());
				
				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height );
	
				if (event.getTarget() instanceof Rectangle)
				{
					Rectangle r = (Rectangle) event.getTarget();
					RectangleUtil.setRect(r, x, y, width, height);
				}
			}
		}
		
		@Override
		protected void handleMouseMoved(final MouseEvent event)
		{
			if (((Node)event.getTarget()).getParent() instanceof Group) return;
			super.handleMouseMoved(event);
			if (event.getTarget() instanceof Rectangle)
			{
				Rectangle r = (Rectangle) event.getTarget();
				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ?  Cursor.H_RESIZE : Cursor.HAND);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	
	protected class CircleMouseHandler extends NodeMouseHandler
	{
		public CircleMouseHandler(Circle c, Canvas d)
			{
				super(d);
				c.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
				c.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
				c.addEventHandler(MouseEvent.MOUSE_MOVED, this);
				c.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
			}
	
		@Override
		protected void handleMousePressed(final MouseEvent event)
		{
			if (((Node)event.getTarget()).getParent() instanceof Group) return;
			super.handleMousePressed(event);
			Circle c = (Circle) event.getTarget();
			if (onEdge(currentPoint, c))
			{
				resizing = true;
				dragging = false;
				undoStack.push(ActionType.Resize);
			}
		}
		
		boolean onEdge(Point2D pt, Circle c)
		{
			double d2 = (pt.getX() - c.getCenterX()) * (pt.getX() - c.getCenterX()) + 
					(pt.getY() - c.getCenterY()) * (pt.getY() - c.getCenterY());
			double dist = Math.sqrt(d2);
			return Math.abs(dist - c.getRadius()) < EPSILON;
		}
	
		@Override
		protected void handleMouseDragged(final MouseEvent event)
		{
			if (((Node)event.getTarget()).getParent() instanceof Group) return;
			super.handleMouseDragged(event);
			if (verbose>3)	
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
	
			if (resizing)
			{
				Circle c = (Circle) target;
				double dx = c.getCenterX() - currentPoint.getX();
				double dy = c.getCenterY() - currentPoint.getY();
	
				double newRadius = Math.sqrt(dx * dx + dy * dy);
				c.setRadius(newRadius);
			}
		}
	
		@Override
		protected void handleMouseMoved(final MouseEvent event)
		{
			super.handleMouseMoved(event);
			Circle c = (Circle) target;
			if (onEdge(currentPoint, c))
				c.setCursor(Cursor.H_RESIZE);
			else
				c.setCursor(Cursor.HAND);
		}
	}
	// **-------------------------------------------------------------------------------
	static public int onVertex(Point2D pt, Polygon p)
	{
		Object[] pts = p.getPoints().toArray();
		
		for ( int i = 0; i < pts.length; i += 2)
			if (Math.abs(pt.getX() - (double) pts[i]) < EPSILON)
				if (Math.abs(pt.getY() - (double) pts[i+1]) < EPSILON)
					return i;
		return -1;
	}

	// **-------------------------------------------------------------------------------
	protected class PolygonMouseHandler extends NodeMouseHandler
	{
		public PolygonMouseHandler(Polygon p, Canvas d)
		{
			super(d);
			p.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			p.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			p.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			p.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}
		@Override
		protected void handleMousePressed(final MouseEvent event)
		{
			super.handleMousePressed(event);
			if (event.getTarget() == drawLayer.getActiveShape())
			{
				drawLayer.setActiveShape(null);
				event.consume();
				return;
			}
			if (verbose>3) System.out.println("PolygonMousePressedHandler: " + event.getTarget());
			Polygon p = (Polygon) target;
			int idx = onVertex(currentPoint, p);
	//			System.out.println("" + idx);
			if (idx >= 0)
				activeIndex = idx;
			else dragging = true;
		}

		int activeIndex = -1;
	
		@Override
		protected void handleMouseDragged(final MouseEvent event)
		{
			super.handleMouseDragged(event);
			if (verbose>1) System.out.println("Index: " + activeIndex);
			Polygon p = (Polygon) target;				
			if (activeIndex>= 0)
			{
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex+1, currentPoint.getY());
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event)
		{
			super.handleMouseMoved(event);
			Polygon p = (Polygon) target;				
			if (onVertex(currentPoint, p) >= 0)
				p.setCursor(Cursor.H_RESIZE);
			else
				p.setCursor(Cursor.HAND);
		}
	}
	//-----------------------------------------------------------------------------------
	public class NodeMouseHandler implements EventHandler<MouseEvent>
	{
		public NodeMouseHandler(Canvas d)
		{
			 drawLayer = d;
		}
		
		@Override public void handle(MouseEvent e)
		{
			target = e.getTarget();
//			scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
			currentPoint = new Point2D(e.getX(), e.getY());
			EventType<?> type = e.getEventType();
			
			if (type == MouseEvent.MOUSE_MOVED)  			handleMouseMoved(e);
			else if  (type == MouseEvent.MOUSE_PRESSED)  	handleMousePressed(e);
			else if  (type == MouseEvent.MOUSE_DRAGGED)  	handleMouseDragged(e);
			else if  (type == MouseEvent.MOUSE_RELEASED)  	handleMouseReleased(e);
		}
		//**-------------------------------------------------------------------------------
		protected Point2D startPoint, currentPoint, prevPoint, offset;
		private Canvas drawLayer;
		private Pane getPane() { return drawLayer.getPane(); }
		private Controller getController()		{			return drawLayer.getController();		}
		protected int verbose = 0;
		protected boolean dragging = false;
		protected boolean resizing = false;
		protected EventTarget target;
		private ContextMenu menu;
		
		//**-------------------------------------------------------------------------------
		protected void handleMouseMoved(MouseEvent event)		{	event.consume();		}

		protected void handleMousePressed(MouseEvent event)
		{
			if (verbose>3)	
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			
			resizing = false;
			prevPoint = currentPoint;
			boolean altDown = event.isAltDown();
			boolean leftClick = event.isPrimaryButtonDown();
			boolean rightClick = event.isSecondaryButtonDown();
			if (altDown)
				getController().getNodeFactory().cloneSelection();
			// do nothing for a right-click
//			if (event.isSecondaryButtonDown())		return;// TODO -- popup up Node menu
			if (event.isPopupTrigger() || rightClick)	
			{
				if (menu == null)
					menu = buildContextMenu();
				menu.show(getPane(), event.getScreenX(), event.getScreenY());
	            return;			// TODO -- popup up Node menu
			}
			Selection sel =  drawLayer.getSelectionMgr();
			Node node = (Node) target;
			boolean wasSelected = sel.isSelected(node);
			if (event.isControlDown())
				sel.select(node, !wasSelected);
			else if ((event.isShiftDown()))
				sel.select(node);
			else	sel.selectX(node);
			
			if (sel.count() > 0)
			{
				dragging = true;
				drawLayer.getController().getUndoStack().push(ActionType.Move);
				startPoint = currentPoint;
			}
			event.consume();
		}
		
		ContextMenu buildContextMenu()
		{
			menu = new ContextMenu();
			MenuItem dup = new MenuItem("Duplicate");			dup.setOnAction(a -> 	{ 	getController().duplicateSelection();    });
			MenuItem del = new MenuItem("Delete");				del.setOnAction(a -> 	{ 	getController().deleteSelection();    });
			MenuItem toFront = new MenuItem("Bring To Front");	toFront.setOnAction(a ->{ 	getController().toFront();    });
			MenuItem toBack = new MenuItem("Send To Back");		toBack.setOnAction(a -> {   getController().toBack();    });
			MenuItem group = new MenuItem("Group");				group.setOnAction(a -> 	{   getController().group();    });
			MenuItem ungroup = new MenuItem("Ungroup");			ungroup.setOnAction(a ->{	getController().ungroup();    });
			menu.getItems().addAll(toFront, toBack, group, ungroup);
			return menu;
		}
		
		protected void handleMouseDragged(MouseEvent event)
		{
			if (verbose>3)	
				System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())			return;
			if (dragging)
			{
				double dx, dy;
				dx = prevPoint.getX() - currentPoint.getX();
				dy = prevPoint.getY() - currentPoint.getY();
				
//				System.out.println("Delta: " + dx + ", " + dy);
				drawLayer.getSelectionMgr().translate(dx, dy);
				prevPoint = currentPoint;
			}
			event.consume();
		}
			
		protected void handleMouseReleased(MouseEvent event)
		{
			startPoint = null;
			resizing = dragging = false;
			drawLayer.getPane().requestFocus();	 // needed for the key event handler to receive events
			event.consume();
			drawLayer.getController().refreshZoomPane();
		}
	}

}
