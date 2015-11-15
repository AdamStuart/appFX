package container.publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import database.forms.EntrezForm;
import diagrams.draw.App;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import gui.TabPaneDetacher;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CSVTableData;
import model.Histogram1D;
import model.Range;
import util.FileUtil;
import util.StringUtil;
import xml.XMLFactory;
import xml.XMLFileTree;

  
public class PublishController implements Initializable
{
	@FXML TabPane tocTabPane;
	@FXML AnchorPane research;
	@FXML AnchorPane abstractAnchor;
	@FXML AnchorPane methodsAnchor;
	@FXML AnchorPane resultsAnchor;
	@FXML VBox graphVBox;
	@FXML ImageView image;
	EntrezForm querier = new EntrezForm();
	@FXML  ChoiceBox<String> species;
	@FXML  ChoiceBox<String> celltype;
	@FXML  ChoiceBox<String> technology;
	@FXML  TextArea keywords;
	@FXML  HTMLEditor hypothesis;
	@FXML  HTMLEditor discussion;
	
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
	public static Application getApplication()	{		return AppPublish.getInstance();	}
	public static Stage getStage()		{		return AppPublish.getStage();	}
	PublishDocument doc;
	PublishDocument getDocument()		{ return doc;	}

	class NodeTreeItem extends TreeItem<org.w3c.dom.Node> {};

	//-------------------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
//		discussion.setId("id");
		doc = new PublishDocument(this);
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
		fileTreeBox.setBorder(Borders.greenBorder);
		VBox.setVgrow(fileTree, Priority.ALWAYS);
		AnchorPane.setBottomAnchor(fileTreeBox, 30d);

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
			installSegmentTable(val.getData());
			image.setVisible(false);
			csvtable.setVisible(true);
		});
	}

	//-------------------------------------------------------------------------------------------
	List<XMLEvent> steps;
	XMLOutputFactory factory      = XMLOutputFactory.newInstance();
	XMLEventFactory  eventFactory = XMLEventFactory.newInstance();

	public void saveas()		
	{ 	
		doc.reset();
		save();	
	}
	
	public void save()
	{
		File f = doc.getSaveDestination();
		if (f != null)
		{
			steps = new ArrayList<XMLEvent>();
			extractState(); 
			extractHypothesis();
			extractResearch();
			extractMethods();
			extractResults();
			extractDiscussion();
			XMLFactory.writeEvents(steps, f.getAbsolutePath());
		}
	}
	private void extractState()
	{
		// window positions, active tab, selections, etc
		steps.add(eventFactory.createStartElement( "", "", "State"));
		steps.add(eventFactory.createAttribute( "active", "Methods"));
		steps.add(eventFactory.createAttribute( "x", "300"));
		steps.add(eventFactory.createAttribute( "y", "400"));
		steps.add(eventFactory.createEndElement( "", "", "State"));
	}
	
	
	private void extractHypothesis()
	{
		steps.add(eventFactory.createStartElement( "", "", "Hypothesis"));
		steps.add(eventFactory.createAttribute("species", species.getSelectionModel().getSelectedItem()));
		steps.add(eventFactory.createAttribute("celltype", celltype.getSelectionModel().getSelectedItem()));
		steps.add(eventFactory.createAttribute("technology", technology.getSelectionModel().getSelectedItem()));
		steps.add(eventFactory.createStartElement( "", "", "Keywords"));
		steps.add(eventFactory.createCData(keywords.getText()));
		steps.add(eventFactory.createEndElement( "", "", "Keywords"));
	}
	
	private void extractResearch()
	{
		steps.add(eventFactory.createStartElement( "", "", "Research"));
		steps.add(eventFactory.createEndElement( "", "", "Research"));
	}
	
	private void extractMethods()
	{
		steps.add(eventFactory.createStartElement( "", "", "Methods"));
		steps.add(eventFactory.createEndElement( "", "", "Methods"));
	}
	
	private void extractResults()
	{
		steps.add(eventFactory.createStartElement( "", "", "Results"));
		steps.add(eventFactory.createEndElement( "", "", "Results"));
	}
	
	private void extractDiscussion()
	{
		steps.add(eventFactory.createStartElement( "", "", "Discussion"));
		steps.add(eventFactory.createCData(discussion.getHtmlText()));
		steps.add(eventFactory.createEndElement( "", "", "Discussion"));
	}
	
	//-------------------------------------------------------------------------------------------


	public void install(Document doc)
	{
		if (doc == null) return;
		Element experiment = doc.getDocumentElement();
		Map<String, org.w3c.dom.Node> partMap = XMLFactory.readElements(experiment);
		setState(partMap.get("State"));
		setHypothesis(partMap.get("Hypothesis"));
		setResearch(partMap.get("Research"));
		setMethods(partMap.get("Methods"));
		setResults(partMap.get("Results"));
		setDiscussion(partMap.get("Discussion"));
	}
	
	private void setState(org.w3c.dom.Node elem)
	{
		// window positions, active tab, selections, etc
		if (elem != null)
		{
			
		}
	}
	private void setHypothesis(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			
		}
	}
	
	private void setResearch(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			
		}
	}
	
	private void setMethods(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			
		}
	}
	
	private void setResults(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			
		}
	}
	
	private void setDiscussion(org.w3c.dom.Node elem)
	{
		if (elem != null)
			if (elem instanceof Element)
				discussion.setHtmlText(((Element)elem).getTextContent());
	}
	
	
	//-------------------------------------------------------------------------------------------
	public void installSegmentTable(CSVTableData inData)
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
			if (i < 2) continue;		// first two columns are position info, I think
			Histogram1D hist = new Histogram1D("#" + i, 100, new Range(mins[i], maxs[i]), false);
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
			hist = new Histogram1D("#" + i, 200, new Range(percentile1,percentile99), false);
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
//		if (chartBox != null) resultsAnchor.getChildren().remove(chartBox);
//		chartBox = new VBox(8);
		graphVBox.getChildren().clear();
		for (Histogram1D histo : histos)
		{
			Range r = histo.getRange();
			System.out.println("Histogram has range of " + r.min() + " - " + r.max());
			if (r.width() < 50) continue;
			
//			double roundedMin = ((int) r.min() / 100 ) * 100;
//			double roundedMax = ((int) r.max() / 100 ) * 100;
//			double unit = (roundedMax - roundedMin) / 5;
			NumberAxis  xAxis = new NumberAxis();		//roundedMin, roundedMax, unit
			NumberAxis  yAxis = new NumberAxis();
			LineChart<Number, Number>  chart = new LineChart<Number, Number>(xAxis, yAxis);
			chart.setTitle(histo.getName());
			chart.setCreateSymbols(false);
			chart.getData().add( histo.getDataSeries());	
			chart.setLegendVisible(false);
			chart.setPrefHeight(100);
			graphVBox.getChildren().add(chart);
			VBox.setVgrow(chart, Priority.ALWAYS);
		}
//		resultsAnchor.getChildren().add(0,graphVBox);
//		AnchorPane.setRightAnchor(chartBox, 50.);
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
	@FXML void showMethodsTree()
	{	
		new MethodsTree(fileTree.getRoot());
		
	}

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
			if (text.length() > 100)
				text = text.substring(0,100) + "...";
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
		setupDictionary();
	}
	
	static Map<String,String> lookupMap = new HashMap<String,String>();
	
	
	void setupDictionary()
	{
		lookupMap.put("Inputobjects", "Input");
		lookupMap.put("Outputobjects", "Output");
		lookupMap.put("PrimaryContainer", "Container");
		lookupMap.put("ObjRef", "Reference");
		lookupMap.put("Obj", "Object");
		lookupMap.put("SpecificParameter", "Parameter");
		lookupMap.put("SpecificParameters", "Parameters");
		lookupMap.put("ObjectConnector", "Connection");
		lookupMap.put("EncapsulatedObjects", "Objects");
		lookupMap.put("EncapsulatedMethods", "Steps");
		lookupMap.put("EncapsulatedObjectsRef", "References");
		lookupMap.put("EncapsulatedMethodsRef", "References");
		lookupMap.put("Meth", "Method");
		lookupMap.put("MethRef", "Reference");
		lookupMap.put("MethodHistory", "History");
	}
		
		
	String lookup(String orig)
	{
		if (orig == null) return "";
		String val = lookupMap.get(orig);
		return (val == null) ? orig : val;
	}
		

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
		public CSVTableData getData()	{	return data;		}
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
			AppPublish.getInstance().doNew(new Stage());
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
}
