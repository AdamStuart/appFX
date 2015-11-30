package publish;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import database.forms.EntrezForm;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import gui.TabPaneDetacher;
import icon.FontAwesomeIcons;
import icon.GlyphsDude;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CSVTableData;
import model.Histogram1D;
import model.Population;
import model.Range;
import table.codeOrganizer.TreeTableModel;
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
	@FXML ChoiceBox<String> species;
	@FXML ChoiceBox<String> celltype;
	@FXML ChoiceBox<String> technology;
	@FXML TextArea keywords;
	@FXML HTMLEditor hypothesis;
	@FXML HTMLEditor discussion;
	@FXML ListView<String> normalizeList;
	@FXML ListView<String> interrogateList;
	@FXML ListView<String> visualizeList;
	@FXML private TreeTableView<Population> classifyTree;

	EntrezForm querier = new EntrezForm();

	
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
		//Analysis --------- 
		setupAnalysis();
	}

	public void start()
	{
		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);
	}
	//-------------------------------------------------------------------------------------------
	// TODO -- persistence is not finished.  Questions about enclosing data vs. referring to it.
	
	private List<XMLEvent> steps;
	private XMLEventFactory  werk = 	XMLEventFactory.newInstance();

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
			extractAnalysis();
			extractDiscussion();
			steps.add(werk.createEndElement( "", "", "Publication"));
			XMLFactory.writeEvents(steps, f.getAbsolutePath());
		}
	}
	String getActiveTab()	{ return tocTabPane.getSelectionModel().getSelectedItem().getText(); }
	private void extractState()
	{
		// window positions, active tab, selections, etc
		steps.add(werk.createStartElement( "", "", "State"));
		steps.add(werk.createAttribute( "active", getActiveTab()));
		steps.add(werk.createAttribute( "x", "" + tocTabPane.getScene().getWindow().getX()));
		steps.add(werk.createAttribute( "y", "" + tocTabPane.getScene().getWindow().getY()));
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
		querier.setXML(werk, steps);
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
	
	private void extractAnalysis()
	{
		steps.add(werk.createStartElement( "", "", "Analysis"));
		steps.add(werk.createEndElement( "", "", "Analysis"));
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
		setAnalysis(partMap.get("Analysis"));
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
//				Element el = (Element) child;
				if (child == null)  continue;
				if ("Keywords".equals(child.getNodeName()))
					keywords.setText(child.getTextContent());
				if ("Content".equals(child.getNodeName()))
					hypothesis.setHtmlText(child.getTextContent());
			}
		}
	}
	
	private void setResearch(org.w3c.dom.Node elem)
	{
		if (elem != null)
			querier.setXML(elem);
	}
	
	private void setMethods(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			NodeList children = elem.getChildNodes();
			int sz = children.getLength();
			for (int i=0; i<sz; i++)
			{
				org.w3c.dom.Node child = children.item(i);
				if ("File".equals(child.getNodeName()))
				{
					String path = child.getAttributes().getNamedItem("path").getNodeValue();
					if (path != null)
					{
						File f = new File(path);
						EDLParsingHelper.setEDLDirectory(f, xmlTree, scans, segments);
					}
				}
				
			}			
		}
	}
	
	private void setResults(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			
		}
	}
	
	
	private void setAnalysis(org.w3c.dom.Node elem)
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
			if (f.isDirectory())
			{		
				setMethodsFilePath(f.getAbsolutePath());
				fileTree.setRoot(f);	// traverse down the file system tree, adding everything	
				EDLParsingHelper.setEDLDirectory(f, xmlTree, scans, segments);
				break;			//  add the first directory, then break
			}
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
// Analysis commands
	
	String[] strs = new String[] { "Filter Samples", "Organize Files", "Image Processing", "Edge Detection", "Feature Recognition", "Quantification", "Parametric Normalization", "Labeling"};
	String[] interrog = new String[] { "Activation", "Stimulation", "Memory", "Expression", "Regulation", "Promotion", "Inhibition", "Apoptosis" };
	String[] viz = new String[] { "QC Montage", "Stats Panel", "Backgating", "Correlation", "Heat Map", "Hover Plot", "Drill Down Chart", "Tree Map", "Anova", "Cytoscape", "VISNE", "SPADE"};
	@FXML	private TreeTableColumn<Population, String> nameColumn;
	@FXML	private TreeTableColumn<Population, String> countColumn;

	private void setupAnalysis()
	{
		normalizeList.setItems(FXCollections.observableArrayList(strs));
		normalizeList.setCellFactory(item -> new StepCell());
		visualizeList.setItems(FXCollections.observableArrayList(viz));
		classifyTree.setRoot(TreeTableModel.getCellPopulationTree());
		nameColumn.setPrefWidth(200);	 
		nameColumn.setCellValueFactory(p -> {
            Population pop = p.getValue().getValue();  
            return new ReadOnlyObjectWrapper<String>(pop.getName());
		});
		countColumn.setCellValueFactory(p -> {
            Population pop = p.getValue().getValue();  
            return new ReadOnlyObjectWrapper(pop.getCount());
		});
		
		interrogateList.setItems(FXCollections.observableArrayList(interrog));
	}
	 public class StepCell extends ListCell<String> {

	     public StepCell() {    }
	       
	     @Override protected void updateItem(String item, boolean empty) {
	         // calling super here is very important - don't skip this!
	         super.updateItem(item, empty);
	           
	         setText(item == null ? "" : item);
	         Text icon = GlyphsDude.createIcon(FontAwesomeIcons.CHECK);
	         icon.setFill(Color.GREEN);
	         setGraphic(item == null ? null : icon);

	     }
	 }

	
	@FXML void doExplore()	{	System.out.println("doExplore: ");	}
	@FXML void doBatch()	{	System.out.println("doBatch: ");	}
	@FXML void doMonitor()	{	System.out.println("doMonitor: ");	}
	@FXML void doConfigure(){  	System.out.println("doConfigure: ");		}

	//--------------------------------------------------------------------------------

	@FXML MenuItem recentMenu;
	
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
		Document doc = PublishDocument.open();
		if (doc != null)
			install(doc);
	}
	
	@FXML void doClose(){	save();		}	//TODO -- pbly obsolete if we install a close handler!
	@FXML void doZip()	{	}	
	
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
	@FXML void doQuit()		{	System.exit(0);}
	@FXML void doUndo()		{	}
	@FXML void doRedo()		{	}
	@FXML void doCut()		{	}
	@FXML void doCopy()		{	}
	@FXML void doPaste()	{	}
	@FXML void doCompare()	{ 	}
}
