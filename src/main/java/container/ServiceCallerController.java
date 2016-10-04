package container;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import container.gmapsfx.MapBorderPane;
import gui.Borders;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.FileUtil;
import util.StringUtil;

  
public class ServiceCallerController implements Initializable
{
	@FXML private ListView<File> list;
	@FXML private TreeTableView<String> xmlTree;
	@FXML TreeTableColumn<TreeTableView, String>  col0;
	@FXML TreeTableColumn<TreeTableView, String>  col1;
	@FXML private Pane content;
	@FXML private Pane streetViewPane;
	@FXML private TextField address;
	@FXML private Button search;

	
	@Override	public void initialize(URL location, ResourceBundle resources)
	{
		//setupDropPane();
		
//		list.setCellFactory(p -> new FileListCell());
		content.setBorder(Borders.blueBorder1);
		content.getChildren().add(new MapBorderPane());
//		streetViewPane.getChildren().add(new StreetViewBorderPane());
	}
	//--------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------
	@FXML void doSearch()
	{ 
		String addr = address.getText();
		System.out.println(addr);

		WebView webView = new WebView();
		WebEngine webEngine = webView.getEngine();
		final String urlGoogleMaps = "https://google-developers.appspot.com/maps/documentation/javascript/examples/full/layer-heatmap";
		webEngine.load(urlGoogleMaps);
		webEngine.setJavaScriptEnabled(true);
		content.getChildren().clear();
		content.getChildren().add(webView);
	}

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
		File file = chooser.showOpenDialog(content.getScene().getWindow());
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
//	//--------------------------------------------------------------------------------
//	static public void openFile(File f)
//	{
//		try
//		{
//			AppContainer.getInstance().getHostServices().showDocument(f.toURI().toURL().toExternalForm());
//		}
//		catch (Exception e){}
//	}
//	
//	private File findFile(String name)
//	{
//		for (File f : list.getItems())
//			if (f.getName().toLowerCase().equals(name.toLowerCase()))	
//				return f;
//		return null;
//	}
//	// --------------------------------------------------------------------------------
//	//--------------------------------------------------------------------------------
//
//	void addFiles(DragEvent ev)
//	{
//		Dragboard db = ev.getDragboard();
//		double x = ev.getX();
//		double y = ev.getY();
//		List<File> files = db.getFiles();
//		for (File f : files)
//		{
//			Label label = new Label(f.getName());
//			AnchorPane.setLeftAnchor(label, x);
//			AnchorPane.setTopAnchor(label, y);
//			y += 20;
//			label.setPadding(new Insets(10,10,10,10));
//			label.setBorder(Borders.etchedBorder);
////			anchor.getChildren().add(label);
//			list.getItems().add(f);
//			if (FileUtil.isXML(f))
//			{
////				TreeItem<String> xml = FileUtil.getXMLtree(f);
////				xmlTree.setRoot(xml);
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
//	}
//	//--------------------------------------------------------------------------------
//	
//	double dragX, dragY;
//	void makeDraggable(Node n)
//	{
//		n.setOnMouseEntered(e -> {	setBackground(n, "#FFF0FF");	});
//		n.setOnMouseExited(e -> {	setBackground(n, "white");	});
//		n.setOnMousePressed(e -> {	dragX = e.getX();	dragY = e.getY();	});
//		n.setOnMouseDragged(e -> {
//			n.setTranslateX(n.getTranslateX() + e.getX() - dragX);	
//			n.setTranslateY(n.getTranslateY() + e.getY() - dragY);	
//		});
//	}
//	
//	void setBackground(Node n, String colorStr)
//	{
//		n.setStyle("-fx-background-color: " + colorStr + "; ");
//	}
//	
//	void addTooltip(Node n, File f)
//	{
//		Tooltip.install(n, new Tooltip(FileUtil.getTextDescription(f)));
//	}
//	
//	// set the background on a single click, and launch the file on a double
//	void makeDoubleClickOpen(Node n)
//	{
//        n.setOnMouseClicked(e -> {
//        	EventTarget t = e.getTarget();
//        	int clickCt = e.getClickCount();
//            if(e.getButton().equals(MouseButton.PRIMARY))
//                if(clickCt == 1 && t instanceof Label)
//				{
//					Label lab = (Label) t;
//					setBackground(lab, "#FFD0FF");
//                }
//                if(clickCt == 2 && t instanceof Label)
//                 {
//					Label lab = (Label) t;
//					setBackground(lab,"white");
//					File f = findFile(lab.getText());
//					if (f != null)
//						openFile(f);
//                 }
//        	});
//	}
//
//	private static class FileListCell extends ListCell<File>
//	{
//		@Override public void updateItem(File item, boolean empty)
//		{
//			super.updateItem(item, empty);
//			if (empty)			{		setGraphic(null);			setText(null);		} 
//			else
//			{
//				Image fxImage = FileUtil.getFileIcon(item.getName());
//				ImageView imageView = new ImageView(fxImage);
//				setGraphic(imageView);
//				setText(item.getName());
//			}
//		}
//	  }
}
