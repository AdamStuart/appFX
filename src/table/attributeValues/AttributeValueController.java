package table.attributeValues;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AttributeValue;
import model.RandomAttributeValueData;
import table.codeOrganizer.TreeTableModel;
import util.FileUtil;

public class AttributeValueController
{
	public static DataFormat avDataFormat = new DataFormat("AttributeValue Data");
	public static DataFormat TreeDataFormat = new DataFormat("Tree Data");

	@FXML	private ToggleGroup selectionGrp;
	@FXML	private RadioButton cellRadio, rowRadio;
	@FXML	private ResourceBundle resources;
	@FXML	private URL location;
	@FXML	private TextArea textArea;
	@FXML	private ImageView dropPane;
	@FXML	private TableColumn<AttributeValue, String> attributeCol;
	@FXML	private TableColumn<AttributeValue, String> valueCol;
	@FXML	private ListView<Integer> listView;
	@FXML	private TreeView<String> treeView;
	@FXML	private ComboBox<TableColumn<AttributeValue, ?>> colSelectCombo;			// a combo box to choose if row or cell selection is active
	@FXML	private TableView<AttributeValue> tableView;

	ObservableList<Integer> selectedIndexes = FXCollections.observableArrayList();

	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";

	static String missing(String s)	{		return String.format(ctrlStr, s, "AttributeValueFXML.fxml");	}

	@FXML	void initialize()
	{
		assert attributeCol != null : missing("attributeCol");
		assert valueCol != null : missing("valueCol");
		assert tableView != null : missing("tableView");
		assert dropPane != null : missing("dropPane");
		assert listView != null : missing("listView");
		assert treeView != null : missing("treeView");

		setupTreeAndTableView();
		listView.setItems(selectedIndexes); // ListView items bound with selection index property of tableview
		setupDropPane();
		
		// the radio buttons change property listener [Row/Cell] selection
		selectionGrp.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1)
			{
				if (t1 == cellRadio)
					setCellSelection();
				else
					setRowSelection();
			}
		});
		if (colSelectCombo != null)
			colSelectCombo.disableProperty().bind(cellRadio.selectedProperty().not());		// Stricting the Column selection - only when cellRadio is off
		setRowFactory();				// set the Row Factory of the table
		setRowSelection();				// Set row selection as default
	}
	
	private void setupTreeAndTableView()
	{
		treeView.setRoot(TreeTableModel.getCodeModuleRoot());
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
		setCellValueFactories(); // set cell value factories
		tableView.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView
		
		// change listview observable list
		tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{
			@Override public void onChanged(Change<? extends Integer> change) { selectedIndexes.setAll(change.getList()); }
		});

		// Setting the items for columns selection
		if (colSelectCombo != null)
		{
		colSelectCombo.setItems(tableView.getColumns());
		colSelectCombo.getItems().forEach(e -> setCellColumnSelection(e));

		// add listener and update of selection type
		colSelectCombo.valueProperty().addListener(new ChangeListener<TableColumn<AttributeValue, ?>>()
		{
			@Override
			public void changed(ObservableValue<? extends TableColumn<AttributeValue, ?>> ov, TableColumn<AttributeValue, ?> t, final TableColumn<AttributeValue, ?> t1)
			{
				if (t1 != null)
					if (cellRadio.isSelected())
						setCellSelection();
			}
		});

		// For showing the column name properly
		colSelectCombo.setConverter(new StringConverter<TableColumn<AttributeValue, ?>>()
		{
			@Override 	public String toString(TableColumn<AttributeValue, ?> t)			{				return t.getText();			}

			@Override 	public TableColumn<AttributeValue, ?> fromString(String string)
			{
				for (TableColumn<AttributeValue, ?> t : colSelectCombo.getItems())
					if (t.getText().equals(string))
						return t;
				return null;
			}
		});
		}

	}
	
	
private void setupDropPane()
{
	URL path = AppAttributeValueTable.class.getResource("/dragChecker/resources/DrapHere.png");
	if (path != null)
		dropPane.setImage(new Image(path.toExternalForm()));
//	Borders.wrap(dropPane).lineBorder().buildAll();			this is in ControlFX
	dropPane.setOnDragEntered(e ->
	{
		System.out.println("dragEntered ");
		InnerShadow shadow = new InnerShadow();
		shadow.setOffsetX(1.0);
		shadow.setColor(Color.web("#FF6666"));
		shadow.setOffsetY(1.0);
		dropPane.setEffect(shadow);
		// dropPane.setStyle(". -fx-fill: red");
		e.consume();
	});
	dropPane.setOnDragExited(e ->
	{
		dropPane.setEffect(null);
		e.consume();
	});
	dropPane.setOnDragOver(e ->
	{
		e.acceptTransferModes(TransferMode.ANY);
		e.consume();
	});
	dropPane.setOnDragDropped(e ->
	{
		Dragboard db = e.getDragboard();
        e.acceptTransferModes(TransferMode.ANY);
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasFiles())
		{
			if (FileUtil.hasXMLFiles(db))
				openXMLfiles(db);
			else textArea.appendText(FileUtil.readFiles(db) + "\n");
        }
		
		if (formats.size() > 0)
		{
			DataFormat f = formats.iterator().next();
			Object p = db.getContent(f);
			if (f != null  && p != null)
			{
				System.out.println("format = " + f.toString());
				String urlStr = p.toString();
				StringBuffer buffer = new StringBuffer(urlStr);
				if (e.getGestureSource() instanceof TableView)
				{
					TableView<AttributeValue> tv = (TableView<AttributeValue>) e.getGestureSource();
					ObservableList<Integer> items = tv.getSelectionModel().getSelectedIndices();
					items.forEach(item -> buffer.append(stringFromItem(tv, item)).append("; "));
					String finalStr = chopOff(buffer.toString(), 2);
					textArea.appendText(finalStr + "\n");
				}
				
				if (e.getGestureSource() instanceof TreeView)
				{
					TreeView<String> tv = (TreeView<String>) e.getGestureSource();
					ObservableList<Integer> items = tv.getSelectionModel().getSelectedIndices();
					items.forEach(item -> buffer.append(item).append(", "));
					String finalStr = chopOff(buffer.toString(), 2);
					textArea.appendText(finalStr + "\n");
				}
				
			}
		}
		e.consume();
	});
	
}
	private void openXMLfiles(Dragboard db)
	{
		Objects.requireNonNull(db);
		db.getFiles().stream()
			.filter(f -> FileUtil.isXML(f))
			.forEach(f-> FileUtil.openXMLfile(f)) ;
	}


	private String stringFromItem(TableView<AttributeValue> table, Integer idx)
	{
		return table.getItems().get(idx).makeString();
	}

	private String chopOff(String s, int nChars)
	{
		int newLen = s.length() - nChars;
		return (newLen < 0) ? "" : s.substring(0, newLen);
	}
	
	private void setCellValueFactories()
	{
		attributeCol.setCellValueFactory(new PropertyValueFactory("attribute"));
		valueCol.setCellValueFactory(new PropertyValueFactory("value"));
		treeView.setOnDragDetected(e -> startDrag(e));
	}
	public static final DataFormat TREE_TABLE_FORMAT = new DataFormat("TreeTableItem");
	private void startDrag(MouseEvent e)
	{
		Dragboard db = treeView.startDragAndDrop(TransferMode.COPY);
		ClipboardContent content = new ClipboardContent();
		int idx = treeView.getSelectionModel().getSelectedIndex();
		String av = "";
		if (idx >= 0)
		{
			TreeItem<String> item = treeView.getTreeItem(idx);
			if (item != null)
			{
				av = item.getValue() + ": " + idx;
			}
				
		}
//		String av = treeView.getTreeItem(idx).getValue().toString() + ": " + idx;
//		content.clear();
		content.put(TREE_TABLE_FORMAT, av);
		db.setContent(content);
		e.consume();

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
					if (rowRadio.isSelected())		// only drag full rows
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
					@Override
					protected void updateItem(Object t, boolean bln)
					{
						super.updateItem(t, bln);
						if (t != null)
						{
							setText(t.toString());
						}
					}
				};

				cell.setOnDragEntered(t ->	{	setSelection(cell, col);	});

				cell.setOnDragDetected(t ->
				{
					if (cellRadio.isSelected() && colSelectCombo.getValue() == col)
					{
						Dragboard db = cell.getTableView().startDragAndDrop(TransferMode.COPY);
						ClipboardContent content = new ClipboardContent();
						content.put(avDataFormat, "XData");
						db.setContent(content);
						setSelection(cell, col);
						t.consume();
					}
				});
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
		TableViewSelectionModel<?> selMod = tableView.getSelectionModel();
		int idx = cell.getIndex();
		if (rowRadio.isSelected())
			if (cell.isSelected())
				selMod.clearSelection(idx);
			else
				selMod.select(idx);
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
		if (cellRadio.isSelected())
		{	
			if (cell.isSelected())	selMod.clearSelection(idx, col);
			 else					selMod.select(idx, col);
		}
	}

}
