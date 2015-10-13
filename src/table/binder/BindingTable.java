package table.binder;

import java.time.LocalDate;

import com.sun.javafx.scene.control.skin.TableViewSkinBase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
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
		table.getProperties().put(TableViewSkinBase.RECREATE, Boolean.TRUE);

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
                        ColorPicker cp = (ColorPicker)evt.getSource();
                        Color cw = (Color)cp.getValue();
                        cw = cp.getValue();
                        table.getSelectionModel().select(getTableRow().getIndex());
        				int idx = table.getSelectionModel().getSelectedIndex();
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
		
		// width, height, area plus units in pairs
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
		colVal.setCellFactory(TextFieldTableCell.<Rect, Double> forTableColumn(new NumberColConverter()));
		colVal.getStyleClass().add("numeric");
		colVal.setOnEditCommit((CellEditEvent<Rect, Double> t) ->	{	getRect(t).setVal(prefix, t.getNewValue());		});
		
		colUnits.setCellValueFactory(new PropertyValueFactory<>(prefix +"Units"));
		colUnits.setCellFactory(col -> new ChoiceBoxTableCell(colUnits, units, true));
		colUnits.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->	{	getRectUnit(t).setUnits(prefix, t.getNewValue());});
	}
	
	private Rect getRect(CellEditEvent<Rect, Double> t) { return ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));}
	private Rect getRectUnit(CellEditEvent<Rect, Unit> t) { return ((Rect) t.getTableView().getItems().get(t.getTablePosition().getRow()));}

	public void select(int row)	{		table.getSelectionModel().select(row);		}
	public int getSelectedIndex()	{		return table.getSelectionModel().getSelectedIndex();			}
	public Object getSelectedItem()	{		return table.getSelectionModel().getSelectedItem();	}

	// hack to refresh table
	//http://stackoverflow.com/questions/11272395/javafx-2-1-2-2-tableview-update-issue
	public void refresh()
	{
		table.getColumns().get(0).setVisible(false);
		table.getColumns().get(0).setVisible(true);
	}

	public void select(Rect observable)
	{
		// TODO Auto-generated method stub
		
	}

	
}
