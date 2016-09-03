package table.networkTable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import icon.FontAwesomeIcons;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.DialogUtil;

public class NetworkTableController implements Initializable
{
	@FXML private BorderPane root;
	@FXML private HBox footer;
	
	@FXML private Button tableOptions;

	@FXML public TableView<NetworkRecord> netTable;
	@FXML public TableView<NodeRecord> nodeTable;
	@FXML public TableView<EdgeRecord> edgeTable;

	// TableColumns are injected by SceneBuilder
//	@FXML public TableColumn<NetworkRecord, String> typeCol;
	@FXML public TableColumn<NetworkRecord, String> netNameCol;
	@FXML public TableColumn<NetworkRecord, Integer> nNodesCol;
//	@FXML public TableColumn<NetworkRecord, Integer> nEdgesCol;
	
	@FXML public TableColumn<NodeRecord, String> idCol;
	@FXML public TableColumn<NodeRecord, String> nodeNameCol;
//	@FXML public TableColumn<NodeRecord, String> defCol;
	@FXML public TableColumn<NodeRecord, String> descCol;

	@FXML public TableColumn<EdgeRecord, String> srcCol;
	@FXML public TableColumn<EdgeRecord, String> interactionCol;
	@FXML public TableColumn<EdgeRecord, String> targetCol;

	@FXML public MenuItem idmapper;
	
	@FXML private Button networks;
	@FXML private Button nodes;
	@FXML private Button edges;
	@FXML private Button vizmap;
	
	

	
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	List<String> nodeList = new  ArrayList<String>();
    
	static TableRow<NodeRecord> thisRow = null;

	@Override public void initialize(URL location, ResourceBundle resources)
	{
		setupTableFactories();
		initializeModel();
		DialogUtil.useGlyph(tableOptions, FontAwesomeIcons.GEAR);
		DialogUtil.useGlyph(networks, FontAwesomeIcons.DIAMOND);
		DialogUtil.useGlyph(nodes, FontAwesomeIcons.ADJUST);
		DialogUtil.useGlyph(edges, FontAwesomeIcons.BINOCULARS);
//		DialogUtil.useGlyph(vizmap, FontAwesomeIcons.DOT_CIRCLE_ALT);
//		new BorderPaneAnimator(root, vizmap, Side.BOTTOM, false, 150);
		
		tableOptions.setOnAction((ev) -> { doTableOptionsPopup(); });
		populateTableOptions();
		
		nodeTable.setRowFactory(tv -> {
        TableRow<NodeRecord> row = new TableRow<>();

        row.setOnDragDetected(event -> {
            if (! row.isEmpty()) {
                Integer index = row.getIndex();
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(row.snapshot(null, null));
                ClipboardContent cc = new ClipboardContent();
                cc.put(SERIALIZED_MIME_TYPE, index);
                db.setContent(cc);
                event.consume();
                thisRow = row;
            }
        });

        row.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                    thisRow = row;
//                  if (thisRow != null) 
//                 	   thisRow.setOpacity(0.3);
                }
            }
        });

        row.setOnDragExited(event -> {
            if (event.getGestureSource() != thisRow &&
                    event.getDragboard().hasString()) {
//               if (thisRow != null) 
//            	   thisRow.setOpacity(1);
               thisRow = null;
            }
        });

        row.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            }
        });

        row.setOnMouseClicked(event -> {
        	if (event.getClickCount() == 2)
            {
                int idx = row.getIndex();
        		getInfo(idx);
              event.consume();
            }
        });

        row.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                NodeRecord draggedNode = nodeTable.getItems().remove(draggedIndex);

                int  dropIndex = (row.isEmpty()) ? nodeTable.getItems().size() : row.getIndex();
                nodeTable.getItems().add(dropIndex, draggedNode);

                event.setDropCompleted(true);
                nodeTable.getSelectionModel().select(dropIndex);
                event.consume();
//                if (thisRow != null) 
//             	   thisRow.setOpacity(1);
                thisRow = null;
            }
        });

        return row ;
    });
		
		showHeatmap();
	}

	private void getInfo(int idx) {
		stage = new Stage();
		try 
		{
		   String rowName = "" + idx;
		   if (idx >=0 && nodeList.size() > idx)
			   	rowName = nodeList.get(idx);
		   stage.setTitle("Information: " + rowName );
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "webview.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    VBox appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, 600, 400));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}

		
	}

	// ------------------------------------------------------

	final ContextMenu tableColumnOptions = new ContextMenu();

	private void populateTableOptions() {
		String[] colnames = new String[] { "Id", "Name", "Definition", "Description", "Selected", "Type" };
		for (String c : colnames)
			tableColumnOptions.getItems().add(new CheckMenuItem(c));
	}
	// ------------------------------------------------------

	private void doTableOptionsPopup()
	{
		tableColumnOptions.setX(tableOptions.getLayoutX() + stage.getX() - 60); 
		tableColumnOptions.setY(tableOptions.getLayoutY() + stage.getY()+ 40);
		tableColumnOptions.show(stage);
	}
	
	private void setupTableFactories()
	{
		nNodesCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, Integer>("nNodes"));
//		nEdgesCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, Integer>("nEdges"));
//		typeCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, String>("type"));
		netNameCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, String>("name"));
		idCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("id"));
		descCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("description"));
		nodeNameCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("name"));
		interactionCol.setCellValueFactory(new PropertyValueFactory<EdgeRecord, String>("interaction"));
		
	}

	private void initializeModel()
	{
		int nNodes = 100;
		NodeRecord[] nodes = new NodeRecord[nNodes];
		for (int i=0; i<nNodes; i++)
			nodes[i] =  new NodeRecord("00"+i, "A"+(i+1), "N"+i, "Unknown Table Node");
		nodeTable.getItems().addAll(nodes);
//		
//		
//		NodeRecord node1 = new NodeRecord("0001", "A", "TNF", "Terminal Necrosis Factor");
//		NodeRecord node2 = new NodeRecord("0002", "B", "TP53", "Tumor Protein 53");
//		NodeRecord node3 = new NodeRecord("0003", "C", "DAPK2", "Death-associated protein kinase 2 ");
//		NodeRecord node4 = new NodeRecord("0004", "D", "MLH1", "mutL homolog 1, colon cancer, nonpolyposis type 2");
//		NodeRecord node5 = new NodeRecord("0005", "E", "E2F3", "E2F transcription factor 3");
//		NodeRecord node6 = new NodeRecord("0006", "F", "LAMC3", "Laminin, gamma 3");
//		NodeRecord node7 = new NodeRecord("0007", "G", "BRCA", "Breast Cancer 1");
//		NodeRecord node8 = new NodeRecord("0008", "H", "PDGFB", "Platelet-derived growth factor beta polypeptide");
//		nodeTable.getItems().addAll(node1, node2, node3, node4, node5, node6, node7, node8	);
//
//		EdgeRecord edge1 = new EdgeRecord("E001", node1, node2, "coex");
//		EdgeRecord edge2 = new EdgeRecord("E002", node1, node3, "coex");
//		EdgeRecord edge3 = new EdgeRecord("E003", node3, node4, "coex");
//		edgeTable.getItems().addAll(edge1, edge2, edge3);
//
//		NetworkRecord net1 = new NetworkRecord("N001", "PrimaryNet");
//		NetworkRecord net2 = new NetworkRecord("N002", "SecondaryNet");
//		NetworkRecord net3 = new NetworkRecord("N003", "TertiaryNet");
//		netTable.getItems().addAll(net1, net2, net3);

	}

	private Stage stage;
	public void setStage(Stage primaryStage)	{		stage = primaryStage;	}

	// ------------------------------------------------------
	@FXML private void showNodesOnly()
	{
//		netTable.setVisible(false);
//		edgeTable.setVisible(false);
		System.out.println("showNodesOnly");
		netTable.setMinWidth(0);
		netTable.setMaxWidth(0);
		netTable.setPrefWidth(0);
		edgeTable.setMinWidth(0);
		edgeTable.setMaxWidth(0);
		edgeTable.setPrefWidth(0);
		showEdges = showNetworks = true;
	}
	
	boolean showEdges = true;
	boolean showNetworks = true;
	@FXML private void showEdges()
	{
		System.out.println("showEdges: " + (showEdges ? "True" : "False"));
		int wid = showEdges ? 300 : 0;
		showEdges = !showEdges;
		edgeTable.setMaxWidth(wid);
		edgeTable.setMinWidth(wid);
		edgeTable.setPrefWidth(wid);

	}

	@FXML private void showNetworks()
	{
		System.out.println("showNetworks: " + (showNetworks ? "True" : "False"));
		int wid = showNetworks ? 200 : 0;
		netTable.setMaxWidth(wid);
		netTable.setPrefWidth(wid);
		netTable.setMinWidth(wid);
		showNetworks = !showNetworks;
	}
//----------------------------------------------------------------------------------
	@FXML private void doNew()
	{
		System.out.println("doNew");
	}

	@FXML private void open()
	{
		System.out.println("open");
	}

	@FXML private void close()
	{
		System.out.println("close");
	}

	@FXML private void save()
	{
		System.out.println("save");
	}

	@FXML private void saveas()
	{
		System.out.println("saveas");
	}

	@FXML private void print()
	{
		System.out.println("print");
	}

	@FXML private void quit()
	{
		System.out.println("quit");
	}
	//----------------------------------------------------------------------------------
	@FXML private void mapId()
	{
		System.out.println("mapId");
		Dialog dlog = new IdMappingDialog("EMBL");
		// Create the custom dialog.
//			Dialog<Pair<String, String>> dialog = new Dialog<>();
		dlog.showAndWait();
	}
	//----------------------------------------------------------------------------------
	@FXML private void showHeatmap()
	{
		System.out.println("showHeatmap");
	}

}
