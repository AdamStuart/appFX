package plate;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import model.AttributeValue;
import model.RandomAttributeValueData;


public class PlateController implements Initializable {

	@FXML private void plateView()			{		System.out.println("PlateView");		}
	@FXML private void tableViewBtn()		{		System.out.println("tableView");		}
	@FXML private void saveSelection()		{		System.out.println("saveSelection");		}
	
	@FXML private ListView sets;
	@FXML private ListView attributes;
	@FXML private TreeTableView analysisTable;
	
	@FXML private Button temp1;
	@FXML private Button temp2;
	@FXML private Button saveSelection;
	@FXML private Button plateView;
	@FXML private Button tableViewBtn;
	@FXML private GridPane plate;

	@FXML	private TableColumn<AttributeValue, String> attributeCol;
	@FXML	private TableColumn<AttributeValue, String> valueCol;
	@FXML	private TableView<AttributeValue> tableView;
	@FXML	private TreeTableColumn<String, String> treeTablePopulation;
	@FXML	private TreeTableColumn<String, String> treeTableCount;
	@FXML	private TreeTableColumn<String, String> treeTableName;
	@FXML	private TreeTableColumn<String, String> treeTableDate;

	public static DataFormat avDataFormat = new DataFormat("AttributeValue Data");

	
	public GridPane getPlate() {		return plate;	}
	
	public PlateController()
	{
	}
	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";
	static String missing(String s)	{		return String.format(ctrlStr, s, "PlateDesigner.fxml");	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		assert attributeCol != null : missing("attributeCol");
		assert valueCol != null : missing("valueCol");
		assert tableView != null : missing("tableView");
		
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
		setCellValueFactories(); // set cell value factories
		tableView.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView

//		tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
//		{
//			@Override public void onChanged(Change<? extends Integer> change) {  }   //selectedIndexes.setAll(change.getList());
//		});
//
		setRowFactory();				// set the Row Factory of the table
		setRowSelection();				// Set row selection as default
//		analysisTable.setRoot(TreeTableModel.getTreeRoot());
	}
	
	private void setCellValueFactories()
	{
		attributeCol.setCellValueFactory(new PropertyValueFactory("attribute"));
		valueCol.setCellValueFactory(new PropertyValueFactory("value"));
		
		 //Creating a column
//        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
//        column.setPrefWidth(150);   
		
//		treeTablePopulation.setCellValueFactory(CellDataFeatures<String, String> p) {
//            public ObservableValue call(CellDataFeatures p) {
//                // p.getValue() returns the TreeItem instance for a particular TreeTableView row,
//                // p.getValue().getValue() returns the Person instance inside the TreeItem
//                return p.getValue().getValue().populationProperty();
//            }
//         });
//       

	
        //Defining cell content
//        treeTablePopulation.setCellValueFactory((CellDataFeatures<TreeTableView, TreeTableColumn> p) -> 
//            new ReadOnlyStringWrapper(p.getValue().getValue()));  

	}
	
	/**
	 * Change the cell selection boolean value of TableView
	 */
	public void setRowSelection()
	{
		tableView.getSelectionModel().clearSelection();
		tableView.getSelectionModel().setCellSelectionEnabled(false);
	}

	/**
	 * Change the cell selection boolean value of TableView
	 */
	public void setCellSelection()
	{
		tableView.getSelectionModel().clearSelection();
		tableView.getSelectionModel().setCellSelectionEnabled(true);
	}

	/**
	 * Set Row Factory for the TableView
	 */
	public void setRowFactory()
	{
		tableView.setRowFactory(new Callback<TableView<AttributeValue>, TableRow<AttributeValue>>()
		{
			@Override public TableRow<AttributeValue> call(TableView<AttributeValue> p)
			{
				final TableRow<AttributeValue> row = new TableRow<AttributeValue>();
				row.setOnDragEntered(t ->	{	setSelection(row);	});

				row.setOnDragDetected(t ->
				{
					//if (rowRadio.isSelected())		// only drag full rows
					{
						Dragboard db = row.getTableView().startDragAndDrop(TransferMode.COPY);
						ClipboardContent content = new ClipboardContent();
						AttributeValue av = row.getItem();
						String s = av.makeString();
						content.put(avDataFormat, s);
						db.setContent(content);
						setSelection(row);
						t.consume();
					}
				});
				return row;
			}
		});
	}
	/**
	 * This function helps to make the Cell Factory for specific TableColumn
	 *
	 * @param col
	 */
	public void setCellColumnSelection(final TableColumn col)
	{
		col.setCellFactory(new Callback<TableColumn<AttributeValue, ?>, TableCell<AttributeValue, ?>>()
		{
			@Override
			public TableCell<AttributeValue, ?> call(TableColumn<AttributeValue, ?> p)
			{
				final TableCell cell = new TableCell()
				{
					@Override protected void updateItem(Object t, boolean bln)
					{
						super.updateItem(t, bln);
						if (t != null)
							setText(t.toString());
					}
				};

				cell.setOnDragEntered(t ->	{	setSelection(cell, col);});

				return cell;

			}
		});
	}
	/**
	 * For the changes on table cell selection used only on the TableCell
	 * selection mode
	 *
	 * @param cell
	 */
	private void setSelection(IndexedCell<?> cell)
	{
		TableViewSelectionModel selMod = tableView.getSelectionModel();
		int idx = cell.getIndex();
		if (cell.isSelected())		selMod.clearSelection(idx);
		else						selMod.select(idx);
	}
	/**
	 * For the changes on the table row selection used only on TableRow
	 * selection mode
	 *
	 * @param cell
	 * @param col
	 */
	private void setSelection(IndexedCell cell, TableColumn col)
	{
		TableViewSelectionModel selMod = tableView.getSelectionModel();
		int idx = cell.getIndex();
		if (cell.isSelected())	selMod.clearSelection(idx, col);
		else					 selMod.select(idx, col);
	}
}
