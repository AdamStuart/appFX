package plate;

import gui.BorderPaneAnimator;
import icon.FontAwesomeIcons;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Transition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;
import model.AttributeValue;
import model.RandomAttributeValueData;
import threeD.Xform;
import util.DialogUtil;
import util.TableUtil;


public class PlateController implements Initializable {


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
		plate = getPlate();
		model = new PlateModel(this, plate);
		tableSetup();
	    protocolListSetup();
	    cssSetup();
	    installSidebars();
		world = new Xform();
		subscene3D = new Plate3D(model, world, 600, 400);  // plate.getWidth(), plate.getHeight());
//        subscene3D.setVisible(true);
	    gridstack.getChildren().addAll(subscene3D);
//	    gridstack.getChildren().add(new Label("HELLO"));
		attrText.setWrapText(true);		//in FXML
  
		doDesign();					// show the 2D editor, hide 3D

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

	@FXML private void plateView()			{		System.out.println("PlateView");		}
	@FXML private void tableViewBtn()		{		System.out.println("tableView");		}
	@FXML private void saveSelection()		
	{		System.out.println("saveSelection: " + model.getSelectedWellStr());		
	}
	
	@FXML private TreeTableView<String> protocolList;  
	@FXML private ListView sets;
	@FXML private ListView attributes;
	@FXML private TreeTableView analysisTable;
	
	@FXML private MenuItem saveSelection;
	@FXML private TextArea attrText;
	@FXML private Button addProtocolBtn;
	@FXML private Button temp1;
	@FXML private Button temp2;
	@FXML private Button saveSelBtn;
	@FXML private Button plateView;
	@FXML private Button tableViewBtn;
	@FXML private GridPane plate;
//	@FXML private SplitPane splitpane;
	@FXML private BorderPane borderpane;
	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
	@FXML private StackPane gridstack;

	@FXML private Button design;
	@FXML private Button plot;
	@FXML private Button config;
	
	@FXML	private TableColumn<AttributeValue, String> attributeCol;
	@FXML	private TableColumn<AttributeValue, String> valueCol;
	@FXML	private TableView<AttributeValue> tableView;
	@FXML	private TreeTableColumn<String, String> treeTablePopulation;
	@FXML	private TreeTableColumn<String, String> treeTableCount;
	@FXML	private TreeTableColumn<String, String> treeTableName;
	@FXML	private TreeTableColumn<String, String> treeTableDate;

	public static DataFormat avDataFormat = new DataFormat("AttributeValue Data");
	public static DataFormat avListDataFormat = new DataFormat("AttributeValue List");

	
	private PlateModel model;
	public GridPane getPlate() {		return plate;	}
	
	//-----------------------------------------------------------------------------------
	@FXML private void doDesign()
	{
		plate.setOpacity(0);  
		plate.setVisible(true); 
		subscene3D.setXAngle(150);
		Transition hider = new Transition() 
		{
			{ setCycleDuration(Duration.millis(500)); }
			protected void interpolate(double frac)         {   plate.setOpacity(frac);     	subscene3D.setOpacity(1.0 - frac);        };
		};
	      hider.onFinishedProperty().set(actionEvent -> {
	    	  subscene3D.setVisible(false);
	    	  plate.setVisible(true);          }  );
//	       plate.setVisible(false);
		   hider.play();
		   model.resetData();
		   
//	       plate.setVisible(true);
//	       plate.setOpacity(1);
//	       subscene3D.setVisible(false);
	}
	//-----------------------------------------------------------------------------------
//    final Xform axisGroup = new Xform();
//    final Xform moleculeGroup = new Xform();
//	private Group root;
    private Plate3D subscene3D; 
    private Xform world;
    //---------------------------------------------------------------------------------------------------
	@FXML private void doPlot()			// a compound transition from 2D to 3D view
	{
		subscene3D.setOpacity(0); 
		subscene3D.setScalar(0);  

		subscene3D.setVisible(true);  
		subscene3D.setWidth(plate.getWidth());
	    subscene3D.setHeight(plate.getHeight());
	    model.setData();			// random values 

		Transition hider = new Transition() 
		{
			{ setCycleDuration(Duration.millis(200)); }
			protected void interpolate(double frac)         	{   subscene3D.setOpacity(frac);     	plate.setOpacity(1.0 - frac);        };
		};
		Transition rotater = new Transition() 
		{
			{ setCycleDuration(Duration.millis(1000)); }
			protected void interpolate(double frac)       		{  subscene3D.setXAngle(150 + 25 * frac );   subscene3D.setDistance(Plate3D.CAMERA_INITIAL_DISTANCE - 200 * frac );    };
		};
	    Transition grower = new Transition() 
		{
			{ setCycleDuration(Duration.millis(500)); }
			protected void interpolate(double frac)      		{  subscene3D.setScalar(frac) ;       };
		};
			
	      hider.onFinishedProperty().set(actionEvent -> 
	      {
	    	  plate.setVisible(false);
	    	  subscene3D.setVisible(true);    rotater.play();      
	    	 }   );

	    rotater.onFinishedProperty().set(actionEvent -> { 	grower.play();      }  );
   
		hider.play();

	}
	//		plate.setRotationAxis(new Point3D(1,0,0));
//		plate.setRotate(45);
    //---------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------
	@FXML private void doConfig()
	{
	}
	//-----------------------------------------------------------------------------------
	@FXML private void addProtocol()
	{
		protocolList.getRoot().getChildren().add(new TreeItem("an example"));
	}
	//-----------------------------------------------------------------------------------
 

	private void cssSetup()
	{
	    URL css = getClass().getResource("PlateDesigner.css");
	    if (css != null)
	    	borderpane.getStylesheets().add(  css.toExternalForm());
	    
	    borderpane.getStyleClass().add("myDialog");
	    plate.getStyleClass().add("plate");
	}
	
	private void protocolListSetup()
	{
		protocolList.setRoot(new TreeItem("Analysis"));
		protocolList.getColumns().get(0).setCellValueFactory(p -> {
            String text = p.getValue().getValue();  
            return new ReadOnlyObjectWrapper<String>(text);
    });
		
	}
	public void setAttributeText(String t)
	{
		attrText.setText(t);
	}
	//-----------------------------------------------------------------------------------
	private void installSidebars()
	{
		new BorderPaneAnimator(borderpane, leftSideBarButton, Side.LEFT, false, 100);
		new BorderPaneAnimator(borderpane, rightSideBarButton, Side.RIGHT, false, 300);
		new BorderPaneAnimator(borderpane, bottomSideBarButton, Side.BOTTOM, false, 30);
		DialogUtil.useGlyph(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		DialogUtil.useGlyph(rightSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_LEFT);
		DialogUtil.useGlyph(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
		DialogUtil.useGlyphToo(design, FontAwesomeIcons.DIAMOND);
		DialogUtil.useGlyphToo(plot, FontAwesomeIcons.BAR_CHART);
		DialogUtil.useGlyphToo(config, FontAwesomeIcons.SEARCH);
	}
	//-----------------------------------------------------------------------------------
		private void tableSetup()
		{
		assert attributeCol != null : missing("attributeCol");
		assert valueCol != null : missing("valueCol");
		assert tableView != null : missing("tableView");
		
		setAttributeValueCellValueFactories(); // set cell value factories
		setTreeTableCellValueFactories();
		tableView.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView

		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
		tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{
			@Override public void onChanged(Change<? extends Integer> change) {  }   //selectedIndexes.setAll(change.getList());
		});

		setAttributeValueTableRowFactory();				// set the Row Factory of the table
		tableView.getSelectionModel().clearSelection();
		tableView.getSelectionModel().setCellSelectionEnabled(false);//  row selection only

		analysisTable.setRoot(AppPlato.getTreeRoot());
		
	}
	private void setAttributeValueCellValueFactories()
	{
		attributeCol.setCellValueFactory(new PropertyValueFactory("attribute"));
		valueCol.setCellValueFactory(new PropertyValueFactory("value"));
	}
	private void setTreeTableCellValueFactories()
	{	
		 //Creating a column
//        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
//        column.setPrefWidth(150);   
	    
		// --- population column
	    treeTablePopulation.setPrefWidth(300);	 
	    treeTablePopulation.setCellValueFactory(p -> {
	            String text = p.getValue().getValue();  
	            return new ReadOnlyObjectWrapper<String>(text);
	    });
	}

	public void setAttributeValueTableRowFactory()
	{
		tableView.setRowFactory(p -> //new Callback<TableView<AttributeValue>, TableRow<AttributeValue>>()
//		{
			//@Override public TableRow<AttributeValue> call(TableView<AttributeValue> p)
			{
				final TableRow<AttributeValue> row = new TableRow<AttributeValue>();
				row.setOnDragEntered(t ->
					{	
						DragEvent e = (DragEvent) t;
						if (e.getGestureSource() == tableView) return;
						TableUtil.selectRow(tableView,row);		
				});

				row.setOnDragDetected(t ->
				{
					//if (rowRadio.isSelected())		// only drag full rows
//					if (!dragging)
					{
						Dragboard db = row.getTableView().startDragAndDrop(TransferMode.COPY);
						ClipboardContent content = new ClipboardContent();
						ObservableList<AttributeValue> items = tableView.getSelectionModel().getSelectedItems();
						boolean multiSelection = items.size() > 1;
										
						if (multiSelection)
						{
							StringBuffer buf = new StringBuffer();
							for (AttributeValue av : items)
								buf.append(av.toString()).append('\n');
							content.put(avListDataFormat, buf.toString());
						}
						else if (items.size() == 1)
						{
							AttributeValue av = row.getItem();
							if (av != null)
							{
								String s = av.makeString();
								content.put(avDataFormat, s);
							}
						}
						else
						{
							content.put(DataFormat.PLAIN_TEXT, "EMPTY");
						}
						db.setContent(content);
						TableUtil.selectRow(tableView,row);		
//						dragging = true;
						t.consume();
					}
				});
				
//				row.setOnDragDone(t ->	{	dragging = false;}	);

				return row;
//			}
		});
	}

	
	
}
