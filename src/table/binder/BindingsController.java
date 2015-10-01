package table.binder;

import java.time.LocalDate;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import table.binder.tablecellHelpers.DateTableCell;
import table.binder.tablecellHelpers.NumberColConverter;

public class BindingsController implements InvalidationListener
{

	@FXML private SimpleDoubleProperty rectScale = new SimpleDoubleProperty(1.);

	@FXML private VBox threeLineContainer;

	@FXML private Label widthDisplay; // axis of rectDisplay
	@FXML private Label heightDisplay; // "
	private Label areaDisplay; // label centered in rectDisplay
	@FXML private Label scaleDisplay; // the scale to get current size to fit, we also show the date field here
	@FXML private VBox dragboxContainer;

	
	// ------------------------------------------------------------------------------

	// specs
	// if area is edited, adjust width and height to maintain aspect ratio
	// if a unit is changed immediately after the value, dont recalc value
	// ------------------------------------------------------------------------------

	boolean verbose = false;

	ValueWithUnitsBox widthLine;
	ValueWithUnitsBox heightLine;
	ValueWithUnitsBox areaLine;
	CornerDragBox dragbox;

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

		initTable();
		createData();
		TableViewSelectionModel<Rect> selModel = tableView.getSelectionModel();
//		if (tableView.getItems().size() > 0)
//			selModel.select(0);

	}

	// ------------------------------------------------------------------------------
	public Rect getActiveRecord()	{		return getActiveRect();	}
	public void setUnits(String id, Unit un)	{		getActiveRecord().setUnits(id, un);	}
	public void setValue(String id, double d)	{		getActiveRecord().setVal(id, d);	}

	// ------------------------------------------------------------------------------
	Rect getActiveRect()
	{
		Object selectedItem = tableView.getSelectionModel().getSelectedItem();
		if (selectedItem == null)
		{
			if (tableView.getItems().isEmpty())
				tableView.getItems().add(new Rect());
System.out.println("items: " + tableView.getItems().size());
			tableView.getSelectionModel().select(0);
			System.out.println("selected: " + tableView.getSelectionModel().getSelectedIndex());
			selectedItem = tableView.getSelectionModel().getSelectedItem();
		}
		return (Rect) selectedItem;
	}
	// ------------------------------------------------------------------------------
	//	key method to move data from the model (getActiveRecord()) to the view components
	
	public void install(double rectScale)
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

		dragbox.install(r, rectScale);

		int idx = tableView.getSelectionModel().getSelectedIndex();
		if (idx >= 0)
		try
		{
			tableView.getItems().set(idx, r);
		}
		catch (UnsupportedOperationException e)  {}
		if (verbose)
			System.out.println("install " + tableView.getItems().indexOf(r));

	}

	
	// ------------------------------------------------------------------------------
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

	private void initTable()
	{

		colorCol.setEditable(true);
		colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
//		colorCol.setCellFactory(p -> { return new ColorTableCell<Rect>();     });
		
		colorCol.setCellFactory(new Callback<TableColumn<Rect, Color>, TableCell<Rect, Color>>() {          

	        @Override public TableCell<Rect, Color> call(TableColumn<Rect,Color> arg0) {
	            return new TableCell<Rect, Color>(){

	                private ColorPicker colorPicker;

	                private ColorPicker createPicker()
	                {
	                    colorPicker = new ColorPicker();
	                    colorPicker.setOnAction(evt -> {
	                       
	                            ColorPicker cp = (ColorPicker)evt.getSource();
	                            Color cw = (Color)cp.getValue();
	                            cw = cp.getValue();
	            				tableView.getSelectionModel().select(getTableRow().getIndex());
	            				int idx = tableView.getSelectionModel().getSelectedIndex();
	            				if (idx >= 0)
	            					tableView.getItems().get(idx).setColor(cw);
	                    });
	                    return colorPicker;
	                }


	                @Override
	                protected void updateItem(Color value, boolean empty) {                      
	                    super.updateItem(value, empty);
	                    if(empty){
	                      setGraphic(null);  return;			//http://stackoverflow.com/questions/25532568/javafx-tableview-delete-issue
	                    }

	                    if(colorPicker == null){
	                        colorPicker = createPicker();
	                        colorPicker.setUserData(value);
	                    }

	                    colorPicker.setValue(value);
	                    setGraphic(colorPicker);
	                }
	            };
	        }            
	    });
		

        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setCellFactory(p -> { return new DateTableCell<Rect>();     });
        dueDateCol.setOnEditCommit(event -> event.getRowValue().setDueDate(event.getNewValue()));
        dueDateCol.setEditable(true);
       
		widthCol.setOnEditCommit((CellEditEvent<Rect, Double> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setVal("width", t.getNewValue());
		});
		widthCol.setCellFactory(TextFieldTableCell.<Rect, Double> forTableColumn(new NumberColConverter()));
		widthCol.setCellValueFactory(new PropertyValueFactory<>("widthVal"));
		widthCol.getStyleClass().add("numeric");
		widthUnitsCol.setCellValueFactory(new PropertyValueFactory<>("widthUnits"));
		ObservableList<Unit> units = FXCollections.observableArrayList(Unit.values());
		widthUnitsCol.setCellFactory(col -> new ChoiceBoxTableCell(widthUnitsCol, units));
		widthUnitsCol.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setUnits("width", t.getNewValue());
		});

		heightCol.setCellValueFactory(new PropertyValueFactory<>("heightVal"));
		heightCol.getStyleClass().add("numeric");
		heightCol.setCellFactory(TextFieldTableCell.<Rect, Double> forTableColumn(new NumberColConverter()));
		heightCol.setOnEditCommit((CellEditEvent<Rect, Double> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setVal("height", t.getNewValue());
		});

		heightUnitsCol.setCellValueFactory(new PropertyValueFactory<>("heightUnits"));

		heightUnitsCol.setCellFactory(col -> new ChoiceBoxTableCell(heightUnitsCol, units));
		heightUnitsCol.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setUnits("height", t.getNewValue());
		});

		areaCol.setCellValueFactory(new PropertyValueFactory<>("areaVal"));
		areaCol.setCellFactory(TextFieldTableCell.<Rect, Double> forTableColumn(new NumberColConverter()));
		areaCol.getStyleClass().add("numeric");
		areaCol.setOnEditCommit((CellEditEvent<Rect, Double> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setVal("area", t.getNewValue());
		});

		areaUnitsCol.setCellValueFactory(new PropertyValueFactory<>("areaUnits"));
		areaUnitsCol.setCellFactory(col -> new ChoiceBoxTableCell(areaUnitsCol, units, true));
		areaUnitsCol.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->
		{
			Rect r = ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));
			r.setUnits("area", t.getNewValue());
		});

		tableView.setEditable(true);
		selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
		selectedCol.setCellValueFactory(new PropertyValueFactory<>("selected"));

		tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
			{	tableSelectionChanged((Rect) oldValue, (Rect) newValue);	}
		);


	}
	// ------------------------------------------------------------------------------
	private void createData()
	{
		Rect[] deflts = new Rect[] { new Rect(10, Unit.CM, 10, Unit.CM, Color.BROWN),
						new Rect(10, Unit.CM, 2, Unit.CM, Color.GREEN), 
						new Rect(1, Unit.IN, 2, Unit.IN, Color.CORAL),
						new Rect(100.5, Unit.M, 132, Unit.M, Color.BLACK) };

		ObservableList<Rect> data = FXCollections.observableArrayList();
		for (Rect r : deflts)
			data.add(r);
		tableView.setItems(data);
	}

	// ------------------------------------------------------------------------------
	private void tableSelectionChanged(Rect oldValue, Rect newValue)
	{
		if (newValue != null)
		{
			newValue.setController(this);
//			install(newValue);
//			install(-1);		this makes a cyclic call.  crashes slowly with no good stack diagnostic
		}
	}
	// ------------------------------------------------------------------------------
	@Override public void invalidated(Observable observable)
	{
		if (observable instanceof Rect)
			tableView.getSelectionModel().select((Rect) observable);
	}
	// ------------------------------------------------------------------------------
	@FXML private Button addRowBtn; 		// only injected in case we want to disable them
	@FXML private Button deleteRowBtn; 		// under certain conditions (eg: tableView.getItems().size() > max)
	// ------------------------------------------------------------------------------
	@FXML void addRow()
	{
		if (verbose) System.out.println("addRow");
		ObservableList<Rect> items = tableView.getItems();
		items.add(new Rect());
		tableView.setItems(null);
		tableView.setItems(items);
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
			tableView.setItems(null);
			tableView.setItems(items);
		System.out.println("items has size of " + items.size());

		try
		{
			install(0);
		}
		catch (Exception e) 		{ e.printStackTrace();}
		}
	}	

}
