package table.binder;

import gui.Backgrounds;
import gui.Borders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.IValueUnitSetter;
import model.Unit;
import model.ValueWithUnitsBox;
import util.NodeUtil;

public class BindingsController implements InvalidationListener, IValueUnitSetter
{

	@FXML private SimpleDoubleProperty rectScale = new SimpleDoubleProperty(1.);

	@FXML private VBox threeLineContainer;
	@FXML private Label widthDisplay; // axis of rectDisplay
	@FXML private Label heightDisplay; // "
	private Label areaDisplay; // label centered in rectDisplay
	@FXML private Label scaleDisplay; // the scale to get current size to fit, we also show the date field here
	@FXML private VBox dragboxContainer;
	
	// ------------------------------------------------------------------------------
	// specs:
	// if area is edited, adjust both width and height to maintain aspect ratio
	// if a unit is changed immediately after the value, dont recalc value
	// ------------------------------------------------------------------------------
	boolean verbose = false;
	// these four items comprise our form or view or scene graph
	
	private ValueWithUnitsBox widthLine;
	private ValueWithUnitsBox heightLine;
	private ValueWithUnitsBox areaLine;
	private CornerDragBox dragbox;
	private BindingTable table;
	// ------------------------------------------------------------------------------
	// the app has 3 components: a dragbox to edit Rects with a mouse; a form to
	// input Rects; and a table<Rect>

	public void initialize()
	{
		assert (widthDisplay != null);
		assert (heightDisplay != null);
		assert (scaleDisplay != null);
		areaDisplay = new Label();
		areaDisplay.setStyle("-fx-text-fill: yellow;");
		dragbox = new CornerDragBox(this, widthDisplay, heightDisplay, areaDisplay, scaleDisplay);
		dragboxContainer.getChildren().add(dragbox);
		VBox.setVgrow(dragbox, Priority.ALWAYS);

		assert (threeLineContainer != null);
		widthLine = new ValueWithUnitsBox("width", "Width:", this);
		heightLine = new ValueWithUnitsBox("height", "Height:", this);
		areaLine = new ValueWithUnitsBox("area", "Area:", this);
		threeLineContainer.setSpacing(8);
		threeLineContainer.getChildren().addAll(widthLine, heightLine, areaLine);

		table = new BindingTable(this);
		createData();
	}

	// ------------------------------------------------------------------------------
	// @formatter:off
	public Rect getActiveRecord()				{		return getActiveRect();	}
	public void setActiveRecord(Rect r)			{		tableView.getItems().set(table.getSelectedIndex(), r);	}
	public void setUnit(String id, Unit un)		{		getActiveRecord().setUnits(id, un);	}
	public void setValue(String id, double d)	{		getActiveRecord().setVal(id, d);	}
	// @formatter:on
	// ------------------------------------------------------------------------------
	//most of this handles only the initial case where there may not be data in the table
	private Rect getActiveRect()
	{
		Object selectedItem = tableView.getSelectionModel().getSelectedItem();
		if (selectedItem == null)
		{
			ObservableList<Rect> items = tableView.getItems();
			if (items == null)
				items = FXCollections.observableArrayList();
			if (items.isEmpty())
				items.add(new Rect(this));
			if (verbose) System.out.println("items: " + items.size());
			table.select(0);
			if (verbose) System.out.println("selected: " + table.getSelectedIndex());
			selectedItem = table.getSelectedItem();
		}
		return (Rect) selectedItem;
	}
	
	// ------------------------------------------------------------------------------
	//	key method to move data from the model (getActiveRecord()) to the view components
	
	public void install()
	{	
		Rect r = getActiveRecord();
		if (r == null)
		{
			System.out.println("getActiveRecord returned null");
			return;
		}
		widthLine.install(r.widthRecordProperty().getValue());
		heightLine.install(r.heightRecordProperty().getValue());
		areaLine.install(r.areaRecordProperty().getValue());
		dragbox.install(r);
//setActiveRecord(r);
		table.refresh();
	}


	// ------------------------------------------------------------------------------
	// All the table information is injected into the controller, but I'm passing
	//	it to the BindingTable
	
	@FXML private TableView<Rect> tableView;
	@FXML private TableColumn<Rect, Double> widthCol;
	@FXML private TableColumn<Rect, Unit> widthUnitsCol;
	@FXML private TableColumn<Rect, Double> heightCol;
	@FXML private TableColumn<Rect, Unit> heightUnitsCol;
	@FXML private TableColumn<Rect, Double> areaCol;
	@FXML private TableColumn<Rect, Unit> areaUnitsCol;
	@FXML private TableColumn<Rect, Boolean> selectedCol;
	@FXML private TableColumn<Rect, Color> colorCol;
	@FXML private TableColumn<Rect, LocalDate> dueDateCol;
	public TableColumn<?,?>[] getCols()	{ return new TableColumn[] {
					selectedCol, colorCol, widthCol, widthUnitsCol, 
					heightCol, heightUnitsCol, areaCol, areaUnitsCol, dueDateCol}; }

	public TableView<Rect> getTableView()	{		return tableView;	}
	// ------------------------------------------------------------------------------
	// hard coded initial data set.  Could come from file or database
	
	private void createData()
	{
		Rect[] deflts = new Rect[] { new Rect(this, 10, Unit.CM, 10, Unit.CM, Color.BROWN),
						new Rect(this, 10, Unit.CM, 2, Unit.CM, Color.GREEN), 
						new Rect(this, 1, Unit.IN, 2, Unit.IN, Color.CORAL),
						new Rect(this, 100.5, Unit.M, 132, Unit.M, Color.BLACK) };

		ObservableList<Rect> data = FXCollections.observableArrayList();
		for (Rect r : deflts)
			data.add(r);
		tableView.setItems(data);
		table.select(0);
		tableView.requestFocus();
	}

	// ------------------------------------------------------------------------------
	@Override public void invalidated(Observable observable)
	{
		if (observable instanceof Rect)
			table.select((Rect) observable);
	}
	// ------------------------------------------------------------------------------
	@FXML private Button addRowBtn; 		// only injected in case we want to disable them
	@FXML private Button deleteRowBtn; 		// under certain conditions (eg: tableView.getItems().size() > max)
	// ------------------------------------------------------------------------------
	@FXML void addRow()
	{
		if (verbose) System.out.println("addRow");
		ObservableList<Rect> items = tableView.getItems();
		Rect newRect = new Rect(this);
		items.add(newRect);
//		tableView.setItems(null);
		tableView.setItems(items);
		table.select(newRect);
		tableView.requestFocus();
	}
	// ------------------------------------------------------------------------------
	@FXML void deleteRow()
	{
		if (verbose) System.out.println("deleteRow");
		Object selectedItem = tableView.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
		{
			ObservableList<Rect> items = tableView.getItems();
			items.remove(selectedItem);
//			tableView.getSelectionModel().select(0);
//			tableView.setItems(null);
			tableView.setItems(items);
			if (verbose) System.out.println("items has size of " + items.size());
			install();
			tableView.requestFocus();
		}	
	}

	// this isn't a control, but a collection of nodes that need coordination
	/*
	 *   *  {@link CornerDragBox}.
	     *
	     * @param a BindingsController and 4 labels
	 	* CornerDragBox is a StackPane that aligns a rectangle with 4 Labels
	 */
	static int MAXWidth = 140;
	private class CornerDragBox extends StackPane
	{
		BindingsController controller;
		boolean verbose = false;
		double viewToModelRatio = 1.;
		Label widthLabel, heightLabel, areaLabel, scaleLabel;
		
		public CornerDragBox(BindingsController c, Label w, Label h, Label ar, Label scle)
		{
			super();
			controller = c;
			widthLabel = w;
			heightLabel = h; 
			areaLabel = ar; 
			scaleLabel = scle;
	        addRectHandlers();
	        setStyle("-fx-background-color: blue;");

	        getChildren().add(areaLabel);
	        StackPane.setAlignment(ar, Pos.CENTER);
	        
	        Rectangle handle = new Rectangle(12,12);  
	        handle.setStyle("-fx-fill:white;");
	        getChildren().add(handle);
	        setBorder(Borders.dashedBorder);
	         StackPane.setAlignment(handle, Pos.BOTTOM_RIGHT);
		}
		double MAX_RECT_WIDTH = 140;
		double MAX_RECT_HEIGHT = 140;
		
		//------------------------------------------------------------------------------
		//  OLD SCHOOL:  can this be replaced with bindings?
		
		public void install(Rect r)
		{
			Color col = r.getColor();
	        setBackground(Backgrounds.colored(col));

	        widthLabel.setText(r.getWidthAndUnits());
			heightLabel.setText(r.getHeightAndUnits());
			areaLabel.setText(r.getAreaAndUnits());
			if (verbose)		System.out.println(r.toString());

			// set the scale and aspect ratio of the rect
			double w = r.getWidthInMeters();
			double h = r.getHeightInMeters();
			double scaleW = MAX_RECT_WIDTH / w;
			double scaleH = MAX_RECT_HEIGHT / h;
			double scale  = Math.min(scaleW, scaleH);		
			NodeUtil.forceWidth(this, (int)(w * scale));  
			NodeUtil.forceHeight(this, (int)(h * scale));  
			
			String dat = DateTimeFormatter.ISO_DATE.format(r.getDueDate());
			scaleLabel.setText(String.format("Scale: %.1f,   Due Date: %s", scale, dat));
		}
		
		//------------------------------------------------------------------------------
		// Display rect mouse handlers
		//------------------------------------------------------------------------------
		private void addRectHandlers()
		{
			setOnMousePressed(ev -> {
				Rect active = controller.getActiveRecord();
				double modelWidth = active.getWidthInMeters();
				viewToModelRatio = getWidth()  / modelWidth;		// getWidthInInches
			});
			setOnMouseDragged(ev -> doDrag(ev));
			setOnMouseReleased(ev -> {});
		}
		
		double pin(double x, double floor, double top)	{		return Math.min(Math.max(floor, x), top);	}
		
		private void doDrag(MouseEvent e)
		{
			if (verbose) System.out.println("doDrag: " + e.getX() + ", " + e.getY());

			double x = pin(e.getX(), 1, MAXWidth);
			double y = pin(e.getY(), 1, MAXWidth);;
			NodeUtil.forceWidth(this, (int)x);  
			NodeUtil.forceHeight(this, (int) y);  

			Rect active = controller.getActiveRecord();
			double wid = Math.max(0.0001, x) / viewToModelRatio;
			active.setWidthInMeters(wid );
			double ht = Math.max(0.0001,y) / viewToModelRatio;		// don't let width or height go negative
			active.setHeightInMeters(ht);	
			active.recalcArea();
			controller.setActiveRecord(active);			
		}
	}

}
