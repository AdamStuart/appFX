package publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import database.forms.EntrezForm;
import database.forms.EntrezRecord;
import diagrams.draw.App;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import gui.TabPaneDetacher;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
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
	@FXML SplitPane resultsplitter;
	@FXML TableView<DataRow> csvtable;
	ObservableList<String> speciesList = FXCollections.observableArrayList("Mouse", "Human", "More...");
	ObservableList<String> cellTypes = FXCollections.observableArrayList("T Cells", "B Cells", "NK Cells", "More...");
	ObservableList<String> technologyList = FXCollections.observableArrayList("ChipCytometry", "PCR", "Mass Spec", "HPLC", "More...");

	@FXML Button plotButton;

	static String[] suppressNames = new String[]{ "SpecificParameters", "Environment", "Machine", "MethodHistory"};		// close the disclosure triangle, as they may be big
	public static Application getApplication()	{		return AppPublish.getInstance();	}
	public static Stage getStage()				{		return AppPublish.getStage();	}
	private PublishDocument doc;
//	private PublishDocument getDocument()		{ return doc;	}

	@FXML void showMethodsTree()	{		new MethodsTree(fileTree.getRoot());	}

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
			resultsplitter.setVisible(false);
		});
		segments.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			installSegmentTable(val.getData());
			image.setVisible(false);
			resultsplitter.setVisible(true);
		});
	}

	//-------------------------------------------------------------------------------------------
	// TODO -- persistence is not finished.  Questions about enclosing data vs. referring to it.
	
	List<XMLEvent> steps;
	XMLOutputFactory factory      = XMLOutputFactory.newInstance();
	XMLEventFactory  werk = XMLEventFactory.newInstance();

	public void saveas()		
	{ 	
		doc.reset();
		save();	
	}
	
	@FXML private void save()
	{
		File f = doc.getSaveDestination();
		if (f != null)
		{
			steps = new ArrayList<XMLEvent>();
			steps.add(werk.createStartElement( "", "", "Publication"));
			extractState(); 
			extractHypothesis();
			extractResearch();
			extractMethods();
			extractResults();
			extractDiscussion();
			steps.add(werk.createEndElement( "", "", "Publication"));
			XMLFactory.writeEvents(steps, f.getAbsolutePath());
		}
	}
	private void extractState()
	{
		// window positions, active tab, selections, etc
		steps.add(werk.createStartElement( "", "", "State"));
		steps.add(werk.createAttribute( "active", "Methods"));
		steps.add(werk.createAttribute( "x", "300"));
		steps.add(werk.createAttribute( "y", "400"));
		steps.add(werk.createEndElement( "", "", "State"));
	}
	
	private void extractHypothesis()
	{
		steps.add(werk.createStartElement( "", "", "Hypothesis"));
		steps.add(werk.createAttribute("species", species.getSelectionModel().getSelectedItem()));
		steps.add(werk.createAttribute("celltype", celltype.getSelectionModel().getSelectedItem()));
		steps.add(werk.createAttribute("technology", technology.getSelectionModel().getSelectedItem()));
		steps.add(werk.createStartElement( "", "", "Keywords"));
		steps.add(werk.createCData(keywords.getText()));
		steps.add(werk.createEndElement( "", "", "Keywords"));
		steps.add(werk.createStartElement( "", "", "Content"));
		steps.add(werk.createCData(hypothesis.getHtmlText()));
		steps.add(werk.createEndElement( "", "", "Content"));
		steps.add(werk.createEndElement( "", "", "Hypothesis"));
	}
	
	private void extractResearch()
	{
		steps.add(werk.createStartElement( "", "", "Research"));
		String query = querier.extractPlain();
		if (!StringUtil.isEmpty(query) )
		{
			steps.add(werk.createStartElement( "", "", "Query"));
			steps.add(werk.createCData(query));
			steps.add(werk.createEndElement( "", "", "Query"));

		}
		ObservableList<EntrezRecord> items = querier.getItems();
		for (EntrezRecord item : items)
		{
			if (item.getPMID() != null) 
			{
				steps.add(werk.createStartElement( "", "", "Item"));
				steps.add(werk.createAttribute("PMID", item.getPMID()));
				steps.add(werk.createEndElement( "", "", "Item"));
			}
		}
		steps.add(werk.createEndElement( "", "", "Research"));
	}
	
	private void extractMethods()
	{
		steps.add(werk.createStartElement( "", "", "Methods"));
		if (methodsPath != null) 
		{
			steps.add(werk.createStartElement( "", "", "File"));
			steps.add(werk.createAttribute("path", getMethodsFilePath()));
			steps.add(werk.createEndElement( "", "", "File"));
		}
		steps.add(werk.createEndElement( "", "", "Methods"));
	}
	
	String methodsPath = null;
	private String getMethodsFilePath()			{		return methodsPath;	}
	private void setMethodsFilePath(String inS)	{		methodsPath = inS;	}
	
	private void extractResults()
	{
		steps.add(werk.createStartElement( "", "", "Results"));
		steps.add(werk.createEndElement( "", "", "Results"));
	}
	
	private void extractDiscussion()
	{
		steps.add(werk.createStartElement( "", "", "Discussion"));
		steps.add(werk.createCData(discussion.getHtmlText()));
		steps.add(werk.createEndElement( "", "", "Discussion"));
	}
	
	//-------------------------------------------------------------------------------------------
	private void setActiveTab(String active)
	{
		for (Tab tab : tocTabPane.getTabs())
			if (tab.getText().equals(active))	
			{
				tocTabPane.getSelectionModel().select(tab);
				break;
			}
	}


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
			String active = getAttribute(elem, "active");
			setActiveTab(active);
			double x = getDoubleAttribute(elem, "x");
			double y = getDoubleAttribute(elem, "y");
			Stage stage = getStage();
			stage.setX(x);
			stage.setY(x);
		}
	}
	
	public double getDoubleAttribute(org.w3c.dom.Node elem, String attrName)
	{
		return StringUtil.toDouble(getAttribute(elem, attrName));
	}
	
	public String getAttribute(org.w3c.dom.Node elem, String attrName)
	{
		if (elem == null) return null;
		org.w3c.dom.Node item = elem.getAttributes().getNamedItem(attrName);
		return (item == null) ? null : item.getTextContent();
	}
	
	public org.w3c.dom.Node getChild(org.w3c.dom.Node elem, String childName)
	{
		if (elem == null) return null;
		NodeList children = elem.getChildNodes();
		int sz = children.getLength();
		for (int i=0; i<sz; i++)
		{
			org.w3c.dom.Node child = children.item(i);
			if (child == null)  continue;
			if (childName.equals(child.getTextContent()))
			return child;
		}
		return null;
	}
	
	private void setHypothesis(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			String Species = getAttribute(elem, "species");
			String Celltype = getAttribute(elem, "celltype");
			String Technology = getAttribute(elem, "technology");
			species.getSelectionModel().select(Species);
			celltype.getSelectionModel().select(Celltype);
			technology.getSelectionModel().select(Technology);

			NodeList children = elem.getChildNodes();
			int sz = children.getLength();
			for (int i=0; i<sz; i++)
			{
				org.w3c.dom.Node child = children.item(i);
				if (child == null)  continue;
				if ("Keywords".equals(child.getTextContent()))
				{
					
				}
				if ("Content".equals(child.getTextContent()))
				{
					
				}
			}
					
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
		if (inData == null) return;
		int nCols = inData.getData().size();
		System.out.println("installing table with " + nCols + " columns");
		
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
			if (i < 2) continue;		// first two columns are position info, I think. skip them
			Histogram1D hist = new Histogram1D("temp" , new Range(mins[i], maxs[i]));
			for (int row=0; row<nRows; row++)		// first pass to calculate tails
			{
				DataRow aRow = data.get(row);
				Integer s = aRow.get(i).get();
				hist.count(s);
			}
			// chop of the tails on both sides and remake the histogram
			double percentile1 = hist.getPercentile(1);
			double percentile99 = hist.getPercentile(99);
			if (percentile1 == percentile99) continue;
			hist = new Histogram1D("#" + i, new Range(percentile1,percentile99));
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
		graphVBox.getChildren().clear();
		for (Histogram1D histo : histos)
		{
			Range r = histo.getRange();
			System.out.println("Histogram has range of " + r.min() + " - " + r.max());
			if (r.width() < 50) continue;
			LineChart<Number, Number> chart = histo.makeChart();
			graphVBox.getChildren().add(chart);
			VBox.setVgrow(chart, Priority.ALWAYS);
		}
	}
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
				setEDLDirectory(f);
				break;			//  add the first directory, then break
			}
		}
	}
	//--------------------------------------------------------------------------------
	private void setEDLDirectory(File f)
	{
		setMethodsFilePath(f.getAbsolutePath());
		fileTree.setRoot(f);	// traverse down the file system tree, adding everything	
		
		for (File child : f.listFiles())
		{
			if (child.isDirectory())		// add sub-directories to the results tab
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
	}
	
	private void addScanJobsDirectory(File f)
	{
		for (File kid : f.listFiles())
		{
			if (FileUtil.isImageFile(kid))
			{
				String id = kid.getParentFile().getParentFile().getParentFile().getName();
				scans.getItems().add(new ScanJob(id, kid));
			}
			else if (kid.isDirectory())
				addScanJobsDirectory(kid);
		}
	}

	//--------------------------------------------------------------------------------
	private void addSegmentsDirectory(File f)
	{
		try
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
							segments.getItems().add(new Segment(id, gkid));		// read the file, build the table
				}
			}
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
		EDLParsingHelper helper = new EDLParsingHelper(xmlTree);
		helper.setupDictionary();
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
				data = readBadCSVfile(csvFile);		// its actually tab-separated
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
				
				line = br.readLine();		// first line is text labels, but not in columns
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

	// TODO -- There's no menu bar at this point, but here are placeholders we'll need
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
	
	void open(File f)	{			}		//TODO
	@FXML void doClose(){	save();		}	//TODO -- pbly obsolete if we install a close handler!
	@FXML void doZip()	{	}	//TODO
	
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
	@FXML void doUndo()	{		}
	@FXML void doRedo()	{	}
	@FXML void doCut()	{	}
	@FXML void doCopy()	{	}
	@FXML void doPaste()	{	}
	@FXML void doCompare()	{ 	}
}