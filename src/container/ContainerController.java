package container;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import diagrams.draw.App;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import gui.TabPaneDetacher;
import icon.FontAwesomeIcons;
import icon.GlyphsDude;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.FileUtil;
import util.StringUtil;

  
public class ContainerController implements Initializable
{
	@FXML private ListView<File> list;
	@FXML private AnchorPane anchor;
	@FXML private TreeTableView<org.w3c.dom.Node> xmlTree;
	@FXML private VBox fileContainer;
	@FXML TreeTableColumn<TreeTableView, String>  col0;
	@FXML TreeTableColumn<TreeTableView, String>  col1;
	private Label description;
	private XMLFileTree fileTree;
	@FXML TabPane tocTabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		description = new Label("You can drop files here.");
		AnchorPane.setRightAnchor(description, new Double(20));
		AnchorPane.setTopAnchor(description, new Double(50));
		anchor.getChildren().add(description);
		setupDropPane();
		fileTree = new XMLFileTree(null);
		
		fileContainer.getChildren().add(fileTree);
		VBox.setVgrow(fileTree, Priority.ALWAYS);
//		setupXMLTree();
//		col0.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
////		col0.setCellValueFactory( cell -> cell.getValue());//		xmlTree.setCellFactory(new PropertyValueFactory("value"));
//		col0.setCellValueFactory((CellDataFeatures<TreeTableView<?>, org.w3c.dom.Node> p) -> 
//			new ReadOnlyStringWrapper(p.getValue().getValue().accessibleTextProperty().get()));  

//		 
//		(xmlTree, param) -> {   
//			new ReadOnlyStringWrapper(param.getValue().getValue().getId());}   
//		);
		list.setCellFactory(p -> new FileListCell());
//		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);

}
//
//	private void setupXMLTree()
//	{
////		xmlTree.setRoot(null);
//	//
//		xmlTree.setFixedCellSize(30);
//		xmlTree.setShowRoot(false);
//	// --- name column---------------------------------------------------------
//	TreeTableColumn<org.w3c.dom.Node, String> nameColumn = new TreeTableColumn<org.w3c.dom.Node, String>("Name");
//	nameColumn.setPrefWidth(220);
//	TreeTableColumn<org.w3c.dom.Node, String> idCol = new TreeTableColumn<org.w3c.dom.Node, String>("Id");
//	idCol.setPrefWidth(100);
////	nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));
////	sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory("id"));
//	nameColumn.setCellValueFactory(p ->
//	{
//		org.w3c.dom.Node f = p.getValue().getValue();
//		String text = "error";
//		if (f != null)
//		{
//			text = lookup(f.getNodeName());
//
//			if (text.equals("Object"))
//			{
//				org.w3c.dom.Node n = f.getAttributes().getNamedItem("Type");
//				if (n != null)
//					text = n.getTextContent();
//			}
//
//			if (text.length() > 24)
//				text = text.substring(0,24) + "...";
//			org.w3c.dom.Node n = f.getAttributes().getNamedItem("Name");
//			if (n != null)
//			{
//				String cont = n.getTextContent();
//				text = lookup(cont);
//			}
//			
//		}
//		return new ReadOnlyObjectWrapper<String>(text);
//	});
//	
//	
//	idCol.setCellValueFactory(p ->
//	{
//		org.w3c.dom.Node f = p.getValue().getValue();
//		String text = "error";
//		if (f != null)
//		{
//			if (f.getNodeName().equals("Restriction"))
//			{
//				org.w3c.dom.Node n = f.getAttributes().getNamedItem("MinOccur");
//				if (n != null)
//					text = n.getTextContent();
//				n = f.getAttributes().getNamedItem("MaxOccur");
//				if (n != null)
//					text += ", " + n.getTextContent();
//			}
//			else
//			{
//			text = f.getTextContent();
//			if (text.length() > 64)
//				text = text.substring(0,64) + "...";
//			
//			org.w3c.dom.Node n = f.getAttributes().getNamedItem("UID");
//			if (n != null)
//				text = n.getTextContent();
//			n = f.getAttributes().getNamedItem("Value");
//			if (n != null)
//			{
//				text = n.getTextContent();
//				n = f.getAttributes().getNamedItem("Unit");
//				if (n != null)
//					text += " " + n.getTextContent();
//			}
//			}
//			
//			
//		}
//		return new ReadOnlyObjectWrapper<String>(text);
//	});
//	xmlTree.getColumns().setAll(new TreeTableColumn[] {nameColumn, idCol});
////	xmlTree.setRowFactory(value);
////	xmlTree.set
//
//	}
//	
//	String lookup(String orig)
//	{
//		if (orig == null) return "";
//		if (orig.equals("Inputobjects"))	return "Input";
//		if (orig.equals("Outputobjects"))	return "Output";
//		if (orig.equals("EncapsulatedObjects"))	return "Objects";
//		if (orig.equals("EncapsulatedMethods"))	return "Methods";
//		if (orig.equals("SpecificParameter"))	return "Parameter";
//		if (orig.equals("SpecificParameters"))	return "Parameters";
//		if (orig.equals("ObjectConnector"))	return "Connection";
//		if (orig.equals("EncapsulatedObjectsRef"))	return "References";
//		if (orig.equals("EncapsulatedMethodsRef"))	return "References";
//		if (orig.equals("Meth"))	return "Method";
//		if (orig.equals("MethRef"))	return "Reference";
//		if (orig.equals("MethodHistory"))	return "History";
//		if (orig.equals("ObjRef"))	return "Reference";
//		if (orig.equals("Obj"))	return "Object";
//		if (orig.equals("PrimaryContainer"))	return "Container";
//		return orig;
//	}
//	
	
//	
//	
//	nameColumn.setCellValueFactory(p ->
//	{
//		org.w3c.dom.Node f = p.getValue().getValue();
//		String text = f.getNodeName();
//		setOnMouseClicked(ev -> {	if (ev.getClickCount() == 2) 	
//		{
//			TreeItem<File> tree = getSelectionModel().getSelectedItem();
//			if (tree != null)
//			{
//				File file = tree.getValue();
//				if (file != null)	ContainerController.openFile(file);
//			}	
//		}
//		});
//			
//		return new ReadOnlyObjectWrapper<String>(text);
//	} );
//		// --- size column---------------------------------------------------------
//
//	sizeColumn.setCellValueFactory( p ->	{	return new ReadOnlyObjectWrapper<Node>(p.getValue().getValue());  });
//	
//	sizeColumn.setCellFactory( p ->
//	{
//		return new TreeTableCell<Node, Node>()
//		{
//			@Override protected void updateItem(File item, boolean empty)
//			{
//				super.updateItem(item, empty);
//				TreeTableView treeTable = p.getTreeTableView();
//				TreeItem<File> treeItem = treeTable.getTreeItem(getIndex());
//
//				String txt = null;
//				if (item == null || empty || treeItem == null || treeItem.getValue() == null)
//					txt = "";
//				else if (treeItem.getValue().isDirectory())
//					txt = getNChildren(treeItem) + " files";
//				else
//				{
//					if (item.length() > 10000000)
//						txt = (item.length() / (1024 * 1024) + " MB");
//					else if (item.length() > 10000)
//						txt = (item.length() / 1024 + " KB");
//					else 		txt = item.length() + " bytes";
//				}
//				setText(txt);
//			}
//		};
//	});
//	sizeColumn.setComparator(new Comparator<Node>()
//	{
//		@Override public int compare(Node f1, Node f2)
//		{
////			long s1 = f1.isDirectory() ? 0 : f1.length();
////			long s2 = f2.isDirectory() ? 0 : f2.length();
////			long result = s1 - s2;
////			if (result < 0)		return -1;
////			if (result == 0)	return 0;
//			return 1;
//		}
//	});
//
//	}

	//--------------------------------------------------------------------------------

	private void setupDropPane()
	{
		anchor.setOnDragEntered(e ->
		{
			anchor.setEffect(Effects.innershadow);
			anchor.setBackground(Backgrounds.tan);
			e.consume();
		});
		// drops don't work without this line!
		anchor.setOnDragOver(e ->	{	e.acceptTransferModes(TransferMode.ANY);  e.consume();	});
		
		anchor.setOnDragExited(e ->
		{
			anchor.setEffect(null);
			anchor.setBackground(Backgrounds.white);
			e.consume();
		});
		
		anchor.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);
			Dragboard db = e.getDragboard();
			Set<DataFormat> formats = db.getContentTypes();
			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
			anchor.setEffect(null);
			anchor.setBackground(Backgrounds.white);
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
			Label label = new Label(f.getName());
			AnchorPane.setLeftAnchor(label, x);
			AnchorPane.setTopAnchor(label, y);
			y += 20;
			label.setPadding(new Insets(10,10,10,10));
			label.setBorder(Borders.etchedBorder);
			anchor.getChildren().add(label);
			list.getItems().add(f);
			if (FileUtil.isXML(f))
			{
				TreeItem<org.w3c.dom.Node> xml = FileUtil.getXMLtree(f, null);
				xmlTree.setRoot(xml);
			}
			makeDraggable(label);
			makeDoubleClickOpen(label);
			addTooltip(label, f);
			if (f.isDirectory())        label.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.FOLDER_ALT,"32"));
			else
			{
	            Image fxImage = FileUtil.getFileIcon(f.getName());
	            ImageView imageView = new ImageView(fxImage);
	            imageView.setFitHeight(32);
	            imageView.setFitWidth(32);
	            label.setGraphic(imageView);
			}
		}
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
					File f = findFile(lab.getText());
					if (f != null)
						openFile(f);
                 }
        	});
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
	
	private File findFile(String name)
	{
		for (File f : list.getItems())
			if (f.getName().toLowerCase().equals(name.toLowerCase()))	
				return f;
		return null;
	}
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
