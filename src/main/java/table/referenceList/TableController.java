package table.referenceList;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import gui.Action.ActionType;
import gui.DraggableTableRow;
import gui.DropUtil;
import gui.UndoStack;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.IController;
import model.bio.Species;
import model.bio.TableRecord;

abstract public class TableController<ROWTYPE extends Map> implements IController, Initializable {

	abstract protected void processFile(File f);
	abstract protected void createTableRecord();

	public DataFormat getMimeType() {		return DataFormat.PLAIN_TEXT;	}
	protected void doSearch() {			}		// default is to do nother

	protected TableRecord<ROWTYPE> tableRecord = null;
	
	@FXML protected ChoiceBox<String> species;
	@FXML protected Button search;
	@FXML protected TextField searchBox;
	@FXML protected BorderPane container;
	@FXML protected TableView<TableColumn> columnTable;
	@FXML protected TableColumn<TableColumn, Text> typeColumn;
	@FXML protected TableColumn<TableColumn, String> colNameColumn;
	@FXML protected TableColumn<TableColumn, Double> widthColumn;
	@FXML protected TableView<ROWTYPE> theTable;
	private  UndoStack undoStack;

	public static final DataFormat  COLUMN_MIME_TYPE = new DataFormat("application/x-java-index-of-column");
	@Override public void initialize(URL location, ResourceBundle resources) {
		createTableRecord();
		undoStack = new UndoStack(this, null);
	
		IController.setGraphic(westSidebar, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		new BorderPaneAnimator(container, westSidebar, Side.LEFT, false, 250);
		westSidebar.fire();		// start with columns hidden
		//-------
		typeColumn.setSortable(false);
		colNameColumn.setSortable(false);
		widthColumn.setSortable(false);
//		typeColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, Icon>("type"));
		colNameColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("text"));
		widthColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, Double>("width"));

		widthColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
		typeColumn.setStyle( "-fx-alignment: CENTER;");
		typeColumn.setCellValueFactory(new Callback<CellDataFeatures<TableColumn, Text>, ObservableValue<Text>>() {
			public ObservableValue<Text> call(CellDataFeatures<TableColumn, Text> p) {
				Object type = p.getValue().getUserData();
				Text txt = GlyphsDude.createIcon(FontAwesomeIcons.ADJUST);
				if (type == null)				txt = null;
				if (type instanceof String)		txt = typeToText(type);
				ReadOnlyObjectWrapper<Text> wrapper = new ReadOnlyObjectWrapper<Text>(txt);
				return wrapper;
			}
				private Text typeToText(Object type) {
					if (type == null) return new Text();
					String str = "";
					GlyphIcons icon = null;
					if (type instanceof String) str = (String) type;
					
					if ("T".equals(str)) icon = FontAwesomeIcons.ITALIC;
					else if ("N".equals(str)) icon = FontAwesomeIcons.DOT_CIRCLE_ALT;
					else if ("B".equals(str)) icon = FontAwesomeIcons.CHECK;
					else if ("D".equals(str)) icon = FontAwesomeIcons.CLOCK_ALT;
					else if ("C".equals(str)) icon = FontAwesomeIcons.EYEDROPPER;
					else if ("E".equals(str)) icon = FontAwesomeIcons.ADJUST;
					else if ("L".equals(str)) icon = FontAwesomeIcons.ALIGN_JUSTIFY;
					else if ("U".equals(str)) icon = FontAwesomeIcons.ANCHOR;
					
					if (icon == null) return new Text();
					return GlyphsDude.createIcon(icon, "12");
				}
			  });

//		typeColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("type"));
//		typeColumn.setCellValueFactory(new Callback<CellDataFeatures<TableColumn, String>, ObservableValue<String>>() {
//			public ObservableValue<String> call(CellDataFeatures<TableColumn, String> p) {
//				Object type = p.getValue().getUserData();
//				String str = "?";
//				if (type == null)				str = "";
//				if (type instanceof String)		str = (String) type;
//				ReadOnlyObjectWrapper wrapper = new ReadOnlyObjectWrapper<String>(str);
//				return wrapper;
//			}
//
//				private String typeToChar(Object type) {
//					if (type == null) return "";
//				if (type instanceof String) return (String) type;
//					return "?";
//				}
//			  });
//		typeColumn.setCellValueFactory(c);
			columnTable.setRowFactory((a) -> {
			  return new DraggableTableRow<TableColumn>(columnTable, COLUMN_MIME_TYPE, this, tableRecord);
		  });
		columnTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// inherit table with columnEditing
		
		if (species != null)
		{
			// TODO make species control
			String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
			species.getItems().addAll(organisms);
			species.getSelectionModel().select(1);
		}
		if (search != null)
		{
			search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
			search.setText("");
			searchBox.setText("");
			searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> 
			{  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		}
		DropUtil.makeFileDropPane(theTable, e -> { doFileDrop(e); });
		String url = "";				// TODO
	//	findGenes(url);
		columnTable.getItems().addListener(resetColumnOrder);
		theTable.getColumns().addListener(resetColumnTable);
	}	

//	Callback c = new Callback< TableColumn.CellDataFeatures<TableColumn.CellDataFeatures, ObservableValue>, TableCell<TableColumn.CellDataFeatures, ObservableValue>>() 
//	{
//		@Override public TableCell<TableColumn.CellDataFeatures, ObservableValue> call(TableColumn.CellDataFeatures<TableColumn.CellDataFeatures, ObservableValue> param)
//		{ 
//			return tableCell;
//			}
//	};
//	
//	TableCell<TableColumn.CellDataFeatures, ObservableValue> tableCell = new TableCell<TableColumn.CellDataFeatures, ObservableValue>() {
//		@Override public void updateItem(ObservableValue s, boolean empty)
//		{
//			if (s != null)
//			{
//				setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.ADJUST));
//			}
//		}
//	};
//	
//	Callback< TableColumn<?, String>, TableCell<?, String>> factory = new CellValueFactory< ?, String>, TableCell<?, String>>() 
//	{
//		@Override public TableCell<?, String> call(TableColumn<?, String> param)	{ return tableCell;}
//	};
	
	public void doFileDrop(DragEvent e)
	{
		getUndoStack().push(ActionType.New);
		Dragboard db = e.getDragboard();
		importing = true;
		for (File f : db.getFiles())
			processFile(f);
		e.consume();
		resetTableColumns();
		importing = false;
	}

	private UndoStack getUndoStack() {
		
		return undoStack;
	}

	@FXML private Button westSidebar;
	private TableColumn<ROWTYPE, String> separatorColumn = new TableColumn<ROWTYPE, String>();
	protected List<TableColumn<ROWTYPE, ?>> allColumns = new ArrayList<TableColumn<ROWTYPE, ?>>();
	protected void makeSeparatorColumn() {
		allColumns.add(separatorColumn);  
		separatorColumn.setPrefWidth(0);  
		separatorColumn.setMaxWidth(0);  
		separatorColumn.setText("--------");		// TODO horizontal line in TableCell
	}
	public Species getSpecies() {
		String name = species.getSelectionModel().getSelectedItem();
		return Species.lookup(name);
	}

	public void  dumpColumns()
	{
		for (TableColumn  col : allColumns)
			System.out.println(col.getUserData() + " " + col.getText() + String.format(" %4.0f", col.getWidth()));
	}
	
	public void resetTableColumns() {
		System.err.println("resetTableColumns");
		dumpColumns();
		columnTable.getItems().clear();
		columnTable.getItems().addAll(allColumns);
		theTable.getColumns().clear();	
		for (TableColumn col : getVisColumns())
			theTable.getColumns().add(col);	
	}
	public List<TableColumn> getVisColumns()
	{
		List<TableColumn> columns = new ArrayList<TableColumn>();
		for (TableColumn col : allColumns)
		{
			String s = col.getText();
			if (s.startsWith("---")) break;
			columns.add(col);
		}
		return columns;
	}

	public TableColumn<ROWTYPE, ?> findColumnContains(String name) {
		String NAME = name.toUpperCase();
		for (TableColumn<ROWTYPE, ?> column : allColumns)
			if (column.getText().toUpperCase().contains(NAME))
				return column;
		return null;
	}


	public TableColumn<ROWTYPE, ?> findColumn(String name) {
		for (TableColumn<ROWTYPE, ?> column : allColumns)
			if (column.getText().equals(name))
				return column;
		return null;
	}
	protected boolean importing = false;

	ListChangeListener<TableColumn> resetColumnOrder = new ListChangeListener<TableColumn>()
	{
		@Override public void onChanged(Change<? extends TableColumn> c)	
		{
			if (allColumns == null) return;		// this gets called too early in initialization
			if (recursion > 0) return;
			if (importing) return;
			recursion++;
//			if (allColumns != null) return;		// this gets called too early in initialization
//			System.out.println("resetColumnOrder");
			theTable.getColumns().clear();
			List<TableColumn> vis = getVisColumns();
			for (TableColumn column : vis)
				theTable.getColumns().add(column);
			// readjust widths, etc
			recursion--;
		}

	};
	protected int findColumnIndexByName(List<TableColumn<ROWTYPE, ?>> cols, String name)
	{
		TableColumn<ROWTYPE, ?> column = findColumnByName(cols, name);
		return theTable.getColumns().indexOf(column);
	}

	protected TableColumn<ROWTYPE, ?> findColumnByName(List<TableColumn<ROWTYPE, ?>> cols, String name) 
	{
		for (TableColumn<ROWTYPE, ?> column : cols)
		{
			String txt = column.getText();
			if (txt.equals(name))
				return column;
			if (txt.equals("Label") && name.equals("Name"))
				return column;
		}
		return null;
	}
	int recursion = 0;
	
	
	ListChangeListener<TableColumn> resetColumnTable = new ListChangeListener<TableColumn>()
	{
		@Override public void onChanged(Change<? extends TableColumn> c)	
		{
			if (recursion > 0) return;
			recursion++;
//			System.out.println("resetColumnTable " + theTable.getColumns().size());
//			if (allColumns.size() < 3393) return;
			ObservableList<? extends TableColumn> list = theTable.getColumns();
			int index = 0;
			for (TableColumn col : list)
				setToIndex(col, index++);
			
			columnTable.getItems().clear();
			for (TableColumn column : allColumns)
				columnTable.getItems().add(column);
			recursion--;
		}
		
		public void setToIndex(TableColumn col, int newIndex)
		{
			int oldIndex = allColumns.indexOf(col);
			if (oldIndex >= 0)
			{
				TableColumn<ROWTYPE,?> tc = allColumns.get(oldIndex);
				allColumns.remove(tc);
				allColumns.add(newIndex, tc);

			}
		}
		
////			String[] colNames = getColumnNames(list);		// names of columns show in gene table
//			String[] colNames = new String[allColumns.size()];
//			for (int i=0; i<allColumns.size(); i++)
//				colNames[i] = allColumns.get(i).getText();
//			int idx = 0;
//			for (; idx < colNames.length; idx++)
//			{
//				int targetIdx = findColumnIndex(allColumns, colNames[idx]);
//				if (targetIdx > idx)
//				{
//					TableColumn<ROWTYPE,?> tc = allColumns.get(targetIdx);
//					allColumns.remove(tc);
//					allColumns.add(idx, tc);
//				}
//			}
//			if (allColumns.remove(separatorColumn))
//				allColumns.set(idx, separatorColumn);
//		}

//		int findColumnIndex(List<TableColumn<ROWTYPE,?>> cols, String t)
//		{
//			for (int i=0; i<cols.size(); i++)
//				if ( cols.get(i).getText().equals(t))
//					return i;
//			return -1;
//		}
	};
}
//		private List<String> getColumnNames(ObservableList<? extends TableColumn> list) {
//			List<String> strs = new ArrayList<String>();
//			for (int i=0; i<list.size(); i++)
//				if (list.get(i).isVisible())
//					strs.add(list.get(i).getText());
//			return strs;
//		}
//		
//	};

