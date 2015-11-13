package container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import database.forms.EntrezForm;
import diagrams.draw.App;
import gui.Backgrounds;
import gui.Effects;
import gui.TabPaneDetacher;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CSVTableData;
import model.Histogram1D;
import model.Range;
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
	@FXML TableView<DataRow> csvtable;

	ObservableList<String> speciesList = FXCollections.observableArrayList("Mouse", "Human", "More...");
	ObservableList<String> cellTypes = FXCollections.observableArrayList("T Cells", "B Cells", "NK Cells", "More...");
	ObservableList<String> technologyList = FXCollections.observableArrayList("ChipCytometry", "PCR", "Mass Spec", "HPLC", "More...");

	@FXML Button plotButton;

	static String[] suppressNames = new String[]{ "SpecificParameters", "Environment", "Machine", "MethodHistory"};

	//-------------------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);

		//Hypothesis---------
		species.setItems(speciesList);
		species.getSelectionModel().selectFirst();
		celltype.setItems(cellTypes);
		celltype.getSelectionModel().selectFirst();
		technology.setItems(technologyList);
		technology.getSelectionModel().selectFirst();

		//Research---------
		research.getChildren().add(querier);
		AnchorPane.setBottomAnchor(querier, 10d);
		AnchorPane.setTopAnchor(querier, 10d);
		AnchorPane.setLeftAnchor(querier, 10d);
		AnchorPane.setRightAnchor(querier, 10d);

		//Methods---------
		setupDropPane();
		setupXMLTree();
		fileTree = new XMLFileTree(null);
		fileTreeBox.getChildren().add(fileTree);

		fileTree.getSelectionModel().selectedItemProperty().addListener((obs, old, val) ->
		{
			File f = val.getValue();
			TreeItem<org.w3c.dom.Node> xml = FileUtil.getXMLtree(f, suppressNames);
			xmlTree.setRoot(xml);
		});
		
		//Results --------- there is an overlay of an ImageView and TableView, so show/hide on selection change
		setupCSVTable();
		scans.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			image.setImage(val.getImage());
			image.setVisible(true);
			csvtable.setVisible(false);
		});
		segments.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			install(val.getData());
			image.setVisible(false);
			csvtable.setVisible(true);
		});
	}

	//-------------------------------------------------------------------------------------------
	public void install(CSVTableData inData)
	{
		if (inData != null)
			System.out.println("install table here " + inData.getData().size());
		
		int nCols = inData.getData().size();
		
		csvtable.getColumns().clear();
		if (inData.getColumnNames().size() == 0)
			for (int i=0;i<nCols;i++)
				inData.getColumnNames().add("#" + i);

        TableColumn<DataRow, Integer> rowNumColumn = new TableColumn<>("Row#");  
        rowNumColumn.setCellValueFactory(cellData -> cellData.getValue().getRowNum().asObject());
		csvtable.getColumns().add(rowNumColumn);
		for (int i=0;i<nCols;i++)
		{
            String name = inData.getColumnNames().get(i);
            TableColumn<DataRow, Integer> newColumn = new TableColumn<>(name);  
            final int j = i;
            newColumn.setCellValueFactory(cellData -> cellData.getValue().get(j).asObject());
			csvtable.getColumns().add(newColumn);
		}
		csvtable.getItems().clear();

		int nRows = inData.getData().get(0).size();
		for (int row=0; row<nRows; row++)
		{
			DataRow newRow = new DataRow(nCols);
			newRow.setRowNum(row);
			for (int i=0;i<nCols;i++)
			{
				String s = inData.getData(i).get(row);
				Integer k = StringUtil.toInteger(s);
				newRow.set(i, k);
			}
			csvtable.getItems().add(newRow);
		}
	}

	//--------------------------------------------------------------------------------
	@FXML private void doPlot()
	{
		System.out.println("doPlot");
		ObservableList<DataRow> data = csvtable.getItems();
		if (data == null) return;
		int nRows = data.size();
		if (nRows == 0) return;
		DataRow row0 = data.get(0);
		int nCols = row0.getWidth();
		
		int[] mins = new int[nCols];
		int[] maxs = new int[nCols];
		for (int row=0; row<nRows; row++)		// scan for ranges of all columns
		{
			DataRow aRow = data.get(row);
			for (int i=0;i<nCols;i++)
			{
				Integer s = aRow.get(i).get();
				mins[i] = Math.min(mins[i],  s);
				maxs[i] = Math.max(maxs[i],  s);
			}
		}

		List<Histogram1D> histos = new ArrayList<Histogram1D>();
		for (int i=0;i<nCols; i++)
		{
			if (i < 2) continue;		// first two columns are postion info, I think
			Histogram1D hist = new Histogram1D(200, new Range(mins[i], maxs[i]), false);
			for (int row=0; row<nRows; row++)
			{
				DataRow aRow = data.get(row);
				Integer s = aRow.get(i).get();
				hist.count(s);
			}
			// chop of the tails on both sides and remake the histogram
			double percentile1 = hist.getPercentile(1);
			double percentile99 = hist.getPercentile(99);
			if (percentile1 == percentile99) continue;
			hist = new Histogram1D(200, new Range(percentile1,percentile99), false);
			for (int row=0; row<nRows; row++)
			{
				DataRow aRow = data.get(row);
				Integer s = aRow.get(i).get();
				hist.count(s);
			}
			histos.add(hist);
		}
		fillChartBox(histos);
	}
	
	private void fillChartBox(List<Histogram1D> histos)
	{
		if (chartBox != null) resultsAnchor.getChildren().remove(chartBox);
		chartBox = new VBox(8);
		for (Histogram1D histo : histos)
		{
			Range r = histo.getRange();
			System.out.println("Histogram has range of " + r.min() + " - " + r.max());
			if (r.width() < 35) continue;
			
//			double roundedMin = ((int) r.min() / 100 ) * 100;
//			double roundedMax = ((int) r.max() / 100 ) * 100;
//			double unit = (roundedMax - roundedMin) / 5;
			NumberAxis  xAxis = new NumberAxis();		//roundedMin, roundedMax, unit
			NumberAxis  yAxis = new NumberAxis();
			LineChart<Number, Number>  chart = new LineChart<Number, Number>(xAxis, yAxis);
			chart.setTitle("Histogram Chart");
			chart.setCreateSymbols(false);
			chart.getData().add( histo.getDataSeries());	
			chart.setTitle("");
			chart.setLegendVisible(false);
			chart.setPrefHeight(100);
			chartBox.getChildren().add(chart);
		}
		resultsAnchor.getChildren().add(0,chartBox);
		AnchorPane.setRightAnchor(chartBox, 50.);
	}
	VBox chartBox;
//--------------------------------------------------------------------------------

	private void setupDropPane()
	{
		tocTabPane.setOnDragEntered(e ->
		{
			tocTabPane.setEffect(Effects.innershadow);
			tocTabPane.setBackground(Backgrounds.tan);
			e.consume();
		});
		// drops don't work without this line!
		tocTabPane.setOnDragOver(e ->	{	e.acceptTransferModes(TransferMode.ANY);  e.consume();	});
		
		tocTabPane.setOnDragExited(e ->
		{
			tocTabPane.setEffect(null);
			tocTabPane.setBackground(Backgrounds.white);
			e.consume();
		});
		
		tocTabPane.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);
			Dragboard db = e.getDragboard();
			Set<DataFormat> formats = db.getContentTypes();
			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
			tocTabPane.setEffect(null);
			tocTabPane.setBackground(Backgrounds.white);
			if (db.hasFiles())  addFiles(e);
		});
	}
	//--------------------------------------------------------------------------------
	// specifically adding an experiment folder with xmls and subfolders
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
				}
				
				break;			// only adds the first directory, then breaks
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
	//--------------------------------------------------------------------------------
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

	//--------------------------------------------------------------------------------
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
		try
		{
			Segment seg = new Segment(id, gkid);		// read the file, build the table
			segments.getItems().add(seg);
		}
		catch (Exception e) {			e.printStackTrace();		}
	}
	//--------------------------------------------------------------------------------

	private void setupCSVTable()
	{
		assert (csvtable != null);
	}
	
	public class DataRow 	 // public because you get access to properties
	{
	    IntegerProperty rowNum = new SimpleIntegerProperty(0);
	    private ObservableList<SimpleIntegerProperty> vals = FXCollections.observableArrayList();
		
	    public DataRow(int nCols)
		{
	        for(int i=0; i<nCols; ++i)
	        	vals.add(new SimpleIntegerProperty(0));
		}
	    public IntegerProperty getRowNum()	{	return rowNum;		}
		public void setRowNum(int row)		{ 	rowNum.set(row); } 
		public void set(int i, Integer s)	{ 	vals.get(i).set(s);	} 
	    public IntegerProperty get(int i) 	{ 	return vals.get(i); }
	    public int getWidth()				{ 	return vals.size();	}
	}
	
	//--------------------------------------------------------------------------------

	private void setupXMLTree()
	{
		assert (xmlTree != null);
		xmlTree.setFixedCellSize(30);
		xmlTree.setShowRoot(false);
		// --- name column---------------------------------------------------------
		TreeTableColumn<org.w3c.dom.Node, String> nameColumn = new TreeTableColumn<org.w3c.dom.Node, String>("Name");
		nameColumn.setPrefWidth(500);
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
	}
	
	String lookup(String orig)
	{
		if (orig == null) return "";
		if (orig.equals("Inputobjects"))			return "Input";
		if (orig.equals("Outputobjects"))			return "Output";
		if (orig.equals("EncapsulatedObjects"))		return "Objects";
		if (orig.equals("EncapsulatedMethods"))		return "Steps";
		if (orig.equals("SpecificParameter"))		return "Parameter";
		if (orig.equals("SpecificParameters"))		return "Parameters";
		if (orig.equals("ObjectConnector"))			return "Connection";
		if (orig.equals("EncapsulatedObjectsRef"))	return "References";
		if (orig.equals("EncapsulatedMethodsRef"))	return "References";
		if (orig.equals("Meth"))					return "Method";
		if (orig.equals("MethRef"))					return "Reference";
		if (orig.equals("MethodHistory"))			return "History";
		if (orig.equals("ObjRef"))					return "Reference";
		if (orig.equals("Obj"))						return "Object";
		if (orig.equals("PrimaryContainer"))		return "Container";
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
	
//	void addTooltip(Node n, File f)
//	{
//		Tooltip.install(n, new Tooltip(FileUtil.getTextDescription(f)));
//	}
	
	// set the background on a single click, and launch the file on a double
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
////					File f = findFile(lab.getText());
////					if (f != null)
////						openFile(f);
//                 }
//        	});
//	}
	//--------------------------------------------------------------------------------
	class Segment
	{
		String id;
		File csvFile;
		CSVTableData data;
		
		Segment(String inID, File inFile)
		{
			id = inID;
			csvFile = inFile;
			if (csvFile != null)		// read table
			{
				data = readBadCSVfile(csvFile);		// its actually tab-separated
			} 
		}
		
		private CSVTableData readBadCSVfile(File f)
		{
			CSVTableData tableData = new CSVTableData();
			try
			{
				FileInputStream fis = new FileInputStream(f);
				//Construct BufferedReader from InputStreamReader
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				List<ObservableList<String>> idata = tableData.getData();
				String line = null;
				
				line = br.readLine();
				line = br.readLine();
				String[] row = line.split("\t"); 
				int len = row.length;
				for (int i=0; i<len; i++)
					tableData.getData().add(FXCollections.observableArrayList());

				while (line != null) {
//					System.out.println(line);
					row = line.split("\t");  
					if (row.length != len)	throw new IllegalArgumentException();		// there must be the same number of tabs in every row
					for (int i = 0; i< row.length; i++)
					{
						idata.get(i).add(row[i]);
						System.out.println(row[i]);
					}
					line = br.readLine();
				}
			 
				br.close();
			}
			catch (Exception e)	{ e.printStackTrace();	}
			
			return tableData;
		}
		public String toString()		{	return id + ": " + csvFile.getName();		}
		public CSVTableData getData()		{	return data;		}
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
