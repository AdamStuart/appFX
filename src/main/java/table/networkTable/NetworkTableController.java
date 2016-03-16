package table.networkTable;

import java.net.URL;
import java.util.ResourceBundle;

import icon.FontAwesomeIcons;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.DialogUtil;

public class NetworkTableController implements Initializable
{
	@FXML private BorderPane root;
	@FXML private HBox footer;
	
	@FXML private Button tableOptions;
	@FXML private ChoiceBox<StrokeOption> strokes;
	@FXML private ChoiceBox<ArrowShapeOption> srcArrow;
	@FXML private ChoiceBox<ArrowShapeOption> targetArrow;
	@FXML private ChoiceBox<Shape> shapes;
//	@FXML private ImageView logo;
//	@FXML private TextField targetYfld;
//	@FXML private Label version;
	@FXML public TableView<NetworkRecord> netTable;
	@FXML public TableView<NodeRecord> nodeTable;
	@FXML public TableView<EdgeRecord> edgeTable;

	// TableColumns are injected by SceneBuilder
	@FXML public TableColumn<NetworkRecord, String> typeCol;
	@FXML public TableColumn<NetworkRecord, String> netNameCol;
	@FXML public TableColumn<NetworkRecord, Integer> nNodesCol;
	@FXML public TableColumn<NetworkRecord, Integer> nEdgesCol;
	
	@FXML public TableColumn<NodeRecord, String> idCol;
	@FXML public TableColumn<NodeRecord, String> nodeNameCol;
	@FXML public TableColumn<NodeRecord, String> defCol;
	@FXML public TableColumn<NodeRecord, String> descCol;

	@FXML public TableColumn<EdgeRecord, String> srcCol;
	@FXML public TableColumn<EdgeRecord, String> interactionCol;
	@FXML public TableColumn<EdgeRecord, String> targetCol;

	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		setupTableFactories();
		initializeModel();
		DialogUtil.useGlyph(tableOptions, FontAwesomeIcons.GEAR);
		tableOptions.setOnAction((ev) -> { doTableOptionsPopup(); });
		populateArrowChoices();
		populateStrokeChoices();
		populateShapeChoices();
		populateTableOptions();
				
	}
	final ContextMenu tableColumnOptions = new ContextMenu(); 

	  private void populateTableOptions()
	  {
		  String[] colnames = new String[] { "Id", "Name",  "Definition",  "Description",  "Selected",  "Type"};
			for (String c : colnames)
			{
				tableColumnOptions.getItems().add(new CheckMenuItem(c));
			}
			
		}
	  private void populateShapeChoices()
		{
			for (Shape opt : Shape.values())
			{
				shapes.getItems().add(opt);
			}
			
		}
	  private void populateStrokeChoices()
		{
			for (StrokeOption opt : StrokeOption.values())
			{
				strokes.getItems().add(opt);
			}
			
		}
    private void populateArrowChoices()
	{
		for (ArrowShapeOption opt : ArrowShapeOption.values())
		{
			srcArrow.getItems().add(opt);
			targetArrow.getItems().add(opt);
		}
		
	}

	private void doTableOptionsPopup()
	{
		tableColumnOptions.setX(tableOptions.getLayoutX() + stage.getX() - 60); 
		tableColumnOptions.setY(tableOptions.getLayoutY() + stage.getY()+ 40);
//		Path solidpath = new Path();
//		solidpath.getElements().add(new MoveTo(0.0f, 15.0f));
//		solidpath.getElements().add(new LineTo(100.0f, 15.0f));
//
//		Path path = new Path();
//		path.getElements().add(new MoveTo(0.0f, 55.0f));
//	    path.getElements().add(new LineTo(100.0f, 55.0f));
//	    path.setStrokeDashOffset(3);
//	    path.getStrokeDashArray().addAll(15d, 20d, 5d, 20d);
//
//		Path dots = new Path();
//		dots.getElements().add(new MoveTo(0.0f, 95.0f));
//		dots.getElements().add(new LineTo(100.0f, 95.0f));
//		dots.setStrokeDashOffset(3);
//		dots.getStrokeDashArray().addAll(3d);
//		MenuItem item1 = new MenuItem();
//		MenuItem item2 = new MenuItem();
//		MenuItem item3 = new MenuItem();
//		item1.setGraphic(solidpath);
//		item2.setGraphic(path);
//		item3.setGraphic(dots);
//		
//	    popup.getItems().clear();
//	    popup.getItems().addAll(item1, item2, item3);
		tableColumnOptions.show(stage);
	}
	private void setupTableFactories()
	{
		nNodesCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, Integer>("nNodes"));
		nEdgesCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, Integer>("nEdges"));
		typeCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, String>("type"));
		netNameCol.setCellValueFactory(new PropertyValueFactory<NetworkRecord, String>("name"));
		idCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("id"));
		descCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("description"));
		nodeNameCol.setCellValueFactory(new PropertyValueFactory<NodeRecord, String>("name"));
		interactionCol.setCellValueFactory(new PropertyValueFactory<EdgeRecord, String>("interaction"));
		
	}

	private void initializeModel()
	{
		
		NodeRecord node1 = new NodeRecord("0001", "LymphNode", "red and bulgy");
		NodeRecord node2 = new NodeRecord("0002", "ConNode", "small silver");
		NodeRecord node3 = new NodeRecord("0003", "Distnode", "black opaque");

		EdgeRecord edge1 = new EdgeRecord("E001", node1, node2, "is_a");
		EdgeRecord edge2 = new EdgeRecord("E002", node1, node3, "is_a");
		EdgeRecord edge3 = new EdgeRecord("E003", node3, node2, "is_a");

		NetworkRecord net1 = new NetworkRecord("N001", "PrimaryNet");

		netTable.getItems().add(net1);
		nodeTable.getItems().addAll(node1, node2, node3);
		edgeTable.getItems().addAll(edge1, edge2, edge3);
	}

	private Stage stage;
	public void setStage(Stage primaryStage)	{		stage = primaryStage;	}

	// ------------------------------------------------------

}
