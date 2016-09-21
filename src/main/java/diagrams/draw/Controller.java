package diagrams.draw;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import animation.BorderPaneAnimator;
import diagrams.draw.Action.ActionType;
import diagrams.draw.App.Tool;
import dialogs.AboutDialog;
import gui.BorderPaneRulers;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Callback;
import model.AttributeMap;
import model.AttributeValue;
import model.RandomAttributeValueData;


public class Controller implements Initializable
{
	private Model model;
	public Model getDrawModel()   		{ 		return model;  }
	private Pasteboard pasteboard;
	public Pasteboard getPasteboard()   { 		return pasteboard;  }
	public NodeFactory getNodeFactory()	{		return pasteboard.getNodeFactory();	}
	private UndoStack undoStack;
	public UndoStack getUndoStack()		{		return undoStack;	}
	private Document doc;

	int verbose = 0;
	
	@FXML private Pane drawPane;
	@FXML private ScrollPane scrollPane;
	@FXML private ListView<Action> undoview;
	@FXML private ListView<Node> resourceListView;
	@FXML private TableView<AttributeValue> attributeTable;
	@FXML private TableColumn<AttributeValue, String> attributeCol;
	@FXML private TableColumn<AttributeValue, String> valueCol;
	@FXML private AnchorPane zoomAnchor;
	@FXML private BorderPane container;			// root of fxml
	@FXML private BorderPane drawContainer;			// rulers and content
	public ListView<Node> getResourceListView()		{		return resourceListView;	}
	
	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
	@FXML private Button toggleRulerButton;
	@FXML private Button toggleGridButton;
	
	@FXML private void setArrow()		{ pasteboard.setTool(Tool.Arrow);	}
	@FXML private void setRectangle()	{ pasteboard.setTool(Tool.Rectangle);}	// TODO capture double click for stickiness
	@FXML private void setOval()		{ pasteboard.setTool(Tool.Circle);		}
	@FXML private void setPolygon()		{ pasteboard.setTool(Tool.Polygon);	}
	@FXML private void setPolyline()	{ pasteboard.setTool(Tool.Polyline);	}
	@FXML private void setLine()		{ pasteboard.setTool(Tool.Line);	}
	
	@FXML private ToggleButton arrow;
	@FXML private ToggleButton rectangle;
	@FXML private ToggleButton circle;
	@FXML private ToggleButton polygon;
	@FXML private ToggleButton polyline;
	@FXML private ToggleButton line;
	
	@FXML private ColorPicker fillColor;
	@FXML private ColorPicker lineColor;
	@FXML private Slider weight;
	@FXML private Slider opacity;
	@FXML private Slider rotation;
	@FXML private Label status1;
	@FXML private Label status2;
	@FXML private Label status3;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	
	@FXML private Slider translateX;			// debug fns for zoom view
	@FXML private Slider translateY;			// debug fns for zoom view
	@FXML private Slider scale;
	//-------------------------------------------------------------
	@FXML private MenuItem undo;
	@FXML private MenuItem redo;
	@FXML private MenuItem clearundo;
	
	@FXML private MenuItem tofront;
	@FXML private MenuItem toback;
	@FXML private MenuItem group;
	@FXML private MenuItem ungroup;
	@FXML private MenuItem delete;
	@FXML private VBox inspector;
	@FXML private HBox bottomDash;
	
	@FXML private void undo()			{ 	undoStack.undo();	}
	@FXML private void redo()			{ 	undoStack.redo();		}
	
	// **-------------------------------------------------------------------------------
	@FXML private void doNew()			{ 	App.getInstance().doNew(null);			}
	@FXML private void open()			{ 	doc.open();	}			//doc.open();
	@FXML private void save()			{ 	doc.save();	}
	@FXML private void saveas()			{	doc.saveas();		}
	@FXML private void close()			
	{ 
		doc.close();	
		App.getInstance().getStage().close();
	}
	@FXML private void print()			{ 	doc.print();			}
	@FXML private void quit()			{ 	Platform.exit();			}
	// **-------------------------------------------------------------------------------
	@FXML private  void cut()			{ 	undoStack.push(ActionType.Cut);		getSelectionManager().cut();	}
	@FXML private  void copy()			{ 	getSelectionManager().copy();		}	// not undoable
	@FXML private  void paste()			{ 	undoStack.push(ActionType.Paste);	doPaste();		}
	@FXML private  void clearUndo()		{	undoStack.clear(); 	}
	@FXML private void selectAll()		{ 	undoStack.push(ActionType.Select); getSelectionManager().selectAll(); 		}
	@FXML public void deleteSelection(){ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteSelection(); 	}
	@FXML public void duplicateSelection(){ 	undoStack.push(ActionType.Duplicate);	getNodeFactory().cloneSelection(); 	}
	// **-------------------------------------------------------------------------------
	@FXML public  void group()			{ 		undoStack.push(ActionType.Group);	getSelectionManager().doGroup();  }
	@FXML public  void ungroup()		{ 		undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void toFront()		{		undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{		undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.getGrid().toBack();  	}

	// **-------------------------------------------------------------------------------
	@FXML private MenuItem connect;			// TODO bind enableProperty to selection size > 2
	@FXML private void addEdges()		
	{ 	
		if (getSelection().size() >= 2)		
		{
			List<Edge> edges = getDrawModel().connectSelectedNodes();
			for (Edge e: edges)
				add(0, e);
		}
	}

	
	static String CSS_Gray2 = "-fx-border-width: 2; -fx-border-color: gray;";
	static String CSS_cellBackground(boolean undone) 	{		return "-fx-background-color: " + (undone ? "GREY; " : "BEIGE; ");	}
	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";
	static String missing(String s)	{		return String.format(ctrlStr, s, "AttributeValueFXML.fxml");	}
	
	// **-------------------------------------------------------------------------------
	private ToggleGroup paletteGroup;
	public ToggleGroup getToolGroup()	{ return paletteGroup;	}
	public Selection getSelectionManager() 		{ 	return pasteboard.getSelectionMgr();  }
	public ObservableList<Node> getSelection() 		{ 	return getSelectionManager().getAll();  }

	// **-------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		final double SCALE_DELTA = 1.1;
		model = new Model(this);
		assert(drawPane != null);
		assert attributeCol != null : missing("attributeCol");
		assert valueCol != null : missing("valueCol");
		assert attributeTable != null : missing("attributeTable");
		undoStack = new UndoStack(this, undoview);
		pasteboard = new Pasteboard(drawPane, this);
		doc = new Document(this);
		paletteGroup = new ToggleGroup();
		paletteGroup.getToggles().addAll(arrow, rectangle, circle, polygon, polyline, line);
		bindInspector();
		String cssURL = this.getClass().getResource("draw.css").toExternalForm();
		drawPane.getStylesheets().add(cssURL);
		
//		drawContainer.setBorder(Borders.etchedBorder);
		drawContainer.setOnScroll(ev -> {
			ev.consume();
	        if (ev.getDeltaY() == 0)   return;	
	        double scaleFactor = (ev.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
	        scale.valueProperty().set(scale.valueProperty().get() * scaleFactor);
//	        drawContainer.setScaleX(drawContainer.getScaleX() * scaleFactor);
//	        drawContainer.setScaleY(drawContainer.getScaleY() * scaleFactor);
		});
//		bottomDash.setBorder(Borders.dashedBorder );
		bottomDash.setPadding(new Insets(3,3,4,4));

		inspector.setBorder(Borders.lineBorder);
		setupPalette();
		setupListviews();
		setupZoomView();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 80);
		new BorderPaneAnimator(container, rightSideBarButton, Side.RIGHT, false, 300);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 100);
		new BorderPaneRulers(drawContainer, toggleRulerButton);
		pasteboard.makeGrid(toggleGridButton, scrollPane);

		boolean startWithShapes = true;
		if (startWithShapes) test1();
			
		
        new Thread(() ->
           Platform.runLater(() -> { refreshZoomPane(); })).start();    
	}
	//-----------------------------------------------------------------------
	@FXML private void doAbout()
	{
		AboutDialog dlog = new AboutDialog();
		dlog.showAndWait();
	}
	Map<Object, Object> dependents = new HashMap<Object, Object>();
	//-----------------------------------------------------------------------
	private final ChangeListener<Object> changeListener = 
		    (obs, oldValue, newValue) ->  System.out.println("The binding is now invalid.");
	@FXML private void test1()
	{
		undoStack.push(ActionType.Test);	
		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO);
		attrMap.putCircle(new Circle(120, 230, 40));
		Shape n1 = f.makeNewShape(Tool.Circle, attrMap);
		final Label text = f.createLabel("root");
    	NodeCenter ctr = new NodeCenter(n1);
    	text.layoutXProperty().bind(ctr.centerXProperty().subtract(text.widthProperty().divide(2.)));	// width / 2
    	text.layoutYProperty().bind(ctr.centerYProperty().subtract(text.heightProperty().divide(2.)));
//    	n1.addEventHandler(EventType.CHANGE, e -> {});
//        n1.boundsInLocalProperty().addListener(changeListener);
//  	n1.visibleProperty().addListener(changeListener);
//    	n1.visibleProperty().addListener((obs, old, val) ->
//    	{  
//    		System.out.println("val: " + val.toString());
//    		if (val == null) {}
//    		});
////		n1.addListenerToDelete(text)
		add(n1);
		add(text);
		dependents.put(n1, text);
	
	
		attrMap.putFillStroke(Color.CORNSILK, Color.BLUE);
		attrMap.putCircle(new Circle(220, 130, 60));
		Shape circ = f.makeNewShape(Tool.Circle, attrMap);		//, "Eli"
//		f.makeNodeMouseHandler(stk);
		add(circ);
	
		attrMap.putFillStroke(Color.LIGHTSKYBLUE, Color.DARKOLIVEGREEN);
		attrMap.putCircle(new Circle(220, 330, 60));
		Shape n3 = f.makeNewShape(Tool.Circle, attrMap);	//, "Fristcut"
		add(n3);
		
		Edge line1 = model.addEdge(circ, n3);
		Edge line2 = model.addEdge(circ, n1);
		
		add(0, line1);
		add(0, line2);
		
		Rectangle r1 = new Rectangle(290, 230, 60, 60);
		attrMap.putRect(r1);
		attrMap.putFillStroke(Color.CORNSILK, Color.DARKOLIVEGREEN);
		Rectangle n4 = (Rectangle) f.makeNewNode(Tool.Rectangle, attrMap);
		add(n4);
			
		Edge line3 = model.addEdge(n4, circ);
		line3.setStrokeWidth(2);
		add(0, line3);
	
		Line line4 = model.addEdge(n1, n3);
		line4.setStrokeWidth(2);
		add(0, line4);
	}

// **-------------------------------------------------------------------------------
	@FXML private void test2()
	{
		undoStack.push(ActionType.Test);	
		double WIDTH = 20;
		double HEIGHT = 20;
		double RADIUS = 10;
		double spacer = 5 * RADIUS;
		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO, 1.0);
		for (int i=0; i<WIDTH; i++)
			for (int j=0; j<HEIGHT; j++)
			{
				Circle c1 = new Circle(i * spacer, j * spacer, RADIUS);
				attrMap.putCircle(c1);
				attrMap.put("id", i + ", " + j);
				add(f.makeNewNode(Tool.Circle, attrMap));
			}
	}
	
	@FXML private void test3()
	{
	}
	//--------------------------------------------------------------------
	
	
	private ZoomView zoomView;
	
	private void setupZoomView()
	{
		zoomView = new ZoomView(zoomAnchor, drawPane, this);
		Border myBorder = new Border(new BorderStroke(Color.GRAY, 
				BorderStrokeStyle.SOLID, 
				CornerRadii.EMPTY, new BorderWidths(5))	);
		
		zoomAnchor.setBorder(myBorder);
//		Borders.wrap(zoomAnchor).lineBorder().buildAll();
//		viewport.xProperty().bind(drawPane.translateXProperty().multiply(-0.25));  
//		viewport.yProperty().bind(drawPane.translateYProperty().multiply(-0.25));  
//		viewport.widthProperty().bind(drawPane.scaleXProperty());  
//		viewport.heightProperty().bind(drawPane.scaleYProperty());  

//		scale.valueProperty().bindBidirectional(drawPane.scaleXProperty());  
//		scale.valueProperty().bindBidirectional(drawPane.scaleYProperty());  
//		translate.valueProperty().bindBidirectional(drawPane.translateYProperty());  
//		translate.valueProperty().bindBidirectional(drawPane.translateXProperty());  
//		
		// binding sliders to drawPane's scale and offset
		
		scale.valueProperty().addListener((ov, old, val) ->  {
            	double scale = Math.pow(2, (double) val);
    			drawPane.setScaleX(scale); 	
    			drawPane.setScaleY(scale); 	
    			zoomView.zoomChanged();
	        });	
		translateX.valueProperty().addListener((ov, old, val) ->  {
				drawPane.setTranslateX((double) val);  
    			zoomView.zoomChanged();
    			status2.setText(translateX.toString());
        });	
		
		translateY.valueProperty().addListener((ov, old, val) ->   {
				drawPane.setTranslateY((double) val);  
    			zoomView.zoomChanged();
    			status3.setText(translateY.toString());
       });	

	}
	// **-------------------------------------------------------------------------------
	private void setupPalette()
	{
		arrow.setGraphic(		GlyphsDude.createIcon(FontAwesomeIcons.LOCATION_ARROW, GlyphIcon.DEFAULT_ICON_SIZE));
		rectangle.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.SQUARE, GlyphIcon.DEFAULT_ICON_SIZE));
		circle.setGraphic(		GlyphsDude.createIcon(FontAwesomeIcons.CIRCLE, GlyphIcon.DEFAULT_ICON_SIZE));
		polygon.setGraphic(		GlyphsDude.createIcon(FontAwesomeIcons.STAR, GlyphIcon.DEFAULT_ICON_SIZE));
		polyline.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.PENCIL, GlyphIcon.DEFAULT_ICON_SIZE));
		line.setGraphic(		GlyphsDude.createIcon(FontAwesomeIcons.LONG_ARROW_RIGHT, GlyphIcon.DEFAULT_ICON_SIZE));
		arrow.setId(Tool.Arrow.name());
		rectangle.setId(Tool.Rectangle.name());
		circle.setId(Tool.Circle.name());
		polygon.setId(Tool.Polygon.name());
		polyline.setId(Tool.Polyline.name());
		line.setId(Tool.Line.name());

		leftSideBarButton.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT, GlyphIcon.DEFAULT_ICON_SIZE));
		rightSideBarButton.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_O_LEFT, GlyphIcon.DEFAULT_ICON_SIZE));
		bottomSideBarButton.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_DOWN, GlyphIcon.DEFAULT_ICON_SIZE));
		leftSideBarButton.setText("");
		rightSideBarButton.setText("");
		bottomSideBarButton.setText("");

		toggleRulerButton.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.BARS, GlyphIcon.DEFAULT_ICON_SIZE));
		toggleRulerButton.setText("");
		toggleGridButton.setGraphic(	GlyphsDude.createIcon(FontAwesomeIcons.TH, GlyphIcon.DEFAULT_ICON_SIZE));
		toggleGridButton.setText("");
	}
	// **-------------------------------------------------------------------------------
	public void setState(String s)
	{
		drawPane.getChildren().clear();
		drawPane.getChildren().add(pasteboard.getGrid());
		addState(s);
	}
	public void addState(String s)
	{
		Scanner scan = new Scanner(s);
		while (scan.hasNextLine())
		{
			String line = scan.nextLine();
			Node node = getNodeFactory().parseNode(line, true);
			if (node != null)
			{
				if (verbose > 0) System.out.print(", adding " + node.getId());
				add(node);
			}
			if (verbose > 0) System.out.println();
		} 
		scan.close();
		refreshZoomPane();
	}
	public void doPaste()
	{
		// TODO Auto-generated method stub
		
	}

	private void setupListviews()
	{
		undoview.setCellFactory(list -> {  return new DrawActionCell();  });	
		undoview.setStyle(CSS_Gray2);
		
		resourceListView.setCellFactory(list ->  { return new DrawNodeCell();   });
		resourceListView.setStyle(CSS_Gray2);

		attributeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
		attributeTable.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView
		attributeTable.getSelectionModel().setCellSelectionEnabled(false);

		setRowFactory();				// set the Row Factory of the table
		
		attributeTable.setStyle(CSS_Gray2);
		attributeCol.setCellValueFactory(new PropertyValueFactory("attribute"));
		valueCol.setCellValueFactory(new PropertyValueFactory("value"));
		
	}
	  static class DrawNodeCell extends ListCell<Node> {
		    @Override
		    public void updateItem(Node item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
//		    	  String style = CSS_cellBackground(item.isUndone());
//		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else     {   setStyle("");  	  setText("");   }		// the cell is reused, so clear it
		    }
		  }
	  
	  
	  static class DrawActionCell extends ListCell<Action> {
		    @Override
		    public void updateItem(Action item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
		    	  String style = CSS_cellBackground(item.isUndone());
		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else   {  	  setStyle("");  	  setText("");    }
		    }
		  }
	  
	  static class StyleSheetCell extends ListCell<String> {
		    @Override
		    public void updateItem(String item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
//		    	  String style = CSS_cellBackground(item.isUndone());
//		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else    {     setStyle("");   	  setText("");      }
		    }
		  }
	// **-------------------------------------------------------------------------------
	void bindInspector()
	{
		strokeLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.PENCIL, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.CAR, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setText("");		fillLabel.setMinWidth(30);		fillLabel.setAlignment(Pos.BASELINE_CENTER);
		strokeLabel.setText(""); 	strokeLabel.setMinWidth(30);	strokeLabel.setAlignment(Pos.BASELINE_CENTER);
		
		fillColor.setOnAction(evt -> apply(true, fillColor));
		lineColor.addEventHandler(ActionEvent.ACTION, evt -> apply(true, lineColor));

		opacity.valueProperty().addListener((ov, old, val) ->   {  	apply(false, opacity);        });	
		weight.valueProperty().addListener((ov, old, val) ->    {   apply(false, weight);   });	
		rotation.valueProperty().addListener((ov, old, val) ->  {   apply(false, rotation);  });	

		
		// sliders don't record undoable events (because they make so many) so snapshot the state on mousePressed
		EventHandler<Event> evH = event ->{	undoStack.push(ActionType.Property);  };
		opacity.setOnMousePressed(evH); 
		weight.setOnMousePressed(evH);
		rotation.setOnMousePressed(evH);
	}
		
	// **-------------------------------------------------------------------------------

	private String getStyleSettings(Control src)
	{
		Color fill = fillColor.getValue();
		Color stroke = lineColor.getValue();
		if (fill == null) fill = Color.GRAY;
		if (stroke == null) stroke = Color.DARKGRAY;
		String fillHex = "-fx-fill: #" + fill.toString().substring(2, 8) + ";\n";
		String strokeHex = "-fx-stroke: #" + stroke.toString().substring(2, 8) + ";\n";
		String wtStr = String.format("-fx-stroke-width: %.1f;\n", weight.getValue());
		String opacStr = String.format("-fx-opacity: %.2f;\n", opacity.getValue());
		String rotStr = String.format("-fx-rotate: %.1f;\n", rotation.getValue());

//		if (src == fillColor)		return fillHex;
//		if (src == lineColor)		return strokeHex;
//		if (src == weight)			return wtStr;
//		if (src == opacity)			return opacStr;
//		if (src == rotation)		return rotStr;

		StringBuilder buff = new StringBuilder();
		buff.append(fillHex).append(strokeHex).append(wtStr).append(opacStr).append(rotStr);
		return buff.toString();
	}
//	
//	public static String getStyle(Shape n)
//	{
//		DecimalFormat fmt = new DecimalFormat("0.00");
//		StringBuilder buff = new StringBuilder();
//		Paint fill = n.getFill();
//		String fillStr = fill.toString();
//		Paint stroke = n.getStroke();
//		String strokeStr = stroke.toString();
//		double wt = n.getStrokeWidth();
//		String wtStr = fmt.format(wt);
//		double opac = n.getOpacity();
//		String opacStr = fmt.format(opac);
//		buff.append("-fx-fill: #").append(fillStr).append(";\n");
//		buff.append("-fx-stroke: #").append(strokeStr).append(";\n");
//		buff.append("-fx-stroke-width: ").append(wtStr).append(";\n");
//		buff.append("-fx-opacity: ").append(opacStr).append(";\n");
//		return buff.toString();
//	}
	
	// **-------------------------------------------------------------------------------
	public void setUpInspector()
	{
		if(getSelectionManager().count() == 1)
		{
			Node firstNode = getSelection().get(0);
			if (firstNode instanceof Shape)
			{
				Shape n = (Shape) getSelection().get(0);
				Paint fill = n.getFill();
				Paint stroke = n.getStroke();
				double wt = n.getStrokeWidth();
				double opac = n.getOpacity();
				double rot = n.getRotate();
				
				fillColor.setValue((Color) fill);
				lineColor.setValue((Color) stroke);
				weight.setValue(wt);
				opacity.setValue(100 * opac);
				rotation.setValue(rot);
			}
			if (firstNode instanceof StackPane)
			{
				StackPane n = (StackPane) getSelection().get(0);
				String style = n.getStyle();
				AttributeMap attr = new AttributeMap(style, true);
				Color fill = attr.getColor("-fx-background-color");
				Color stroke = attr.getColor("-fx-border-color");
				double opac = attr.getDouble("-fx-opacity", 1);
				double wt = attr.getDouble("-fx-border-width", 12);
				double rot = attr.getDouble("-fx-rotate", 0);
//				stroke = attr.getColor("-fx-border-color");
//				n.getBackground().getFills().get(0).getFill();
//				""
//				Paint stroke = n.getChildren().get(0).getBetBorder().getStrokes().get(0).get();
//				double wt = n.getStrokeWidth();
//				double opac = n.getOpacity();
//				
				fillColor.setValue(fill);
				lineColor.setValue(stroke);
				weight.setValue(wt);
				rotation.setValue(rot);
				opacity.setValue(100 * opac);
			}
			
		}
	}
	// **-------------------------------------------------------------------------------
	private void apply(boolean undoable, Control src)							
	{ 	 			getSelectionManager().applyStyle(getStyleSettings(src));	
		if (undoable) 
			undoStack.push(ActionType.Property); 
	}
	// **-------------------------------------------------------------------------------
	public void selectAll(ObservableList<Node> n)	{		getSelectionManager().selectAll(n);	}

	public void setStatus(String s)					{ 		status1.setText(s);	}
	public void remove(Node n)						
	{		
		getDrawModel().removeNode(n);
		Object dependent = dependents.get(n);
		if (dependent != null)
		{
			drawPane.getChildren().remove(dependent);	
			dependents.remove(n);
		}
		drawPane.getChildren().remove(n);	
	}
	public void add(Node n)							
	{		
		drawPane.getChildren().add(n);	
		if ("Marquee".equals(n.getId())) 	return;
		model.addResource(n.getId(), n);
	}
	public void add(int idx, Node n)							
	{		
		drawPane.getChildren().add(idx, n);	
		if ("Marquee".equals(n.getId())) 	return;
		model.addResource(n.getId(), n);
	}
	public void addAll(ObservableList<Node> n)		{		drawPane.getChildren().addAll(n);	}
	// **-------------------------------------------------------------------------------
	public String getState()			{ 	return Model.traverseSceneGraph(drawPane).toString();  }
	
	public void refreshZoomPane()		{	zoomView.zoomChanged();}

	// **-------------------------------------------------------------------------------
	public void reportStatus(String string)	
	{		
		status1.setText(string);	
		refreshZoomPane();
	}
	public void setStatus2(String status)	{	status2.setText(status);	}
	public void setStatus3(String status)	{	status3.setText(status);	}
	// **-------------------------------------------------------------------------------
	public void addStylesheet(File f)
	{
		Scene scene = drawPane.getScene();
		ObservableList<String> sheets = scene.getStylesheets();
		sheets.add(f.getName());
//		attributeTable.setItems(sheets);
	}
	// **-------------------------------------------------------------------------------
	/**
	 * Set Row Factory for the TableView
	 */
	public void setRowFactory()
	{
		attributeTable.setRowFactory(new Callback<TableView<AttributeValue>, TableRow<AttributeValue>>()
		{
			@Override public TableRow<AttributeValue> call(TableView<AttributeValue> p)
			{
				final TableRow<AttributeValue> row = new TableRow<AttributeValue>();
				row.setOnDragEntered(e ->	{		});

				row.setOnDragDetected(e ->
				{
					Dragboard db = row.getTableView().startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();
					AttributeValue av = row.getItem();
					String s = av.makeString();			/// handle multiple selection by asking table to generate the string
					content.put(DataFormat.PLAIN_TEXT, s);
					db.setContent(content);
//					setSelection(row);
					e.consume();
				});
				return row;
			}
		});
	}
	// **-------------------------------------------------------------------------------
	// TODO add charts
	
	public void scatter(TableView content)	{
	}

	public void timeseries(TableView content)	{
	}
	
}
