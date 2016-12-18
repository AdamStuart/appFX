package container;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import animation.BorderPaneAnimator;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import icon.FontAwesomeIcons;
import icon.GlyphsDude;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.bio.OntologyTerm;
import util.DialogUtil;
import util.FileUtil;
import util.StringUtil;
import xml.XMLFileTree;

  
public class ContainerController implements Initializable
{
	@FXML private Button leftToggle;
	@FXML private Button rightToggle;
	@FXML private ListView<File> list;
	@FXML private AnchorPane anchor;
	@FXML private TreeTableView<org.w3c.dom.Node> xmlTree;
	@FXML private TreeTableView<OntologyTerm> ontologytree;
	@FXML private VBox fileTreeContainer;
	@FXML private BorderPane container;
	@FXML private VBox fileListContainer;
	@FXML TreeTableColumn<TreeTableView<File>, String>  col0;
	@FXML TreeTableColumn<TreeTableView<File>, String>  col1;
	@FXML TreeTableColumn<TreeTableView<OntologyTerm>, String>  colId;
	@FXML TreeTableColumn<TreeTableView<OntologyTerm>, String>  colName;
	@FXML TreeTableColumn<TreeTableView<OntologyTerm>, String>  colDef;
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
		new BorderPaneAnimator(container, leftToggle, Side.LEFT, false, 280);
		new BorderPaneAnimator(container, rightToggle, Side.RIGHT, false, 180);
		DialogUtil.useGlyph(leftToggle, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		DialogUtil.useGlyph(rightToggle, FontAwesomeIcons.ARROW_CIRCLE_O_LEFT);
	
		fileTree = new XMLFileTree(null);
		fileTreeContainer.getChildren().add(fileTree);
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
		setupTableColumns();
	
}
	
	private void setupTableColumns()
	{	
		colName.setPrefWidth(520);
		colName.setCellValueFactory(new TreeItemPropertyValueFactory("name"));  
		colId.setCellValueFactory(new TreeItemPropertyValueFactory("id"));  
		colDef.setCellValueFactory(new TreeItemPropertyValueFactory("def"));  
		
		colName.setCellFactory(col -> {
	        TreeTableCell<TreeTableView<OntologyTerm>, String> cell = new TreeTableCell<TreeTableView<OntologyTerm>, String>() 
	        {
	            @Override
	            public void updateItem(String item, boolean empty) {
	                super.updateItem(item, empty);
//	                setText(empty ? "A" : "B");
	                if (empty)       setText("");
	                 else {
	                    setText(item);
	                    int index = getTreeTableRow().getIndex();
	                    TreeItem<TreeTableView<OntologyTerm>> row = getTreeTableView().getTreeItem(index);
//	                    
	                    Object term = row.getValue();
	                    if (term instanceof OntologyTerm)
	                    {
	                    	OntologyTerm t = (OntologyTerm) term;
		                    setTooltip(new Tooltip(t.getDescriptor()));		//term.getDescriptor())
	                    }
	                }
	            }
	        };
//	        cell.setAlignment(Pos.CENTER);
	        return cell;
	    });
	    
//		colId.setCellFactory(tv ->  {
////	        final Tooltip tooltip = new Tooltip();
//	        TreeTableCell<TreeTableView, Onterm> cell = new TreeTableCell<TreeTableView, Onterm>() {
//	            @Override
//	            public void updateItem(Onterm item, boolean empty) {
//	                super.updateItem(item, empty);
////	                if (empty) {
////	                    setText(null);
////	                    setTooltip(null);
////	                } else if (false) {
////	                    setText("Cell Line Ontology");
////	                    setTooltip(null);
////	                } else {
////	                    setText(item.getName());
////	                    tooltip.setText(item.getDescriptor());
////	                    setTooltip(tooltip);
////	                }
//	            }
//	        };
//	        cell.setOnMouseClicked(e -> {
//	            if (e.getClickCount() == 2 && ! cell.isEmpty()) {
//	            	Onterm term = cell.getItem();
//	            	System.out.println("Set root to " + term.toString());
//	                // do whatever you need with path...
//	            }
//	        });
//	        return cell ;
//	    });
////		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);

	}


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
//			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
			anchor.setEffect(null);
			anchor.setBackground(Backgrounds.white);
			if (db.hasFiles())  
			try
			{
					addFiles(e);
			}
			catch (Exception ex) {}
		});
	}
	//--------------------------------------------------------------------------------

	void addFiles(DragEvent ev) throws Exception
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
				xmlTree.setVisible(true);
				ontologytree.setVisible(false);
			}
			if (FileUtil.isOBO(f))
			{
				long start = System.currentTimeMillis();
				List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
				System.out.println("Reading: " + (System.currentTimeMillis() - start));
				Ontology o = readObo(lines);
				ontologytree.setRoot(o.createTree());
				ontologytree.setVisible(true);
				xmlTree.setVisible(false);
				o.dump();
				File homeDir = new File("");
				String output = o.createSIF("CL");
				FileUtil.writeTextFile(homeDir, "CL.SIF", output);
				
				String txns = o.createSIF("NCBIT");
				FileUtil.writeTextFile(homeDir, "Taxons.SIF", txns);
				
				String goTerms = o.createSIF("GO");
				FileUtil.writeTextFile(homeDir, "GoTerms.SIF", goTerms);
			
				String ubers = o.createSIF("UBERON");
				FileUtil.writeTextFile(homeDir, "Uberons.SIF", ubers);
			
				String pato = o.createSIF("PATO");
				FileUtil.writeTextFile(homeDir, "PATO.SIF", pato);
			
				System.out.println("SIF FILE GENERATED\n" + output);
				
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
	private Ontology readObo(List<String> lines)
	{
		long start = System.currentTimeMillis();
		String term = null;
		int index = 0;
		int end = lines.size();
		if (end == 0) return null;
		Ontology ontology = new Ontology();
		while (term == null && index < end)
		{
			String line = lines.get(index++);
			if (line.startsWith("[Term]"))
			{
				line = lines.get(index++);
				term = line.trim();
				OntologyTerm onterm = new OntologyTerm(term.substring(3).trim());
				while (term != null)
				{
					line = lines.get(index++);
					if (line.trim().isEmpty())
						term = null;
					else onterm.add(line);
				}
				ontology.addTerm(onterm);

			}
		}
		System.out.println("Parsing: " + (System.currentTimeMillis() - start));

		return ontology;
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
		File file = chooser.showOpenDialog(anchor.getScene().getWindow());
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
