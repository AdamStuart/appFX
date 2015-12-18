package publish;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import container.mosaic.refimpl.javafx.MosaicPane;
import database.forms.EntrezForm;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import gui.TabPaneDetacher;
import icon.FontAwesomeIcons;
import icon.GlyphsDude;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CSVTableData;
import model.Histogram1D;
import model.IntegerDataRow;
import model.Population;
import model.Range;
import table.codeOrganizer.TreeTableModel;
import util.FileUtil;
import util.StringUtil;
import xml.XMLFactory;
import xml.XMLFileTree;
import xml.XMLTools;

  
public class PublishController implements Initializable
{
	@FXML TabPane tocTabPane;
	@FXML AnchorPane research;
	@FXML AnchorPane abstractAnchor;
	@FXML AnchorPane methodsAnchor;
	@FXML AnchorPane resultsAnchor;
	@FXML AnchorPane mosaicAnchor;
	@FXML AnchorPane specsAnchor;
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
	@FXML private VBox fileTreeBox;
	EntrezForm querier = new EntrezForm();

	
	@FXML SplitPane resultsplitter;
	@FXML ListView<ScanJob> scans;
	@FXML ListView<Segment> segments;
	@FXML TableView<IntegerDataRow> csvtable;

	// Methods
	@FXML private VBox connectionsBox; 
	@FXML private TreeTableView<org.w3c.dom.Node> xmlTree;
	private XMLFileTree fileTree = new XMLFileTree(null);
	@FXML ListView<SOPLink> soplist = new ListView<SOPLink>();
	
	ObservableList<String> speciesList = FXCollections.observableArrayList("Mouse", "Human", "More...");
	ObservableList<String> cellTypes = FXCollections.observableArrayList("T Cells", "B Cells", "NK Cells", "More...");
	ObservableList<String> technologyList = FXCollections.observableArrayList("ChipCytometry", "PCR", "Mass Spec", "HPLC", "More...");

	//-------------------------------------------------------------------------------------------
	// Results
	@FXML Button plotButton;
	@FXML Button plot2DButton;
	@FXML Button plotAllButton;

	@FXML CheckBox addYOffset;
	@FXML CheckBox showAllColumns;
	@FXML CheckBox showSumCk;
	
	@FXML CheckBox auth;
	@FXML CheckBox scheduled;
	@FXML CheckBox resources;
	@FXML CheckBox qc;
	@FXML CheckBox lucky;
	@FXML Label droplabel;
	

	private BooleanProperty addOffset = new SimpleBooleanProperty(false);
	public BooleanProperty addOffsetProperty() 	{ return addOffset; }
	public boolean getAddOffset()				{ return addOffset.get();}
	public void setAddOffset(boolean b)			{ addOffset.set(b);}
	
	BooleanProperty allColumns = new SimpleBooleanProperty(false);
	public BooleanProperty allColumnsProperty() { return allColumns; }
	public boolean getAllColumns()				{ return allColumns.get();}
	public void setAllColumns(boolean b)		{ allColumns.set(b);}
	
	BooleanProperty showSum = new SimpleBooleanProperty(false);
	public BooleanProperty showSumProperty() 	{ return showSum; }
	public boolean getShowSum()					{ return showSum.get();}
	public void setShowSum(boolean b)			{ showSum.set(b);}
	
	//-------------------------------------------------------------------------------------------
	
	static String[] suppressNames = new String[]{ "SpecificParameters", "Environment", "Machine", "MethodHistory"};		// close the disclosure triangle, as they may be big
	public static Application getApplication()	{		return AppPublish.getInstance();	}
	public static Stage getStage()				{		return AppPublish.getStage();	}
	private PublishDocument doc;
//	private PublishDocument getDocument()		{ return doc;	}

	@FXML void showMethodsTree()	{		new MethodsTree(fileTree.getRoot());	}
	MosaicPane<Region> mosaicPane;
	PublishModel model;
	
	//-------------------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resBundle)
	{
		model = new PublishModel(this);
		auth.setOnAction(e -> showDropLabel());
		scheduled.setOnAction(e -> showDropLabel());
		resources.setOnAction(e -> showDropLabel());
		qc.setOnAction(e -> showDropLabel());
		lucky.setOnAction(e -> showDropLabel());
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
		setupXMLTree();
		setupSOPList();
		setupDropPane();
		fileTreeBox.getChildren().add(fileTree);
		
//		String surface = "model5";		Mosaic Pane seems to be brittle around "bad" input here.
		
		//Results --------- there is an overlay of an ImageView and TableView, so show/hide on selection change
		
		graphVBox.setSpacing(5);
		
		addOffset.bind(addYOffset.selectedProperty());
		allColumns.bind(showAllColumns.selectedProperty());
		showSum.bind(showSumCk.selectedProperty());
		setupCSVTable();
		scans.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			image.setImage(val.getImage());
			image.setVisible(true);
			resultsplitter.setVisible(false);
		});
		segments.getSelectionModel().selectedItemProperty().addListener((obs, old, val)-> {
			if (val != null) installSegmentTable(val.getData());
			image.setVisible(false);
			resultsplitter.setVisible(true);
		});
		//Analysis --------- 
		setupAnalysis();
		setupMosaic();
		
		String agendaUrl = "https://docs.google.com/document/d/1VT5hmAjFJSIQT_1YsJpF6ELrzmJ1iT2Lng0s-DyZnSg/edit?ts=563105e1";
		agendaPage.getEngine().load(agendaUrl);
	}
					
	private void setActiveTab(String active)
	{
		for (Tab tab : tocTabPane.getTabs())
			if (tab.getText().equals(active))	
			{
				tocTabPane.getSelectionModel().select(tab);
				break;
			}
	}
					
	private void showDropLabel()
	{
		boolean showIt = auth.isSelected() && scheduled.isSelected() && resources.isSelected() && lucky.isSelected() && qc.isSelected();
		droplabel.setVisible(showIt);
	}
	
	public Label getLabel(Color color, String id) {
		Label label = new Label();
		label.textProperty().set(id);
		label.textAlignmentProperty().set(TextAlignment.CENTER);
		label.alignmentProperty().set(Pos.CENTER);
		label.setOpacity(1.0);
		label.setTextFill(Color.WHITE);
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16d));
		String style = "-fx-background-color: #" + color.toString().substring(2, 8).toUpperCase() + 
						";-fx-alignment:center;-fx-text-alignment:center;";
		label.setStyle(style);
		label.setManaged(false);
		return label;
	}
	/** Mapping of element id's to labels for later reference when serializing */
	private java.util.Map<String, Label> clientMap = new java.util.HashMap<>();

	
	private void setupSOPList()
	{
		ObservableList<SOPLink> links = FXCollections.observableArrayList();
		links.add(new SOPLink("http://chipcytometry.com/Blog/", "Chip Cytometry SOPs"));
		links.add(new SOPLink("http://www.protocol-online.org/prot/Cell_Biology/Flow_Cytometry__FCM_/", "FACS Protocols"));
		links.add(new SOPLink("https://www.thermofisher.com/us/en/home/referen"
						+ "ces/protocols/cell-and-tissue-analysis/flow-cytometry-protocol.html", "ThermoFisher"));
			soplist.setItems(links);
		soplist.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
			{
				SOPLink link = soplist.getSelectionModel().getSelectedItem();
				if (link != null)
					StringUtil.launchURL(link.getUrl());
			}
		});
		
	}
	
	private void setupMosaic()
	{
		mosaicPane = new MosaicPane<Region>();
		mosaicAnchor.getChildren().add(mosaicPane);
		mosaicPane.prefHeightProperty().bind(mosaicAnchor.heightProperty());
		mosaicPane.prefWidthProperty().bind(mosaicAnchor.widthProperty());
		
		String[] model = { 
						"Methods , 	0, 		0, 		0.50, 	1",  
						"File , 	0.50, 	0, 		0.50, 	0.33", 
						"Inventory, 0.5, 	0.33, 	0.50, 	0.33",  
						"SOPs, 		.5, 	0.66, 	0.50, 	0.33"
						}; 
		Color[] colors = new Color[] { Color.GAINSBORO,   Color.CORAL,   Color.CORNFLOWERBLUE,   Color.DARKSLATEGRAY};
		/** Used to randomize ui element colors */
		Random random = new Random();

		int i = 0;
		for(String def : model) {
			String[] args = def.split("[\\s]*\\,[\\s]*");
			int offset = args.length > 4 ? args.length - 4 : 0;
			String id = args.length == 4 ? "ID" + i : args[0];
			Label l = getLabel(i > 4 ? colors[random.nextInt(5)] : colors[i], id);
			if (verbose) System.out.println("adding " + id);
			mosaicPane.add(l, id, 
				Double.parseDouble(args[offset + 0]), 
				Double.parseDouble(args[offset + 1]),
				Double.parseDouble(args[offset + 2]),
				Double.parseDouble(args[offset + 3]));
			clientMap.put(id, l);
			i++;
		}
      mosaicPane.getEngine().addSurface(mosaicPane.getSurface());
	}
	boolean verbose = false;
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
			extractHypothesis();				// TODO these should be bound, not extracted
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
			String active = XMLTools.getChildAttribute(elem, "active");
			setActiveTab(active);
			double x = XMLTools.getDoubleAttribute(elem, "x");
			double y = XMLTools.getDoubleAttribute(elem, "y");
			Stage stage = getStage();
			stage.setX(x);
			stage.setY(x);
		}
	}
	private void setHypothesis(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			String Species = XMLTools.getChildAttribute(elem, "species");
			String Celltype = XMLTools.getChildAttribute(elem, "celltype");
			String Technology = XMLTools.getChildAttribute(elem, "technology");
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
		inData.populateCSVTable(csvtable);
	}
	//--------------------------------------------------------------------------------
	public void showAllColumns(boolean isShowing)
	{
		List<TableColumn<IntegerDataRow,?>> cols = csvtable.getColumns();
		TableColumn<?,?> col0 = cols.get(0);
		if (col0 == null || cols.size() < 9) return;		// table not populated
//		boolean isShowing = col0.isVisible();		
		for (int i=0;i<6;i++)
			cols.get(i).setVisible(!isShowing);
	}
	
	
	

	//--------------------------------------------------------------------------------
	@FXML private void doPlotAll()
	{
		
		System.out.println("doPlotAll   				 NEEDS THREADING");
		graphVBox.getChildren().clear();
		graphVBox.setPrefWidth(750);
		graphVBox.setBorder(Borders.dashedBorder);
		model.processSegmentTables(graphVBox, segments.getItems());
		
		}
	//--------------------------------------------------------------------------------
	@FXML void doHistogramProfiles()	
	{	
		System.out.println("doHistogramProfiles: ");	
		graphVBox.getChildren().clear();
		graphVBox.setPrefWidth(1450);
		resultsplitter.setDividerPosition(0, 0.1);
//		graphVBox.setBorder(Borders.dashedBorder);
		model.profileHistograms(graphVBox, segments.getItems());
	}

	//--------------------------------------------------------------------------------
		@FXML private void doPlot()
		{
			ObservableList<IntegerDataRow> data = csvtable.getItems();
			if (data == null) return;
			Segment activeSeg = segments.getSelectionModel().getSelectedItem();
			CSVTableData model = activeSeg == null ? null : activeSeg.getData();
			if (model != null)
			{
				List<Histogram1D> histos = model.getHistograms(); 
				graphVBox.getChildren().clear();
				if (histos == null) return;
				for (Histogram1D histo : histos)
				{
					if (histo == null) continue;		// first 5 are null
					Range r = histo.getRange();
					System.out.println("Histogram has range of " + r.min() + " - " + r.max());
					if (r.width() < 50) continue;
					LineChart<Number, Number> chart = histo.makeChart();
					graphVBox.getChildren().add(chart);
					VBox.setVgrow(chart, Priority.ALWAYS);
				}
			}
		}
		//--------------------------------------------------------------------------------
		@FXML private void doPlot2D()
		{
			System.out.println("doPlot2D");
			graphVBox.getChildren().clear();
			
			ObservableList<IntegerDataRow> data = csvtable.getItems();
			if (data == null) return;
			Segment activeSeg = segments.getSelectionModel().getSelectedItem();
			if (activeSeg != null)
			{
				CSVTableData model = activeSeg.getData();
				model.getImages().clear();
				model.generateScatters(graphVBox);
			}
		}

	//--------------------------------------------------------------------------------
	Background saveBG;

	private void setupDropPane()
	{
		tocTabPane.setOnDragEntered(e ->
		{
			saveBG = tocTabPane.getBackground();
			tocTabPane.setEffect(Effects.innershadow);
			tocTabPane.setBackground(Backgrounds.tan);
			e.consume();
		});
		// drops don't work without this line!
		tocTabPane.setOnDragOver(e ->	{	e.acceptTransferModes(TransferMode.ANY);  e.consume();	});
		
		tocTabPane.setOnDragExited(e ->
		{
			tocTabPane.setEffect(null);
			tocTabPane.setBackground(saveBG);
			e.consume();
		});
		
		tocTabPane.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);
			Dragboard db = e.getDragboard();
//			Set<DataFormat> formats = db.getContentTypes();
//			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
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
		List<File> files = db.getFiles();
		
		for (File f : files)
		{
			if (FileUtil.isZip(f))
			{
				String manifest = FileUtil.decompress(f);
				System.out.println("\nunzipped these files: \n" + manifest);
				f = new File(StringUtil.chopExtension(f.getAbsolutePath()));
			}
			if (f.isDirectory())
			{		
				if (f.getName().toUpperCase().contains("GATE"))
					EDLParsingHelper.addCSVFilesToSegments(f, segments);
				else
				{
					setMethodsFilePath(f.getAbsolutePath());
					fileTree.setRoot(f);				// traverse down the file system tree, adding everything	
					EDLParsingHelper.setEDLDirectory(f, xmlTree, scans, segments);
				}
				break;			//  add the first directory, then break
			}
		}
		
	}
	//--------------------------------------------------------------------------------

	private void setupCSVTable()
	{
		assert (csvtable != null);
	}
	
	//--------------------------------------------------------------------------------
	private void setupXMLTree()
	{
		assert (xmlTree != null);
//		xmlTree = new TreeTableView<org.w3c.dom.Node>();
		xmlTree.setFixedCellSize(30);
		xmlTree.setShowRoot(false);
		EDLParsingHelper helper = new EDLParsingHelper(xmlTree);
		helper.setupDictionary();
	}

	//--------------------------------------------------------------------------------
// Analysis commands
	
	String[] strs = new String[] { "Filter Samples", "Organize Files", "Image Processing", "Edge Detection", "Feature Recognition", "Quantification", "Parametric Normalization", "Labeling"};
	String[] stepList = new String[] { "Read Zip File", "Check Manifest", "Queue Files", "Read CSV Files", "Ranges Set", "Distributions Set", "Gutter Gates Applied", "Statistics Generated", "Smoothed", "Distriubtions Regenerated", "Normalized", "Baseline Peak Found", "Populations Gated"};
	String[] interrog = new String[] { "Activation", "Stimulation", "Memory", "Expression", "Regulation", "Promotion", "Inhibition", "Apoptosis" };
	String[] viz = new String[] { "QC Montage", "Stats Panel", "Backgating", "Correlation", "Heat Map", "Hover Plot", "Drill Down Chart", "Tree Map", "Anova", "Cytoscape", "VISNE", "SPADE"};
	@FXML	private TreeTableColumn<Population, String> nameColumn;
	@FXML	private TreeTableColumn<Population, String> countColumn;
	@FXML	private TreeTableColumn<Population, String> markerColumn;
	@FXML	private TreeTableColumn<Population, String> rangeColumn;
	@FXML	private WebView agendaPage;

	private void setupAnalysis()
	{
		normalizeList.setItems(FXCollections.observableArrayList(stepList));
		normalizeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		normalizeList.setCellFactory(item -> new StepCell());
		
		classifyTree.setRoot(TreeTableModel.getCellPopulationTree());
		classifyTree.getStyleClass().add("classifierTree");
		
		nameColumn.setPrefWidth(200);	 
		nameColumn.setCellValueFactory(p -> {    Population pop = p.getValue().getValue();   	return new ReadOnlyObjectWrapper<String>(pop.getName());	});
		countColumn.setCellValueFactory(p -> {   Population pop = p.getValue().getValue();     	return new ReadOnlyObjectWrapper(pop.getCount());	});
		markerColumn.setCellValueFactory(p -> {   Population pop = p.getValue().getValue();     return new ReadOnlyObjectWrapper<String>(pop.getMarker());	});
		rangeColumn.setCellValueFactory(p -> 
		{  
			Population pop = p.getValue().getValue();   
			String txt =  (pop.getHigh() <= pop.getLow()) ?  "" :"(" + pop.getLow() + " - " + pop.getHigh() + "%)";
			return new ReadOnlyObjectWrapper<String>(txt);	
		});
		
		
		interrogateList.setItems(FXCollections.observableArrayList(interrog));
		interrogateList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		visualizeList.setItems(FXCollections.observableArrayList(viz));
		visualizeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

}
	//--------------------------------------------------------------------------------
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
	
	@FXML void doClose()	{	save();		}	//TODO -- pbly obsolete if we install a close handler!
	@FXML void doQuit()		{	System.exit(0);}

	@FXML void doZip()		{	}		
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
	//--------------------------------------------------------------------------------
	@FXML void doUndo()		{	System.out.println("doUndo");	}
	@FXML void doRedo()		{	System.out.println("doRedo");	}
	@FXML void doCut()		{	System.out.println("doCut");	}
	@FXML void doCopy()		{	System.out.println("doCopy");	}
	@FXML void doPaste()	{	System.out.println("doPaste");	}
	@FXML void doCompare()	{ 	System.out.println("doCompare");	}
	
	
	@FXML void doBatch()	{	System.out.println("doBatch: ");	}
	@FXML void doMonitor()	{	System.out.println("doMonitor: ");	}
	@FXML void doConfigure(){  	System.out.println("doConfigure: ");		}

}
