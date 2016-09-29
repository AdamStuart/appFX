package diagrams.draw;

import java.io.File;
import java.util.List;
import java.util.Set;

import diagrams.draw.Action.ActionType;
import diagrams.draw.App.Tool;
import gui.Effects;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.util.Pair;
import model.AttributeMap;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;
import util.StringUtil;

public class ShapeFactory {
	private Model model;
	private Pasteboard drawLayer;
	private UndoStack undoStack;

	/*
	 * The ShapeFactory is responsible for create new nodes that are shapes.
	 * Largely this is about defining the mouse event and drop handlers for the
	 * shapes.
	 * 
	 * makeMarquee creates the selection rectangle that is added on canvas
	 * drags.
	 */
	public ShapeFactory(Pasteboard l, UndoStack u) {
		drawLayer = l;
		undoStack = u;
		model = drawLayer.getController().getDrawModel();
	}

	// -----------------------------------------------------------------------
	public Rectangle makeMarquee() {
		AttributeMap attrMap = new AttributeMap();
		attrMap.putAll("id", "Marquee", "-fx-stroke", "Green", "-fx-fill", "TRANSPARENT", "-fx-stroke-width", "1");
		Rectangle marquee = (Rectangle) makeNewShape("Rectangle", attrMap, false);
		marquee.getStrokeDashArray().addAll(3.0, 7.0, 3.0, 7.0);
		// marquee.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		// marquee.setOpacity(0.5);
		return marquee;
	}

	public static boolean isMarquee(Node node) {
		return node != null && "Marquee".equals(node.getId());
	}

	// -----------------------------------------------------------------------

	public Shape makeNewNode(Tool type, AttributeMap attrMap) {
		assert(type != null);
		return (type.isShape()) ? makeNewShape(type.toString(), attrMap, true) : null;
	}

	// **-------------------------------------------------------------------------------
	public Shape makeNewShape(Tool type, AttributeMap attrMap) {
		Shape sh = makeNewShape(type.toString(), attrMap, true);
		sh.setId(attrMap.get("GraphId"));
		if (sh instanceof  Circle)
		{
			double rad = attrMap.getDouble("radius");
			if (Double.isNaN(rad))
				rad = attrMap.getDouble("Width") / 2;
				((Circle) sh).setRadius(rad);
		}
		return sh;
	}

	public Shape makeNewShape(String s, AttributeMap attrMap, boolean addHandlers) {
		Shape newShape;
		if ("Circle".equals(s))					newShape = new Circle();
		else if ("Rectangle".equals(s))			newShape = new Rectangle();
		else if ("Shape1".equals(s))			newShape = new Shape1();
		else if ("Shape2".equals(s))			newShape = new Shape2();
		else if ("Polygon".equals(s))			newShape = new Polygon();
		else if ("Polyline".equals(s))			newShape = new Polyline();
		else if ("Line".equals(s))	{ newShape = new Line(); 
									attrMap.put("stroke-width", "3");  }		// a hack to make it easier to select
		else	return null;

		makeHandlers(newShape);
		newShape.setId(model.gensym("" + s.charAt(0)));
		newShape.setFill(drawLayer.getDefaultFill());
		newShape.setStroke(Color.BLUE);

		newShape.setStrokeWidth(1f);
		newShape.setManaged(false);
		setAttributes(newShape, attrMap);
		newShape.getProperties().putAll(attrMap);
		return newShape;
	}

	public Shape nodeFromGPML(String gpmlStr,  AttributeMap attrMap, boolean addHandlers) {
		String txt = gpmlStr.trim();
		if (txt.startsWith("<DataNode "))
		{
			String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
			String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
			attrMap.addGPML(graphics);
			attrMap.addGPML(xref);
		}
		String shapeType = attrMap.get("ShapeType");
		Shape newShape = makeNewShape(shapeType, attrMap, addHandlers); 
		return newShape;
	}

	public Edge edgeFromGPML(String gpmlStr, AttributeMap attrMap,  boolean addHandlers) {
		String txt = gpmlStr.trim();
		if (txt.startsWith("<Interaction>"))
		{
			String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
			String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
			attrMap.addGPMLEdgeInfo(graphics);
			attrMap.addGPML(xref);
			return new Edge(attrMap, model);
		}
		return null;
	}
// **-------------------------------------------------------------------------------
// this doesn't work because it can't pass the text back to be added to the drawLayer
	//	public Shape makeLabeledShape(Tool tool, AttributeMap attrMap, String s) {
//		Shape newShape = makeNewShape(tool, attrMap);
//		final Label text = createLabel(s);
//    	NodeCenter ctr = new NodeCenter(newShape);
//    	text.layoutXProperty().bind(ctr.centerXProperty().subtract(text.widthProperty().divide(2.)));	// width / 2
//    	text.layoutYProperty().bind(ctr.centerYProperty().subtract(text.heightProperty().divide(2.)));
//		return newShape;
//}

	public StackPane makeLabeledShapePane(Tool tool, AttributeMap attrMap, String s) {
		Shape newShape = makeNewShape(tool, attrMap);
		StackPane stack = new StackPane();
		final Label text = createLabel(s);
		text.setTranslateX(attrMap.getDouble("centerX"));
		text.setTranslateY(attrMap.getDouble("centerY"));
		StackPane.setAlignment(newShape, Pos.CENTER);
		StackPane.setAlignment(text, Pos.CENTER);
		stack.getChildren().addAll(newShape, text);
		makeNodeMouseHandler(stack);

		return stack;
	}
	public Group makeLabeledShapeGroup(Tool tool, AttributeMap attrMap, String s) {
		Shape newShape = makeNewShape(tool, attrMap);
		Group group = new Group();
		final Label text = createLabel(s);
		text.setTranslateX(attrMap.getDouble("centerX"));
		text.setTranslateY(attrMap.getDouble("centerY"));
		StackPane.setAlignment(newShape, Pos.CENTER);
		StackPane.setAlignment(text, Pos.CENTER);
		group.getChildren().addAll(newShape, text);
		makeNodeMouseHandler(group);
		return group;
	}
 
	
	public Label createLabel(String s) {
		final Label text = new Label(s);
		text.setFont(new Font(18));
//		text.setBoundsType(TextBoundsType.VISUAL);
		text.setMouseTransparent(true);
		return text;
	}
	// -----------------------------------------------------------------------
	boolean UseGPML = true;
	public Shape parseNode(AttributeMap attrMap) {
		if (UseGPML) 	
			return nodeFromGPML(attrMap.get("type"), attrMap, false);
		return makeNewShape(attrMap.get("type"), attrMap, false);
	}

	private void setAttributes(Shape shape, AttributeMap map) {
		// if (verbose>0) System.out.println(map.toString());
		for (String k : map.keySet()) 
		{
			String val = map.get(k);
			k = k.toLowerCase();
			if (k.equals("stroke"))				k = "-fx-stroke";
			if (k.equals("strokeWidth"))		k = "-fx-stroke-weight";
			if (k.equals("id"))					shape.setId(val);
			double d = StringUtil.toDouble(val); // exception safe: comes back
													// NaN if val is not a
													// number
			if (shape instanceof Rectangle) {
				Rectangle r = (Rectangle) shape;
				if (k.equals("centerx"))		r.setX(d);
				else if (k.equals("centery"))	r.setY(d);
				else if (k.equals("x"))			r.setX(d);
				else if (k.equals("y"))			r.setY(d);
				else if (k.equals("width"))		r.setWidth(d);
				else if (k.equals("height"))	r.setHeight(d);
			}
			if (shape instanceof Circle) {
				Circle circ = (Circle) shape;
				if (k.equals("centerx"))		circ.setCenterX(d);
				else if (k.equals("centery"))	circ.setCenterY(d);
				else if (k.equals("x"))			circ.setCenterX(d);
				else if (k.equals("y"))			circ.setCenterY(d);
				else if (k.equals("width"))		circ.setRadius(d/2);
				else if (k.equals("radius"))	circ.setRadius(d);
			}
			if (shape instanceof Polygon) {
				Polygon poly = (Polygon) shape;
				if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
			}
			if (shape instanceof Polyline) {
				Polyline poly = (Polyline) shape;
				if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
			}
			if (shape instanceof Line) {
				Line line = (Line) shape;
				if (k.equals("points"))			parseLinePoints(line, map.get(k));
				if (k.equals("stroke-width"))	line.setStrokeWidth(d);
			}
			if (shape instanceof Shape)
				try {
					Shape sh = shape;
					if (k.equals("fill") || k.equals("-fx-fill")) {
						sh.setFill(Color.web(val));
						// String lastTwoChars = val.substring(val.length()-2);
						// int opac = Integer.parseInt(lastTwoChars, 16);
						// shape.setOpacity(opac / 255.);
					} else if (k.equals("-fx-stroke"))			sh.setStroke(Color.web(val));
					else if (k.equals("-fx-stroke-weight"))		sh.setStrokeWidth(d);
					// else if (k.equals("selected")) shape.setSelected(val);
				} catch (Exception e) {
					System.err.println("Parse errors: " + k);
				}
		}
	}

	private void parsePolygonPoints(Polygon poly, String string) {	parsePoints(poly.getPoints(), string);	}

	private void parsePolylinePoints(Polyline poly, String string) {parsePoints(poly.getPoints(), string);	}

	private void parsePoints(ObservableList<Double> points, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		for (String d : doubles)
			points.add(Double.parseDouble(d));
	}

	private void parseLinePoints(Line line, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		assert(doubles.length == 4);
		line.setStartX(Double.parseDouble(doubles[0]));
		line.setStartY(Double.parseDouble(doubles[1]));
		line.setEndX(Double.parseDouble(doubles[2]));
		line.setEndY(Double.parseDouble(doubles[3]));
	}

	// **-------------------------------------------------------------------------------
	// MouseEvents and DragEvents
	public void makeHandlers(Shape s) {
		if (s == null)
			return;
		if (s instanceof Circle)		new CircleMouseHandler((Circle) s, drawLayer);
		if (s instanceof Rectangle)		new RectMouseHandler((Rectangle) s, drawLayer);
		if (s instanceof Polygon)		new PolygonMouseHandler((Polygon) s, drawLayer);
		if (s instanceof Polyline)		new PolylineMouseHandler((Polyline) s, drawLayer);
		if (s instanceof Line)			new LineMouseHandler((Line) s, drawLayer);
		if (s instanceof Shape1)		new ShapeMouseHandler((Shape1) s, drawLayer);
		if (s instanceof Shape2)		new ShapeMouseHandler((Shape2) s, drawLayer);

		s.setOnDragEntered(e -> {	s.setEffect(Effects.sepia);			e.consume();		});
		s.setOnDragExited(e -> 	{	s.setEffect(null);					e.consume();		});
		s.setOnDragOver(e -> 	{	e.acceptTransferModes(TransferMode.ANY); 	e.consume();	});
		s.setOnDragDropped(e -> {
			e.acceptTransferModes(TransferMode.ANY);
			handleDrop(s, e);
			e.consume();
		});

	}

	// **-------------------------------------------------------------------------------
	private void handleDrop(Shape s, DragEvent e) {
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		Shape shape = (Shape) e.getTarget();
		if (db.hasString()) {
			String q = db.getString();
			if (q.contains("-fx-")) {
				AttributeMap attrMap = new AttributeMap(q);
				setAttributes(shape, attrMap);
				Selection sel = drawLayer.getController().getSelectionManager();
				if (sel.isSelected(shape))
					sel.setAttributes(attrMap);
			}
			System.out.println(q);
		}

		if (db.hasFiles()) {
			List<File> files = db.getFiles();
			if (files != null) {
				// controller.getUndoStack().push(ActionType.Add, " file");
//				int offset = 0;
				for (File f : files) {
//					offset += 20;
					System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f)) {
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

	private class RectMouseHandler extends NodeMouseHandler {
		public RectMouseHandler(Rectangle r, Pasteboard d) {
			super(d);
			r.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			r.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			r.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			r.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
			startPoint = currentPoint;

			if (RectangleUtil.inCorner(event)) {
				resizing = true;
				dragging = false;
				startPoint = RectangleUtil.oppositeCorner(event);
				undoStack.push(ActionType.Resize);
				System.out.println("Pressed: " + currentPoint + " is opposite " + startPoint.toString());
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

			if (resizing) {
//				System.out.println("startPoint: " + startPoint.toString());
//				System.out.println("CurrentPoint: " + currentPoint.toString());
				double x, y, width, height;
				x = Math.min(startPoint.getX(), currentPoint.getX());
				y = Math.min(startPoint.getY(), currentPoint.getY());
				width = Math.abs(startPoint.getX() - currentPoint.getX());
				height = Math.abs(startPoint.getY() - currentPoint.getY());

//				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);

				if (event.getTarget() instanceof Rectangle) {
					Rectangle r = (Rectangle) event.getTarget();
					RectangleUtil.setRect(r, x, y, width, height);
				}
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
			if (event.getTarget() instanceof Rectangle) {
				Rectangle r = (Rectangle) event.getTarget();
				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
			}
		}
	}
	// **-------------------------------------------------------------------------------

	private class ShapeMouseHandler extends NodeMouseHandler {
		public ShapeMouseHandler(Shape s, Pasteboard d) {
			super(d);
			s.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			s.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			s.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			s.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

//			if (resizing) {
////				System.out.println("startPoint: " + startPoint.toString());
////				System.out.println("CurrentPoint: " + currentPoint.toString());
//				double x, y, width, height;
//				x = Math.min(startPoint.getX(), currentPoint.getX());
//				y = Math.min(startPoint.getY(), currentPoint.getY());
//				width = Math.abs(startPoint.getX() - currentPoint.getX());
//				height = Math.abs(startPoint.getY() - currentPoint.getY());
//
////				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);
//
//				if (event.getTarget() instanceof Rectangle) {
//					Rectangle r = (Rectangle) event.getTarget();
//					RectangleUtil.setRect(r, x, y, width, height);
//				}
//			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
//			if (event.getTarget() instanceof Rectangle) {
//				Rectangle r = (Rectangle) event.getTarget();
//				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
//			}
		}
	}
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------

	protected class CircleMouseHandler extends NodeMouseHandler {
		public CircleMouseHandler(Circle c, Pasteboard d) {
			super(d);
			c.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			c.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			c.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			c.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() == Tool.Line) 
				{
//				create the line from the center
//				return;
				
				}
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)			return;
			super.handleMousePressed(event);
			Circle c = (Circle) event.getTarget();
			if (LineUtil.onEdge(currentPoint, c)) {
				resizing = true;
				dragging = false;
				undoStack.push(ActionType.Resize);
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)		return;
			super.handleMouseDragged(event);
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());

			if (resizing) {
				Circle c = (Circle) target;
				double dx = c.getCenterX() - currentPoint.getX();
				double dy = c.getCenterY() - currentPoint.getY();

				double newRadius = Math.sqrt(dx * dx + dy * dy);
				c.setRadius(newRadius);
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Circle c = (Circle) target;
			if (LineUtil.onEdge(currentPoint, c))	c.setCursor(Cursor.H_RESIZE);
			else							c.setCursor(Cursor.HAND);
		}
	}


	// **-------------------------------------------------------------------------------
	protected class PolygonMouseHandler extends NodeMouseHandler {
		public PolygonMouseHandler(Polygon p, Pasteboard d) {
			super(d);
			p.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			p.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			p.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			p.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == drawLayer.getActiveShape()) {
				drawLayer.setActiveShape(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("PolygonMousePressedHandler: " + event.getTarget());
			Polygon p = (Polygon) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			// System.out.println("" + idx);
			if (idx >= 0)	activeIndex = idx;
			else			dragging = true;
		}

		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)
				System.out.println("Index: " + activeIndex);
			Polygon p = (Polygon) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Polygon p = (Polygon) target;
			if (LineUtil.onVertex(currentPoint, p) >= 0)	p.setCursor(Cursor.H_RESIZE);
			else	p.setCursor(Cursor.HAND);
		}
	}

	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	protected class PolylineMouseHandler extends NodeMouseHandler {
		public PolylineMouseHandler(Polyline p, Pasteboard d) {
			super(d);
			p.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			p.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			p.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			p.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			Polyline p = (Polyline) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			if (event.getTarget() == drawLayer.getActiveShape() && idx == 0) {
				drawLayer.setActiveShape(null);
				activeIndex = p.getPoints().size();
				p.getPoints().addAll(currentPoint.getX(), currentPoint.getY());
				event.consume();
				drawLayer.removeDragLine();
				drawLayer.resetTool();
				return;
			}
			if (verbose > 3)
				System.out.println("PolylineMousePressedHandler: " + event.getTarget());
			// System.out.println("" + idx);
			if (idx >= 0)		activeIndex = idx;
			else				dragging = true;

			p.getPoints().set(activeIndex, currentPoint.getX());
			p.getPoints().set(activeIndex+1, currentPoint.getY());
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)				System.out.println("Index: " + activeIndex);
			Polyline p = (Polyline) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseX != null) 	mouseX.set(event.getX());
//			if (mouseY != null) 	mouseY.set(event.getY());
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
//			drawLayer.setLastClick(event.getX(), event.getY());
			Polyline p = (Polyline) target;
			if (LineUtil.onVertex(currentPoint, p.getPoints()) >= 0)			p.setCursor(Cursor.H_RESIZE);
			else p.setCursor(Cursor.HAND);
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseY != null) 	
		}
		@Override
		protected void handleMouseReleased(final MouseEvent event) {
			
//			drawLayer.getPane().getChildren().remove(dragLine);
//			dragLine = null;
		}

	}
	// **-------------------------------------------------------------------------------
	protected class LineMouseHandler extends NodeMouseHandler {
		public LineMouseHandler(Line p, Pasteboard d) {
			super(d);
			p.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			p.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			p.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			p.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == drawLayer.getActiveShape()) {
				drawLayer.setActiveShape(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("LineMousePressedHandler: " + event.getTarget());
			Line p = (Line) target;
			activeIndex = LineUtil.onEndpoint(currentPoint, p);
			// System.out.println("" + idx);
//			if (idx >= 0)		activeIndex = idx;
//			else				dragging = true;
			
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 3)				System.out.println("Index: " + activeIndex);
			Line p = (Line) target;
			if (activeIndex == 0) {
				p.setStartX(currentPoint.getX());
				p.setStartY(currentPoint.getY());
			}
			else if (activeIndex == 1) {
				p.setEndX(currentPoint.getX());
				p.setEndY(currentPoint.getY());
			}
			else super.handleMouseDragged(event);
		}

//		@Override
//		protected void handleMouseMoved(final MouseEvent event) {
//			super.handleMouseMoved(event);
//			drawLayer.setLastClick(event.getX(), event.getY());
//			Line p = (Line) target;
//			if (onEndpoint(currentPoint, p) >= 0)	p.setCursor(Cursor.H_RESIZE);
//			else p.setCursor(Cursor.HAND);
//			if (mouseX != null) 	mouseX.set(event.getX());
//			if (mouseY != null) 	mouseY.set(event.getY());
//			p.setEndX(event.getX());
//			p.setEndY(event.getY());
//		}
	}

	// -----------------------------------------------------------------------------------
	public void makeNodeMouseHandler(Node n) {
		NodeMouseHandler h = new NodeMouseHandler(drawLayer);
		n.addEventHandler(MouseEvent.MOUSE_PRESSED, h);
		n.addEventHandler(MouseEvent.MOUSE_DRAGGED, h);
		n.addEventHandler(MouseEvent.MOUSE_MOVED, h);
		n.addEventHandler(MouseEvent.MOUSE_RELEASED, h);
	}
	// -----------------------------------------------------------------------------------
		private class NodeMouseHandler implements EventHandler<MouseEvent> {
			public NodeMouseHandler(Pasteboard d) {
				drawLayer = d;
			}

		@Override
		public void handle(MouseEvent e) {
			target = e.getTarget();
			// scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
			currentPoint = new Point2D(e.getX(), e.getY());
			if (verbose > 4)
				System.out.println((int) currentPoint.getX() + ", " + (int) currentPoint.getY());
			EventType<?> type = e.getEventType();

			if (type == MouseEvent.MOUSE_MOVED)				handleMouseMoved(e);
			else if (type == MouseEvent.MOUSE_PRESSED)		handleMousePressed(e);
			else if (type == MouseEvent.MOUSE_DRAGGED)		handleMouseDragged(e);
			else if (type == MouseEvent.MOUSE_RELEASED)		handleMouseReleased(e);
			prevPoint = currentPoint;
		}

		// **-------------------------------------------------------------------------------
		protected Point2D startPoint, currentPoint, prevPoint, offset;
		private Pasteboard drawLayer;
		private Pane getPane() 				{	return drawLayer.getPane();		}
		private Controller getController() 	{	return drawLayer.getController();		}

		protected int verbose = 3;
		protected boolean dragging = false;
		protected boolean resizing = false;
		protected EventTarget target;
		Line dragLine = new Line();
		private ContextMenu menu;

		// **-------------------------------------------------------------------------------
		protected void handleMouseMoved(MouseEvent event) {
			event.consume();
		}

		protected void handleMousePressed(MouseEvent event) {
			if (verbose > 3)
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			if (drawLayer.getTool() == Tool.Arrow)
			{
				resizing = false;
				prevPoint = currentPoint;
				boolean altDown = event.isAltDown();
				// boolean leftClick = event.isPrimaryButtonDown();
				boolean rightClick = event.isSecondaryButtonDown();
				if (altDown)
					getController().getNodeFactory().cloneSelection();
				// do nothing for a right-click
				// if (event.isSecondaryButtonDown()) return;// TODO -- popup up
				// Node menu
				if (event.isPopupTrigger() || rightClick) {
					if (menu == null)
						menu = buildContextMenu();
					if (menu != null)
						menu.show(getPane(), event.getScreenX(), event.getScreenY());
					return;
				}
				Selection sel = drawLayer.getSelectionMgr();
				Node node = (Node) target;
				boolean wasSelected = sel.isSelected(node);
				if (event.isControlDown())			sel.select(node, !wasSelected);
				else if ((event.isShiftDown()))		sel.select(node);
				else if (!wasSelected)				sel.selectX(node);
	
				if (sel.count() > 0) {
					dragging = true;
					drawLayer.getController().getUndoStack().push(ActionType.Move);
					startPoint = currentPoint;
				}
				event.consume();
			}
	}
		//--------------------------------------------------------------------------------------------
		private ContextMenu buildContextMenu() {
			menu = new ContextMenu();
			Controller c = getController();
			MenuItem dup = 		makeItem("Duplicate", a -> 		{	c.duplicateSelection();	});
			MenuItem del = 		makeItem("Delete", a -> 		{	c.deleteSelection();	});
			MenuItem toFront = 	makeItem("Bring To Front", a -> {	c.toFront();	});
			MenuItem toBack = 	makeItem("Send To Back", a -> 	{	c.toBack();	});
			MenuItem group = 	makeItem("Group", a -> 			{	c.group();	});
			MenuItem ungroup = 	makeItem("Ungroup", a -> 		{	c.ungroup();	});
			menu.getItems().addAll(toFront, toBack, group, ungroup, dup, del);
			return menu;
		}
		
		private MenuItem makeItem(String name, EventHandler<ActionEvent> foo)
		{
			MenuItem item = new MenuItem(name);	
			item.setOnAction(foo);
			return item;
		}
		//--------------------------------------------------------------------------------------------

		protected void handleMouseDragged(MouseEvent event) {
		if (verbose > 3)
				System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())
				return;
			if (drawLayer.getTool() == Tool.Arrow)
			{
				if (dragging) {
					double dx, dy;
					dx = prevPoint.getX() - currentPoint.getX();
					dy = prevPoint.getY() - currentPoint.getY();
	
					// System.out.println("Delta: " + dx + ", " + dy);
					drawLayer.getSelectionMgr().translate(dx, dy);
					prevPoint = currentPoint;
				}
			}
			event.consume();
		}

		protected void handleMouseReleased(MouseEvent event) {
			startPoint = null;
//			dragLine = null;
			resizing = dragging = false;
			drawLayer.getPane().requestFocus(); // needed for the key event
												// handler to receive events
			event.consume();
			drawLayer.getController().refreshZoomPane();
		}
	}

}
