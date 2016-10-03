package diagrams.draw;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.draw.Action.ActionType;
import diagrams.draw.App.Tool;
import diagrams.draw.gpml.GPML;
import gui.Borders;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.web.WebView;
import model.AttributeMap;
import model.CSVTableData;
import util.FileUtil;
import util.MacUtil;
import util.RectangleUtil;
import util.StringUtil;

public class NodeFactory
{
/*
 *  NodeFactory has responsibility of creating nodes of all types, whether by command, mouse action, drops,
 *  files being read in, or undo.  The DrawLayer is the "parent" of the NodeFactory, but the term is
 *  not the same as node.parent().  
  */
	int verbose = 0;
	
	public NodeFactory(Pasteboard layer)
	{
		drawLayer = layer;
		undoStack = drawLayer.getController().getUndoStack();
		shapeFactory = new ShapeFactory(drawLayer, undoStack);
	}
	// **-------------------------------------------------------------------------------
	private Pasteboard drawLayer;
	private UndoStack undoStack;
	private ShapeFactory shapeFactory;		// refactoring shapes to a new file, because they all have different mouse handlers
	//@formatter:off
	private Model getModel()				{ 	return drawLayer.getController().getDrawModel();	}
	private Controller getController()		{ 	return drawLayer.getController();	}
	private String gensym(String s)			{	return getModel().gensym(s);	}
	public ShapeFactory getShapeFactory()	{ 	return shapeFactory; }
	boolean showId = true;

	
	//@formatter:on
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert an org.w3c.dom.Node  a local node.  
	 * 
	 */
	public Node parseGPML(org.w3c.dom.Node datanode) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = datanode.getChildNodes();
		attrMap.add(datanode.getAttributes());
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node child = elems.item(i);
			String name = child.getNodeName();
			if (name.equals("#text")) continue;
			attrMap.add(child.getAttributes());
//			System.out.println(name);
		}
		String type = attrMap.get("Type");
		if (type == null)
			type = attrMap.get("ShapeType");
		Tool tool = Tool.lookup(type);
		if (tool == null) return null;
		if (tool.isShape())
			return shapeFactory.makeNewShape(tool, attrMap);
		if (Tool.isSVG(type))
			return makeNewSVGPane(attrMap);
		return makeNewNode(tool, attrMap);
	}
	
	private Node makeNewSVGPane(AttributeMap attrMap) {
		return null;
	}
	
	public Label parseGPMLLabel(org.w3c.dom.Node labelNode) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = labelNode.getChildNodes();
		attrMap.add(labelNode.getAttributes());
		String txt = attrMap.get("TextLabel");
		if (txt == null) txt = "Undefined";
		Label label = new Label(txt);
//		label.setManaged(false);
		String name = "";
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node child = elems.item(i);
			name = child.getNodeName();
			if (name != null && name.equals("TextLabel")) 
				label.setText(child.getNodeValue());
			if (name != null && name.equals("Attribute")) 
			{
				NamedNodeMap attrs = child.getAttributes();
				String key = "", val = "";
				for (int j=0; j<attrs.getLength(); j++)
				{
					org.w3c.dom.Node grandchild = attrs.item(j);
					String grandname = grandchild.getNodeName();
					{
						if ("Key".equals(grandname))	key = grandchild.getNodeValue();
						if ("Value".equals(grandname))	val = grandchild.getNodeValue();
					}
				}
				if (StringUtil.hasText(key) && StringUtil.hasText(val))
				{
					if (key.startsWith("org.pathvisio."))
						key = key.substring(14);
					label.setText(key + ":\n" + val);
					label.setTextFill(Color.CHOCOLATE);
					
				}
			}
			if (name != null && name.equals("Graphics")) 
				applyGraphicsNode(label, child);
		}
		return label;
	}

	private void applyGraphicsNode(Label label, org.w3c.dom.Node child) {
		NamedNodeMap attrs = child.getAttributes();
		String name = "";
		for (int i=0; i<attrs.getLength(); i++)
		{
			org.w3c.dom.Node item = attrs.item(i);
			String val = item.getNodeValue();
			double d = StringUtil.toDouble(val);
			name = item.getNodeName();
			
			if ("CenterX".equals(name)) 		 {	label.setLayoutX(d);}
			else if ("CenterY".equals(name)) 	 {	label.setLayoutY(d);}
			else if ("Width".equals(name)) 		 {	label.maxWidth(d); label.prefWidth(d);}
			else if ("Height".equals(name)) 	{	label.maxHeight(d); label.prefHeight(d);}
			else if ("ZOrder".equals(name)) {}
//			else if ("Color".equals(name)) {	label.setBorder(Borders.coloredBorder(val));}
			else if ("Color".equals(name)) {	label.setTextFill(Color.web(val));	}
			else if ("FillColor".equals(name)) 	
			{	label.setBackground(new Background(
					new BackgroundFill(Paint.valueOf(val), null, null)));
			}
			else if ("FontSize".equals(name)) {}
			else if ("FontWeight".equals(name)) {}
			else if ("Valign".equals(name)) {}
			else if ("ShapeType".equals(name)) 	
			{	if ("RoundedRectangle".equals(val)) {}		}
		}
		double w = StringUtil.toDouble(attrs.getNamedItem("Width").getNodeValue());
		double h = StringUtil.toDouble(attrs.getNamedItem("Height").getNodeValue());
//		label.getWidth();
//		double h = label.getWidth();
		label.setLayoutX(label.getLayoutX() - w / 2.);
		label.setLayoutY(label.getLayoutY() - h / 2.);
	}
		
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert a string representation into a node.  
	 * 
	 * param useCache:  determines whether to look for the node in the resource map.
	 * 					Shapes are generally recreated and not stored in the cache.
	 */
	public Node parseGPML(String gpml, boolean useCache )
	{
		AttributeMap attrMap = new AttributeMap();
		attrMap.addDataNodeGPML(gpml);
		String type = attrMap.get("ShapeType");
		Tool tool = Tool.fromString(type);
		if (tool.isShape())		return shapeFactory.parseShape(attrMap);
		if (tool.isControl())	return makeNewNode(tool, attrMap);
		return null;
		
	}
	@Deprecated
	public Node parseNode(String s, boolean useCache)
	{
		int attributeStart = s.indexOf('[');
		if (attributeStart < 0) return null;
		char firstChar = s.charAt(0);
		int start = Character.isDigit(firstChar) ? 2 : 0;
		String type = s.substring(start, attributeStart).trim();
		String attributes = s.substring(attributeStart).trim();
		if ("Text".equals(type) && attributes.startsWith("[text=\"\"")) return null;  // don't save unused labels
		
		
		AttributeMap attrMap = new AttributeMap(attributes);
		attrMap.put("type", type);
		String id = attrMap.getId();
		if ("null".equals(id))
			id = attrMap.get("text");
		if (useCache)
		{
			Node cached = getModel().getResource(id);
			if (cached != null)
			{
				setAttributes(cached, attrMap);
				return cached;
			}
		}

//		System.out.println("Everything should be cached!!");
		Tool tool = Tool.fromString(type);
		if (tool.isShape())		return shapeFactory.parseShape(attrMap);
		if (tool.isControl())	return makeNewNode(tool, attrMap);
		return null;
	}

	public void setAttributes(Node shape, AttributeMap map)
	{
		if (verbose>0) System.out.println(map.toString());
		for (String k : map.keySet())
		{
			String val = map.get(k);
			if (k.equals("GraphId"))			shape.setId(val);
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
			if (shape instanceof Polyline)
			{
				Polyline poly = (Polyline) shape;
				if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
			}
			if (shape instanceof Line)
			{
				Line line = (Line) shape;
				if (k.equals("startX"))			line.setStartX(d);
				else if (k.equals("startY"))	line.setStartY(d);
				else if (k.equals("endX"))		line.setEndX(d);
				else if (k.equals("endY"))		line.setEndY(d);
			}
			if (shape instanceof StackPane)
			{
				StackPane r = (StackPane) shape;
				if (k.equals("x"))				r.setLayoutX(d);
				else if (k.equals("y"))			r.setLayoutY(d);
				else if (k.equals("width"))		{ r.setMinWidth(d); r.setMaxWidth(d); r.prefWidth(d); }
				else if (k.equals("height"))	{ r.setMinHeight(d); r.setMaxHeight(d); r.prefHeight(d); }
				else if (k.equals("rotate"))	{ r.setRotate(d); }
				else if (k.equals("fill"))				
				{
					Background b = new Background(new BackgroundFill(Color.web(val), CornerRadii.EMPTY, Insets.EMPTY));
					r.setBackground(b);
				}
			}
			if (shape instanceof Shape)
			try
			{
				Shape sh = (Shape) shape;
				if (k.equals("fill") || k.equals("-fx-fill"))				
				{
					sh.setFill(Color.web(val));
					String lastTwoChars = val.substring(val.length()-2);
					int opac = Integer.parseInt(lastTwoChars, 16);
					shape.setOpacity(opac / 255.);
				}
				else if (k.equals("stroke")  || k.equals("-fx-stroke"))		sh.setStroke(Color.web(val));
				else if (k.equals("strokeWidth")  || k.equals("-fx-stroke-width"))	sh.setStrokeWidth(d);
//				else if (k.equals("selected"))		shape.setSelected(val);
			}
			catch (Exception e) { System.err.println("Parse errors: " + k); }
		}	
	}
	// **-------------------------------------------------------------------------------
	// Polygons and polylines are stored the same, but have different base types
	private void parsePolygonPoints(Polygon poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolylinePoints(Polyline poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolyPoints(ObservableList<Double> pts, String string)
	{
		String s = string.trim();
		s = s.substring(1, s.length()-1);
		String[] doubles = s.split(",");
		for (String d : doubles)
			pts.add(StringUtil.toDouble(d));
	}

	// **-------------------------------------------------------------------------------
	public Node makeNewNode(Tool type, AttributeMap attrMap)
	{
		if (type == Tool.Browser)	return makeBrowser(attrMap);	
		if (type == Tool.Text)		return makeTextArea(attrMap);	
		if (type == Tool.Table)		return makeTableView(attrMap);	
		if (type == Tool.Image)		return makeImageView(attrMap);	
		if (type == Tool.Shape2)	return makeNewSVGPane(attrMap);	
		return null;
	}
	// **-------------------------------------------------------------------------------
	public StackPane handleFileDrop(File f, double x, double y)
	{
		AttributeMap attrs = new AttributeMap(f, x, y);
		if (FileUtil.isImageFile(f))	return makeImageView(attrs);
		if (FileUtil.isSVG(f))			return makeSVGPath(attrs);
		if (FileUtil.isCSV(f))			return makeTableView(attrs);
		if (FileUtil.isWebloc(f))		return makeBrowser(attrs);
		if (FileUtil.isTextFile(f))		return makeTextArea(attrs);
		if (FileUtil.isGPML(f))			new GPML(getController()).addFile(f);
		return null;
	}
	private StackPane makeSVGPath(AttributeMap attrs) {
		String path = attrs.get("file");
		if (path != null)
		{
			String s = FileUtil.readFileIntoString(path);
			if (s != null)
			{
				SVGPath svg = new SVGPath();
				int idx1 = s.indexOf("<g>");
				int idx2 = s.indexOf("</g>") + 4;
				if (idx1 >0 && idx2 > idx1)
					s = s.substring(idx1, idx2);
				svg.setContent(s);
			    StackPane border = makeStackPane(attrs, svg);
				return border;
			}
		}
		return null;
	}
	// **-------------------------------------------------------------------------------
	public Group makeGroup(ObservableList<Node> items)
	{
		Group group = new Group();
		addGroupMouseHandlers(group);
		group.getChildren().addAll(items);
		return group;
	}
	// **-------------------------------------------------------------------------------
	public StackPane makeBrowser(AttributeMap attrMap)
	{
		String url = attrMap.get("url");
		if (url == null) 
		{
			String filepath = attrMap.get("file");		// f.getAbsolutePath()
			url = MacUtil.urlFromPlist(filepath);
		}
		if (url == null) return null;
		WebView webView = new WebView();
		webView.setZoom(0.4);
		webView.getEngine().load(url);
	    StackPane border = makeStackPane(attrMap, webView);
		return border;
	}
	
	// **-------------------------------------------------------------------------------
	public StackPane makeImageView(AttributeMap attrMap)
	{
		String filepath = attrMap.get("file");		// f.getAbsolutePath()
		if (filepath == null) return null;
		Image img = new Image("file:" + filepath);
		if (img.isError())
			System.out.println("makeImageView error");
		ImageView imgView = new ImageView(img);
		if (attrMap.getId() == null) 
			attrMap.put("GraphId", gensym("I"));
		
		imgView.prefWidth(200);
		imgView.prefHeight(200);
		imgView.setFitWidth(200);
		imgView.setFitHeight(200);
		attrMap.put("name", filepath);
//		Label imgView = new Label("TODO FIXME");
	    StackPane border = makeStackPane(attrMap, imgView);
	    imgView.setMouseTransparent(true);
	    imgView.fitWidthProperty().bind(Bindings.subtract(border.widthProperty(), 20));
	    imgView.fitHeightProperty().bind(Bindings.subtract(border.heightProperty(), 40));
	    imgView.setTranslateY(-10);

	    return border;
	}
	// **-------------------------------------------------------------------------------
	public StackPane makeTableView(AttributeMap attrMap)
	{
		TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
		if (attrMap.getId() == null)
			attrMap.put("GraphId", gensym("T"));
		CSVTableData data = FileUtil.openCSVfile(attrMap.get("file"), table);		// TODO THIS CURRENTLY ASSUMES ALL INTS!!
		attrMap.put("name", attrMap.get("file"));
		if (data == null) return null;
	    StackPane border = makeStackPane(attrMap, table);
		return border;
	}
	// **-------------------------------------------------------------------------------
	public StackPane makeTextArea(AttributeMap attr)
	{
		String text = attr.get("text");
		if (text == null)
		{
			String name = attr.get("file");
			StringBuilder buffer = new StringBuilder();
			attr.put("name", name);
			FileUtil.readFile(new File(name), buffer);
			text = buffer.toString();
			attr.put("text", text);
		}
		TextArea textArea = new TextArea(text);
	    StackPane border = makeStackPane(attr, textArea);
	    textArea.setPrefColumnCount(60);
	    textArea.setPrefRowCount(20);
		return border;
	}
	// **-------------------------------------------------------------------------------
	private StackPane makeStackPane(AttributeMap attrMap, Node content)
	{
		String id = attrMap.getId();
		double x = attrMap.getDouble("x");
		double y = attrMap.getDouble("y");
		double w = attrMap.getDouble("width");
		double h = attrMap.getDouble("height");
		String title = attrMap.get("name");
		return makeStackPane( x,  y,  w,  h,  title,  content,  id);
	}
	
	// **-------------------------------------------------------------------------------
	private StackPane makeStackPane(double x, double y, double w, double h, String title, Node content, String id)
	{
		StackPane border = new StackPane();
		border.setId(id);
		if (Double.isNaN(w)) w = 400;
		if (Double.isNaN(h)) h = 300;
		border.prefWidth(w);	border.setMinWidth(w);  border.setLayoutX(x - w/2);
		border.prefHeight(h);	border.setMinHeight(h);  border.setLayoutY(y - h/2);
	   
		border.setStyle("-fx-border-color: green; -fx-border-width: 3; -fx-background-color: beige; -fx-opacity: 1.0;");
		addBorderMouseHandlers(border);
	    
		HBox titleBar = new HBox(50);
	    titleBar.setMaxHeight(25);
	    Label idLabel = new Label(id);
	    idLabel.setMinWidth(50);
	    border.getChildren().addAll(titleBar, content);
	    StackPane.setAlignment(titleBar, Pos.TOP_CENTER);
	    StackPane.setAlignment(content, Pos.BOTTOM_CENTER);
	    StackPane.setMargin(content, new Insets(25,0,0,0));
	    Label titleLabel = new Label(title);
		titleBar.getChildren().addAll(idLabel, titleLabel);
	    titleBar.setMouseTransparent(true);
	    
	    makePopupMenu(titleLabel, content);

	    return border;
	}
	
	private StackPane makeStackPane(StackPane orig, String title, String id)
	{
		double x = orig.getLayoutX();
		double y = orig.getLayoutY();
		double w = orig.getWidth();
		double h = orig.getHeight();
		AttributeMap attrs = new AttributeMap(orig);
		int nKids = orig.getChildren().size();
		if (nKids == 2)
		{
			Node content = orig.getChildren().get(1);
//			System.out.println("content = " + content.toString());
			Node copiedContent = null;
			if (content instanceof ImageView)
			{
				ImageView iview = new ImageView(((ImageView)content).getImage());
				iview.setMouseTransparent(true);
				iview.fitWidthProperty().bind(Bindings.subtract(orig.widthProperty(), 20));
				iview.fitHeightProperty().bind(Bindings.subtract(orig.heightProperty(), 40));
				iview.setTranslateY(-10);
				return makeStackPane(x,y,w,h,title, iview, id);
			}
			if (content instanceof TextArea)
			{
				TextArea view = new TextArea(((TextArea)content).getText());
				view.setPrefColumnCount(60);
				view.setPrefRowCount(20);
				view.setMouseTransparent(true);
				return makeStackPane(x,y,w,h,title, view, id);
			}
				
			if (content instanceof WebView)
			{
				String location = ((WebView)content).getEngine().getLocation();
				WebView view = new WebView();
				view.getEngine().load(location);
				return makeStackPane(x,y,w,h,title, view, id);
			}
				
			if (content instanceof TableView)
			{
				TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
//				if (attrMap.getId() == null)
//					attrMap.put("id", gensym("T"));
//				CSVTableData data = new CSVTableData(id);
			    StackPane border = makeStackPane(attrs, table);
				return border;
			}
		}
		return null;
	}
	
	// **-------------------------------------------------------------------------------
	private  void makePopupMenu(Control attachee, Node content)
	{

		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setOnShowing((e) -> {     System.out.println("showing");		});
		contextMenu.setOnShown((e) -> {       System.out.println("shown");		});
		attachee.setContextMenu(contextMenu);

		MenuItem item1 = makeItem("Bring To Front", e -> System.out.println("Bring To Front")  );
		MenuItem item2 = makeItem("Send To Back", e -> System.out.println("Send To Back")  );
		contextMenu.getItems().addAll(item1, item2);
		
		if (content instanceof TableView)
		{
			MenuItem item3 = makeItem("Line Chart", e -> System.out.println("Line Chart") );
			MenuItem item4 = makeItem("Scatter Chart", e -> System.out.println("Scatter Chart") );
			contextMenu.getItems().addAll(item3, item4);
		}
	}
	private MenuItem makeItem(String name, EventHandler<ActionEvent> foo)
	{
		MenuItem item = new MenuItem(name);	
		item.setOnAction(foo);
		return item;
	}
	// **-------------------------------------------------------------------------------
	public void addBorderMouseHandlers(StackPane border)
	{
		border.addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedHandler());
		border.addEventHandler(MouseEvent.MOUSE_DRAGGED, new MouseDraggedHandler());
		border.addEventHandler(MouseEvent.MOUSE_MOVED, new MouseMovedHandler());
		border.addEventHandler(MouseEvent.MOUSE_RELEASED, new MouseReleasedHandler());
		border.setOnDragDropped(e ->	{ 	handleDrop(e);			e.consume();  	});
	}
	// **-------------------------------------------------------------------------------
	public void addGroupMouseHandlers(Group g)
	{
		g.addEventHandler(MouseEvent.MOUSE_PRESSED, new GroupMousePressedHandler());
		g.addEventHandler(MouseEvent.MOUSE_DRAGGED, new GroupMouseDraggedHandler());
	}

	private class GroupMousePressedHandler implements EventHandler<MouseEvent>
	{
		@Override public void handle(final MouseEvent event)
		{
			if (verbose>3)	
				System.out.println("GroupMousePressedHandler, Target: " + event.getTarget());
			
			EventTarget target = event.getTarget();
			currentPoint = new Point2D(event.getX(), event.getY());
			Node node = (Node) target;
			local = node.localToParent(currentPoint);
			boolean rightClick = event.isSecondaryButtonDown();
	
			prevPoint = local;
			boolean altDown = event.isAltDown();
			if (altDown)
				cloneSelection();
			
			if (event.isPopupTrigger() || rightClick)	
			{
				if (menu == null)
					menu = buildContextMenu(event);
				menu.show(drawLayer.getPane(), event.getScreenX(), event.getScreenY());
	            return;
			}

			boolean wasSelected = drawLayer.getSelectionMgr().isSelected(node);
			if (event.isControlDown())
				drawLayer.getSelectionMgr().select(node, !wasSelected);
			else if (!wasSelected)
				drawLayer.getSelectionMgr().selectX(node);
			
			dragging = true;
			undoStack.push(ActionType.Move);
			startPoint = local;
			event.consume();
		}
	}
	// **-------------------------------------------------------------------------------
	private class GroupMouseDraggedHandler implements EventHandler<MouseEvent>
	{
		@Override		public void handle(final MouseEvent event)
		{
			currentPoint = new Point2D(event.getX(), event.getY());
			if (verbose>3)	
				System.out.println("GroupMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())			return;
			double dx, dy;
			dx = prevPoint.getX() - local.getX();
			dy = prevPoint.getY() - local.getY();
			drawLayer.getSelectionMgr().translate(dx, dy);
			prevPoint = local;
		}
	}	// **-------------------------------------------------------------------------------
	private void handleDrop(DragEvent e)
	{	
		Dragboard db = e.getDragboard();
		e.acceptTransferModes(TransferMode.ANY);
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
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
					if (verbose > 2)
						System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f))
					{
						String path = f.getAbsolutePath();
						int idx = path.indexOf("/know");
						if (idx > 0)
							path = path.substring(idx);
						
						StringBuilder buff = new StringBuilder();
						FileUtil.readFileIntoBuffer(f, buff);
						Node n = (Node) e.getTarget();
						String styl = buff.toString();
//						n.getStyleClass().add(styl);
						if (verbose > 3)
							System.out.println("Style: " + styl);
						n.getScene().getStylesheets().add(path);
				
					}
				}
			}
		}
		e.consume();
	}
// **-------------------------------------------------------------------------------
//	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";

	boolean resizing = false;
	boolean dragging = false;
	Point2D startPoint, currentPoint, local, prevPoint;

//**-------------------------------------------------------------------------------
//ABSTRACT HANDLERS
	private class MouseMovedHandler implements EventHandler<MouseEvent>
	{
		@Override	public void handle(final MouseEvent event)		
		{	
			currentPoint = new Point2D(event.getSceneX(), event.getSceneY());
		}
	}
	// **-------------------------------------------------------------------------------
	
	private class MousePressedHandler implements EventHandler<MouseEvent>
	{
		@Override public void handle(final MouseEvent event)
		{
			if (verbose>3)	
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			
			resizing = false;
			EventTarget target = event.getTarget();
//			scenePoint = new Point2D(event.getSceneX(), event.getSceneY());
			currentPoint = new Point2D(event.getX(), event.getY());
			Node node = (Node) target;
			local = node.localToParent(currentPoint);
			boolean rightClick = event.isSecondaryButtonDown();
	
			prevPoint = local;
			boolean altDown = event.isAltDown();
			if (altDown)
				cloneSelection();
			if (event.isPopupTrigger() || rightClick)	
			{
				if (menu == null)
					menu = buildContextMenu(event);
					
				menu.show(drawLayer.getPane(), event.getScreenX(), event.getScreenY());
				
	            return;// TODO -- popup up Node menu
			}
			if (event.isSecondaryButtonDown())			// middle or mouse wheel click
			{
//				return;
			}
			
			boolean wasSelected = drawLayer.getSelectionMgr().isSelected(node);
			if (event.isControlDown())
				drawLayer.getSelectionMgr().select(node, !wasSelected);
			else if (!wasSelected)
				drawLayer.getSelectionMgr().selectX(node);
						
			if (RectangleUtil.inCorner(event))
			{
				resizing = true;
				undoStack.push(ActionType.Resize);
				startPoint = node.localToParent(RectangleUtil.oppositeCorner(event));
			}
			else if (drawLayer.getSelectionMgr().count() > 0)
			{
				dragging = true;
				undoStack.push(ActionType.Move);
				startPoint = local;
			}
			event.consume();
		}
	}
	// **-------------------------------------------------------------------------------
	ContextMenu menu;

	ContextMenu buildContextMenu(MouseEvent event)
	{
		menu = new ContextMenu();

		EventTarget target = event.getTarget();
		if (target instanceof StackPane)
		{
//			System.out.println("sdfasdfasd");
			int nKids = ((StackPane)target).getChildren().size();
			if (nKids == 2)
			{
				Node content = ((StackPane)target).getChildren().get(1);
				if (content instanceof TableView)
				{
					MenuItem scatter = new MenuItem("Make Scatter Chart");
					scatter.setOnAction(e -> {});
					MenuItem timeseries = new MenuItem("Make Time Series");
					timeseries.setOnAction(e -> {});
					SeparatorMenuItem sep = new SeparatorMenuItem();
					menu.getItems().addAll(scatter, timeseries, sep);

				}
			}
		}
		
		MenuItem toFront = new MenuItem("Bring To Front");
		toFront.setOnAction(e ->  {   	getController().toFront();   });
		MenuItem toBack = new MenuItem("Send To Back");
		toBack.setOnAction(e ->  {   	getController().toBack();   });
		MenuItem group = new MenuItem("Group");
		group.setOnAction(e ->  {		getController().group();    });
		MenuItem ungroup = new MenuItem("Ungroup");
		ungroup.setOnAction(e -> { 		getController().ungroup();  });
		menu.getItems().addAll(toFront, toBack, group, ungroup);
		return menu;
	
		
	}
	// **-------------------------------------------------------------------------------
	public void cloneSelection()
	{
		List<Node> sel = drawLayer.getSelectionMgr().getAll();
		undoStack.push(ActionType.Duplicate);
		for (Node n : sel)
		{
			if ("Marquee".equals(n.getId()))	continue;
			String text = Model.describe(n);
			int idx = text.indexOf("GraphId=");
			if (idx < 0) continue;			// FIXME: nodes read in from file don't have id
			int idStart = idx+ "GraphId=\"".length();		// inject a new id into the string
			int idEnd = text.indexOf("\"", idStart );
			String oldId = text.substring(idStart, idEnd);
			String newId = getModel().cloneResourceId(oldId);
			String newText = text.substring(0, idStart);		
			newText += newId + text.substring(idEnd);
			
//			cloneNode(oldId, newId);  //parseNode(newText, false);
			Object ob = getModel().getResource(oldId);
			if (ob != null && ob instanceof StackPane)
			{
				StackPane clone =  makeStackPane((StackPane)ob, "", newId);
				if (clone != null)
				{
					setAttributes(clone, new AttributeMap(newText));
					drawLayer.getController().add(clone);
				}
			}
			else 
			{
				Node clone = parseGPML(newText, false);
				if (clone != null)		
					drawLayer.getController().add(clone);
				
			}
		}
	}
	StackPane cloneNode(String origId, String newId)
	{
		Object ob = getModel().getResource(origId);
		if (ob != null && ob instanceof StackPane)
		{
			StackPane pane = (StackPane) ob;
			return makeStackPane(pane, "", newId);
		}
		return null;
	}
	// **-------------------------------------------------------------------------------
	private class MouseDraggedHandler implements EventHandler<MouseEvent>
	{
		@Override		public void handle(final MouseEvent event)
		{
			currentPoint = new Point2D(event.getX(), event.getY());
			if (verbose>3)	
				System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())			return;
			if (event.getTarget() instanceof StackPane)
			{
				if (resizing)
				{
					StackPane r = (StackPane) event.getTarget();
					Point2D local = r.localToParent(currentPoint);
					RectangleUtil.setRect(r, startPoint, local);
							
				}
				else if (dragging)
				{
					StackPane r = (StackPane) event.getTarget();
					Point2D local = r.localToParent(currentPoint);
					double dx = prevPoint.getX() - local.getX();
					double dy = prevPoint.getY() - local.getY();
				
	//				System.out.println("Delta: " + dx + ", " + dy);
					drawLayer.getSelectionMgr().translate(dx, dy);
					prevPoint = local;
				}
				event.consume();
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private class MouseReleasedHandler implements EventHandler<MouseEvent>
	{
		@Override		public void handle(final MouseEvent event)
		{
			startPoint = null;
			resizing = dragging = false;
			drawLayer.getPane().requestFocus();	 // needed for the key event handler to receive events
			event.consume();
			drawLayer.getController().refreshZoomPane();
		}
	}

}