package container;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import database.forms.EntrezForm;
import diagrams.draw.App;
import gui.Backgrounds;
import gui.Effects;
import gui.TabPaneDetacher;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.TableData;
import util.FileUtil;
import util.StringUtil;

  
public class PublishController implements Initializable
{
	@FXML TabPane tocTabPane;
	@FXML AnchorPane research;
	@FXML AnchorPane abstractAnchor;
	@FXML AnchorPane methodsAnchor;
	@FXML AnchorPane resultsAnchor;
	@FXML ImageView image;
	EntrezForm querier = new EntrezForm();
	@FXML  ChoiceBox<String> species;
	@FXML  ChoiceBox<String> celltype;
	@FXML  ChoiceBox<String> technology;
	
	@FXML ListView<ScanJob> scans;
	@FXML ListView<Segment> segments;
	@FXML private TreeTableView<org.w3c.dom.Node> xmlTree;
	
	@FXML private VBox fileTreeBox;
	private XMLFileTree fileTree;
	@FXML SplitPane methodsplitter;
	@FXML TableView<Segment> csvtable;

	ObservableList<String> speciesList = FXCollections.observableArrayList("Mouse", "Human", "More...");
	ObservableList<String> cellTypes = FXCollections.observableArrayList("T Cells", "B Cells", "NK Cells", "More...");
	ObservableList<String> technologyList = FXCollections.observableArrayList("ChipCytometry", "PCR", "Mass Spec", "HPLC", "More...");

	static String[] suppressNames = new String[]{ "SpecificParameters", "Environment", "Machine", "MethodHistory"};

	@Override public void initialize(URL location, ResourceBundle resources)
	{
		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);
		research.getChildren().add(querier);
		AnchorPane.setBottomAnchor(querier, 10d);
		AnchorPane.setTopAnchor(querier, 10d);
		AnchorPane.setLeftAnchor(querier, 10d);
		AnchorPane.setRightAnchor(querier, 10d);
		species.setItems(speciesList);
		species.getSelectionModel().selectFirst();
		celltype.setItems(cellTypes);
		celltype.getSelectionModel().selectFirst();
		technology.setItems(technologyList);
		technology.getSelectionModel().selectFirst();
		setupXMLTree();
//		scans.getItems().addAll(new ScanJob("Pos 1", null), new ScanJob("Pos 2", null),  new ScanJob("Pos 3", null));
//		segments.getItems().addAll("Cells", "Values", "Gate1");
		fileTree = new XMLFileTree(null);
		fileTreeBox.getChildren().add(fileTree);
		fileTree.getSelectionModel().selectedItemProperty().addListener((obs, old, val) ->
		{
			File f = val.getValue();
			TreeItem<org.w3c.dom.Node> xml = FileUtil.getXMLtree(f, suppressNames);
			xmlTree.setRoot(xml);
		});
		setupDropPane();
		scans.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			image.setImage(val.getImage());
			image.setVisible(true);
			csvtable.setVisible(false);
		});
		segments.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
//			csvtable.setData(val.getData());
			image.setVisible(false);
			csvtable.setVisible(true);
		});
	}
	//--------------------------------------------------------------------------------

	private void setupDropPane()
	{
		fileTreeBox.setOnDragEntered(e ->
		{
			fileTreeBox.setEffect(Effects.innershadow);
			fileTreeBox.setBackground(Backgrounds.tan);
			e.consume();
		});
		// drops don't work without this line!
		fileTreeBox.setOnDragOver(e ->	{	e.acceptTransferModes(TransferMode.ANY);  e.consume();	});
		
		fileTreeBox.setOnDragExited(e ->
		{
			fileTreeBox.setEffect(null);
			fileTreeBox.setBackground(Backgrounds.white);
			e.consume();
		});
		
		fileTreeBox.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);
			Dragboard db = e.getDragboard();
			Set<DataFormat> formats = db.getContentTypes();
			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
			fileTreeBox.setEffect(null);
			fileTreeBox.setBackground(Backgrounds.white);
			if (db.hasFiles())  addFiles(e);
		});
	}
	//--------------------------------------------------------------------------------

	void addFiles(DragEvent ev)
	{
		Dragboard db = ev.getDragboard();
		double x = ev.getX();
		double y = ev.getY();
		List<File> files = db.getFiles();
		
		for (File f : files)
		{
			if (f.isDirectory())
			{
				fileTree.setRoot(f);
				for (File child : f.listFiles())
				{
					if (child.isDirectory())
					{
						String chName = child.getName().toLowerCase();
						if ("scanjobs".equals(chName))
						{
							scans.getItems().clear();
							addScanJobsDirectory(child);
						}
						if ("segments".equals(chName))
						{
							segments.getItems().clear();
							addSegmentsDirectory(child);
						}
					}
					else if (FileUtil.isXML(child))
					{
						// do nothing as the entire tree was added in whole
					}
				}
				
				break;
			}
		}
			
			
//			Label label = new Label(f.getName());
//			AnchorPane.setLeftAnchor(label, x);
//			AnchorPane.setTopAnchor(label, y);
//			y += 20;
//			label.setPadding(new Insets(10,10,10,10));
//			label.setBorder(Borders.etchedBorder);
//			fileTreeBox.getChildren().add(label);
////			list.getItems().add(f);
//			if (FileUtil.isXML(f))
//			{
//				TreeItem<String> xml = FileUtil.getXMLtree(f);
//				xmlTree.setRoot(xml);
//			}
//			makeDraggable(label);
//			makeDoubleClickOpen(label);
//			addTooltip(label, f);
//			if (f.isDirectory())        label.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.FOLDER_ALT,"32"));
//			else
//			{
//	            Image fxImage = FileUtil.getFileIcon(f.getName());
//	            ImageView imageView = new ImageView(fxImage);
//	            imageView.setFitHeight(32);
//	            imageView.setFitWidth(32);
//	            label.setGraphic(imageView);
//			}
//		}
	}
	private void addScanJobsDirectory(File f)
	{
		for (File kid : f.listFiles())
		{
			if (FileUtil.isImageFile(kid))
				addJob(kid);
			else if (kid.isDirectory())
				addScanJobsDirectory(kid);
		}
	}
	
	
	private void addJob(File kid)
	{
		String id = kid.getParentFile().getParentFile().getParentFile().getName();
		scans.getItems().add(new ScanJob(id, kid));
	}

	private void addSegmentsDirectory(File f)
	{
		File[] kids = f.listFiles();
		for (File kid : kids)
		{
			String id = kid.getName();
			if (kid.isDirectory())
			{
				File[] grandkids = kid.listFiles();
				for (File gkid : grandkids)
					if (FileUtil.isCSV(gkid))
						addSegmentFile(id, gkid);
			}
		}
	}
	
	

	private void addSegmentFile(String id, File gkid)
	{
		segments.getItems().add(new Segment(id, gkid));
	}
	//--------------------------------------------------------------------------------

	private void setupXMLTree()
	{
		
		assert (xmlTree != null);
		xmlTree.setFixedCellSize(30);
		xmlTree.setShowRoot(false);
	// --- name column---------------------------------------------------------
	TreeTableColumn<org.w3c.dom.Node, String> nameColumn = new TreeTableColumn<org.w3c.dom.Node, String>("Name");
	nameColumn.setPrefWidth(220);
	TreeTableColumn<org.w3c.dom.Node, String> idCol = new TreeTableColumn<org.w3c.dom.Node, String>("Id");
	idCol.setPrefWidth(100);

	nameColumn.setCellValueFactory(p ->
	{
		org.w3c.dom.Node f = p.getValue().getValue();
		String text = "error";
		if (f != null)
		{
			text = lookup(f.getNodeName());

			if (text.equals("Object") || text.equals("Method")|| text.equals("Machine"))
			{
				org.w3c.dom.Node n = f.getAttributes().getNamedItem("Type");
				if (n != null)
					text = n.getTextContent();
			} else
			{
				org.w3c.dom.Node n = f.getAttributes().getNamedItem("Name");
				if (n != null)
					text = n.getTextContent();
			}
			
		}
		text = lookup(text);
		if (text.length() > 64)
			text = text.substring(0,64) + "...";
		return new ReadOnlyObjectWrapper<String>(text);
	});
	
	
	idCol.setCellValueFactory(p ->
	{
		org.w3c.dom.Node f = p.getValue().getValue();
		String text = "error";
		if (f != null)
		{
			if (f.getNodeName().equals("Restriction"))
			{
				org.w3c.dom.Node n = f.getAttributes().getNamedItem("MinOccur");
				if (n != null)
					text = n.getTextContent();
				n = f.getAttributes().getNamedItem("MaxOccur");
				if (n != null)
					text += ", " + n.getTextContent();
			}
			else
			{
				text = f.getTextContent();
				if (text.length() > 64)
					text = text.substring(0,64) + "...";
				
				org.w3c.dom.Node n = f.getAttributes().getNamedItem("UID");
				if (n != null)
					text = n.getTextContent();
				n = f.getAttributes().getNamedItem("Value");
				if (n != null)
				{
					text = n.getTextContent();
					n = f.getAttributes().getNamedItem("Unit");
					if (n != null)
						text += " " + n.getTextContent();
				}
			}
		}
		return new ReadOnlyObjectWrapper<String>(text);
	});
	xmlTree.getColumns().setAll(new TreeTableColumn[] {nameColumn, idCol});
//	xmlTree.setRowFactory(value);
//	xmlTree.set

	}
	
	String lookup(String orig)
	{
		if (orig == null) return "";
		if (orig.equals("Inputobjects"))	return "Input";
		if (orig.equals("Outputobjects"))	return "Output";
		if (orig.equals("EncapsulatedObjects"))	return "Objects";
		if (orig.equals("EncapsulatedMethods"))	return "Steps";
		if (orig.equals("SpecificParameter"))	return "Parameter";
		if (orig.equals("SpecificParameters"))	return "Parameters";
		if (orig.equals("ObjectConnector"))	return "Connection";
		if (orig.equals("EncapsulatedObjectsRef"))	return "References";
		if (orig.equals("EncapsulatedMethodsRef"))	return "References";
		if (orig.equals("Meth"))	return "Method";
		if (orig.equals("MethRef"))	return "Reference";
		if (orig.equals("MethodHistory"))	return "History";
		if (orig.equals("ObjRef"))	return "Reference";
		if (orig.equals("Obj"))	return "Object";
		if (orig.equals("PrimaryContainer"))	return "Container";
		return orig;
	}
		
	//--------------------------------------------------------------------------------
	double dragX, dragY;
	void makeDraggable(Node n)
	{
		n.setOnMouseEntered(e -> {	setBackground(n, "#FFF0FF");	});
		n.setOnMouseExited(e -> {	setBackground(n, "white");	});
		n.setOnMousePressed(e -> {	dragX = e.getX();	dragY = e.getY();	});
		n.setOnMouseDragged(e -> {
			n.setTranslateX(n.getTranslateX() + e.getX() - dragX);	
			n.setTranslateY(n.getTranslateY() + e.getY() - dragY);	
		});
	}
	
	void setBackground(Node n, String colorStr)
	{
		n.setStyle("-fx-background-color: " + colorStr + "; ");
	}
	
	void addTooltip(Node n, File f)
	{
		Tooltip.install(n, new Tooltip(FileUtil.getTextDescription(f)));
	}
	
	// set the background on a single click, and launch the file on a double
	void makeDoubleClickOpen(Node n)
	{
        n.setOnMouseClicked(e -> {
        	EventTarget t = e.getTarget();
        	int clickCt = e.getClickCount();
            if(e.getButton().equals(MouseButton.PRIMARY))
                if(clickCt == 1 && t instanceof Label)
				{
					Label lab = (Label) t;
					setBackground(lab, "#FFD0FF");
                }
                if(clickCt == 2 && t instanceof Label)
                 {
					Label lab = (Label) t;
					setBackground(lab,"white");
//					File f = findFile(lab.getText());
//					if (f != null)
//						openFile(f);
                 }
        	});
	}
	//--------------------------------------------------------------------------------
	class Segment
	{
		String id;
		File csvFile;
		TableData data;
		
		Segment(String inID, File inFile)
		{
			id = inID;
			csvFile = inFile;
			if (csvFile != null)
				; // read table
		}
		public String toString()		{	return id + ": " + csvFile.getName();		}
		public TableData getData()		{	return data;		}
	}
	//--------------------------------------------------------------------------------
	class ScanJob
	{
		String id;
		File imageFile;
		Image image;
		
		ScanJob(String inID, File inFile)
		{
			id = inID;
			imageFile = inFile;
			if ((inFile == null || !FileUtil.isImageFile(inFile)))
				image =  null;
			else
				try{
					String path = inFile.getCanonicalPath();
					image =  new Image(new FileInputStream(path));

				}
			catch (Exception e) { System.out.println(inFile.getAbsolutePath());   e.printStackTrace(); }
		}
		public String toString()		{	return id;		}
		public Image getImage()		{	return image;		}
	}
	
	//--------------------------------------------------------------------------------

	@FXML void doNew()
	{
		try
		{
			AppContainer.getInstance().doNew(new Stage());
		} 
		catch (Exception e)		{		e.printStackTrace();	}
		
	}
	
	@FXML void doOpen()
	{
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Container File...");
		File file = chooser.showOpenDialog(App.getInstance().getStage());
		if (file != null)		open(file);
	}
	
	void open(File f)
	{
		
	}
	@FXML void doClose()
	{
		
	}
	@FXML void doZip()
	{
	}
	@FXML void doUnzip()
	{
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(FileUtil.zipFilter);
       File f = fileChooser.showOpenDialog(null);		    //Show open file dialog
       if (f != null)
       {
    	  String entries =  FileUtil.decompress(f);
    	 System.out.println("decompressed: " + entries 
    		+ "\nsee folder at: " + StringUtil.chopExtension(f.getAbsolutePath()));
       }
	}
	//--------------------------------------------------------------------------------------
	@FXML void doUndo()
	{	
	}
	@FXML void doRedo()
	{
	}
	//--------------------------------------------------------------------------------------
	@FXML void doCut()
	{
	}
	@FXML void doCopy()
	{
	}
	@FXML void doPaste()
	{
	}
	//--------------------------------------------------------------------------------
	@FXML void doCompare()
	{ 
	}
	//--------------------------------------------------------------------------------
	static public void openFile(File f)
	{
		try
		{
			AppContainer.getInstance().getHostServices().showDocument(f.toURI().toURL().toExternalForm());
		}
		catch (Exception e){}
	}
//	
//	private File findFile(String name)
//	{
//		for (File f : list.getItems())
//			if (f.getName().toLowerCase().equals(name.toLowerCase()))	
//				return f;
//		return null;
//	}
	// --------------------------------------------------------------------------------

	private static class FileListCell extends ListCell<File>
	{
		@Override public void updateItem(File item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty)			{		setGraphic(null);			setText(null);		} 
			else
			{
				Image fxImage = FileUtil.getFileIcon(item.getName());
				ImageView imageView = new ImageView(fxImage);
				setGraphic(imageView);
				setText(item.getName());
			}
		}
	  }
}
