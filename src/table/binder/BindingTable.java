package table.binder;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import model.Unit;
import table.binder.tablecellHelpers.ChoiceBoxTableCell;
import table.binder.tablecellHelpers.DateTableCell;
import table.binder.tablecellHelpers.NumberColConverter;

// There is so much code that goes into initializing a table that
// I abstracted it out of BindingController.  This class is not a 
// subclass of TableView, but a wrapper class that does initialization
// on the table and columns

public class BindingTable
{
	private BindingsController controller;
	private TableView<Rect> table;
	
//  This is the array we get from the controller.  
//		public TableColumn[] getCols()	{ return new TableColumn[] {
//					selectedCol, colorCol, widthCol, widthUnitsCol, 
//					heightCol, heightUnitsCol, areaCol, areaUnitsCol,heightCol, dueDateCol}; }
	
	public BindingTable(BindingsController bindingsController)
	{
		controller = bindingsController;
		table = controller.getTableView();
		table.setEditable(true);
		table.getSelectionModel().
			selectedIndexProperty().addListener((a,b,c) -> 	{ controller.install();});

		// didn't work for me.  See ref @ BindingsController.116
		// table.getProperties().put(TableViewSkinBase.RECREATE, Boolean.TRUE);

		TableColumn[] cols = controller.getCols();		// 9  columns defined in the FXML
		
		// checkbox
		TableColumn<Rect, Boolean> selectedCol = cols[0];
		selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
		selectedCol.setCellValueFactory(new PropertyValueFactory<>("selected"));

		// color popup
		TableColumn<Rect, Color> colorCol = cols[1];
		colorCol.setEditable(true);
		colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
		colorCol.setCellFactory((t) -> {    return new TableCell<Rect, Color>(){
            private ColorPicker colorPicker;
            private ColorPicker createPicker()
            {
                colorPicker = new ColorPicker();
                colorPicker.setOnAction(evt -> {
                		boolean wasSelected = getTableRow().getIndex() == 90;
                        ColorPicker cp = (ColorPicker)evt.getSource();
                        Color cw = (Color)cp.getValue();
                        cw = cp.getValue();
                        select(getTableRow().getIndex());
        				int idx = getSelectedIndex();
        				if (idx >= 0)
        					table.getItems().get(idx).setColor(cw);
                });
                return colorPicker;
            }

            @Override  protected void updateItem(Color value, boolean empty) 
            {                      
                super.updateItem(value, empty);
                if(empty){   setGraphic(null);  return;}		//http://stackoverflow.com/questions/25532568/javafx-tableview-delete-issue
                if(colorPicker == null){
                    colorPicker = createPicker();
                    colorPicker.setUserData(value);
                }
                colorPicker.setValue(value);
                setGraphic(colorPicker);
            }
        	};
	    });
		
		// initialize the width, height, area plus units in pairs
		setUpCols("width", cols[2], cols[3]);
		setUpCols("height", cols[4], cols[5]);
		setUpCols("area", cols[6], cols[7]);
			
		// date picker
		TableColumn<Rect, LocalDate> dueDateCol = cols[8];
		dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setCellFactory(p -> { return new DateTableCell<Rect>();     });
        dueDateCol.setOnEditCommit(event -> event.getRowValue().setDueDate(event.getNewValue()));
        dueDateCol.setEditable(true);
	       
	}
	// ------------------------------------------------------------------------------
	// macros to customize our value and unit columns in the table, in pairs
	
	static ObservableList<Unit> units = FXCollections.observableArrayList(Unit.values());
	
	private void setUpCols(String prefix, TableColumn<Rect, Double> colVal, TableColumn<Rect, Unit> colUnits)
	{
		colVal.setCellValueFactory(new PropertyValueFactory<>(prefix +"Val"));

		
		Callback<TableColumn<Rect, Double>, TableCell<Rect, Double>> factory = TextFieldTableCell.<Rect, Double> forTableColumn(new NumberColConverter());

		TableCell<Rect, Double>	cell = factory.call(colVal);

		cell.addEventFilter(KeyEvent.KEY_TYPED, event ->
		{	
			System.out.println("KEY_TYPED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
		cell.addEventFilter(KeyEvent.KEY_PRESSED, event ->
		{	
			System.out.println("KEY_PRESSED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
		cell.addEventFilter(KeyEvent.KEY_RELEASED, event ->
		{	
			System.out.println("KEY_RELEASED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
	
		colVal.setCellFactory(factory);
		colVal.getStyleClass().add("numeric");
		colVal.setOnEditCommit((CellEditEvent<Rect, Double> t) ->	{	getRect(t).setVal(prefix, t.getNewValue());		});
		
		colUnits.setCellValueFactory(new PropertyValueFactory<>(prefix +"Units"));
		colUnits.setCellFactory(col -> new ChoiceBoxTableCell(colUnits, units, true));
		colUnits.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->	{	getRectUnit(t).setUnits(prefix, t.getNewValue());});
	}
	
	private Rect getRect(CellEditEvent<Rect, Double> t) { return ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));}
	private Rect getRectUnit(CellEditEvent<Rect, Unit> t) { return ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));}

	public boolean isSelected(int row) {	return	table.getSelectionModel().isSelected(row);		}
	public void select(int row)		{		table.getSelectionModel().select(row);		}
	public void select(Rect r)		{		table.getSelectionModel().select(r);			}
	public int getSelectedIndex()	{		return table.getSelectionModel().getSelectedIndex();			}
	public Object getSelectedItem()	{		return table.getSelectionModel().getSelectedItem();	}

	// hack to refresh table
	//http://stackoverflow.com/questions/11272395/javafx-2-1-2-2-tableview-update-issue
	public void refresh()
	{
		table.getColumns().get(0).setVisible(false);
		table.getColumns().get(0).setVisible(true);
	}


	
}
