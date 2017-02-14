package publish;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import org.w3c.dom.Document;

import container.mosaic.refimpl.javafx.MosaicPane;
import database.forms.EntrezForm;
import game.bookclub.BorderPaneAnimator;
import gui.DropUtil;
import gui.ProgressStatus;
import gui.TabPaneDetacher;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.bio.Gene;
import model.dao.StringDataRow;
import model.stat.Population;
import util.FileUtil;
import util.StringUtil;
import xml.XMLFileTree;
import xml.XMLTools;
import xml.XMLTreeItem;

  
public class PublishController implements Initializable
{
	@FXML AnchorPane top;
	@FXML BorderPane borderPane;
	@FXML BorderPane fluomicsContainer;
	@FXML TabPane tocTabPane;
		
	@FXML AnchorPane abstractAnchor;
	@FXML AnchorPane researchAnchor;
	@FXML AnchorPane methodsAnchor;
	@FXML AnchorPane checkpointAnchor;
	@FXML AnchorPane resultsAnchor;
	@FXML AnchorPane analysisAnchor;
	@FXML AnchorPane discussionAnchor;
	@FXML AnchorPane mosaicAnchor;			// Visualize
	@FXML AnchorPane annotateAnchor;
	@FXML AnchorPane referencesAnchor;
//	@FXML AnchorPane visualizationAnchor;
	
	@FXML AnchorPane joinAnnchor;
//	@FXML AnchorPane specsAnchor;
	List<AnchorPane>  anchors;

	//
	//-------------------------------------------------------------------------------------------
	@FXML HTMLEditor hypothesis;

	@FXML ImageView image;
	@FXML ChoiceBox<String> species;
	@FXML ChoiceBox<String> celltype;
	@FXML ChoiceBox<String> technology;
	//---------------------------------------------- 
	@FXML TextArea keywords;
	public String getSelectedSpecies()	{		return species.getSelectionModel().getSelectedItem();	}
	public String getSelectedCellType()	{		return celltype.getSelectionModel().getSelectedItem();	}
	public String getSelectedTechnology(){		return technology.getSelectionModel().getSelectedItem();	}
	public String getKeywords()			{		return keywords.getText();	}
	public String getHTML()				{		return hypothesis.getHtmlText();	}
	public void setSelectedSpecies(String s)	{ species.getSelectionModel().select(s);	}
	public void setSelectedCellType(String s)	{ celltype.getSelectionModel().select(s);	}
	public void setSelectedTechnology(String s)	{ technology.getSelectionModel().select(s);	}
	public void setKeywords(String s)			{ keywords.setText(s);	}
	public void setHTML(String s)				{ hypothesis.setHtmlText(s);	}
	@FXML private VBox fileTreeBox;
	private EntrezForm querier = new EntrezForm();
	public EntrezForm getQuerier()	{		return querier;	}

	// CheckPoint
	@FXML CheckBox auth;
	@FXML CheckBox scheduled;
	@FXML CheckBox resources;
	@FXML CheckBox qc;
	@FXML CheckBox lucky;
	@FXML Button cluster;
	@FXML Button betweenness;
	@FXML Button toggleLeftButton;
	@FXML Button overRep;
	@FXML CheckBox MiST;
	@FXML CheckBox SAInt;
	@FXML Label droplabel;
	
	// Methods
	@FXML private VBox connectionsBox; 
	@FXML private TreeTableView<org.w3c.dom.Node> xmlTree;
	private XMLTreeItem xmlTreeRoot;
	private XMLFileTree fileTree = new XMLFileTree(null);
	@FXML ListView<SOPLink> soplist = new ListView<SOPLink>();
	@FXML private TextField filterText; 
	
	public String getFilterText()		{ return filterText.getText();	}
	public void setFilterText(String s)	{ filterText.setText(s); }

	public TreeTableView<org.w3c.dom.Node> getXmlTree()		{ return xmlTree;	}
	public XMLFileTree 	getFileTree()		{ return fileTree;	}
	
	// Results
//	@FXML Button plotButton;
//	@FXML Button plot2DButton;
//	@FXML Button plotAllButton;
//	@FXML VBox graphVBox;

//	@FXML CheckBox addYOffset;
//	@FXML CheckBox showAllColumns;
//	@FXML CheckBox showSumCk;
//	@FXML SplitPane resultsplitter;
//	@FXML ListView<ScanJob> scans;
//	public ListView<ScanJob> getScans() { return scans; }
//	@FXML ListView<Segment> segments;
//	public ListView<Segment> getSegments() { return segments; }

	@FXML TableView<ResultsRow> resultsTable;
	@FXML Button filterPrey;
	@FXML Button removeContaminants;
	@FXML Button prescore;
	@FXML Button score;

	// Annotate -----------------------------------------------------
	
	@FXML TableView<StringDataRow> annotationTable;
	
	@FXML Slider significance;
	@FXML Slider foldChange;

	@FXML CheckBox H1N1;
	@FXML CheckBox H3N2;
	@FXML CheckBox H5N1;

	@FXML CheckBox hr12;
	@FXML CheckBox hr24;
	@FXML CheckBox hr48;
	@FXML CheckBox hr72;
	@FXML CheckBox hr96;

	@FXML CheckBox hideOrphans;
	@FXML CheckBox literature;
	@FXML CheckBox binary;
	@FXML CheckBox regulatory;
	@FXML CheckBox signaling;

	@FXML ChoiceBox<String> windowSize;
	@FXML ChoiceBox<String> layout;

	@FXML TableColumn<Gene, String> labelColumn;
	@FXML TableColumn<Gene, String> idColumn;
	@FXML TableColumn<Gene, String> nameColumn;
	@FXML TableColumn<Gene, String> timeColumn;
	@FXML TableColumn<Gene, String> H1N1Column;
	@FXML TableColumn<Gene, String> H3N2Column;
	@FXML TableColumn<Gene, String> H5N1Column;
	@FXML Label sigLabel;
	@FXML Label foldChangeLabel;
	// Annotate -----------------------------------------------------
	@FXML TableView<Gene> analysisTable;
	@FXML TableColumn<Gene, Double> betweennessColumn;
	@FXML TableColumn<Gene, Integer> clusterColumn;
	@FXML TableColumn<Gene, String> overRepColumn;
	// Analysis
//	@FXML ListView<String> normalizeList;
//	@FXML ListView<String> interrogateList;
//	@FXML ListView<String> visualizeList;
//	@FXML private TreeTableView<Population> classifyTree;
//	@FXML private VBox canvasVbox;
	
	// discussion
	@FXML HTMLEditor discussion;
	public String getDiscussionHTML() {		return discussion.getHtmlText();	}
	//-------------------------------------------------------------------------------------------
	// check boxes in the results tab to govern what is included in tables and graphs
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
	
	public static Application getApplication()	{		return AppPublish.getInstance();	}
	public static Stage getStage()				{		return AppPublish.getStage();	}
	private PublishDocument doc;
//	private PublishDocument getDocument()		{ return doc;	}

	@FXML void showMethodsTree()	{		new MethodsTree(fileTree.getRoot());	}
	MosaicPane<Region> mosaicPane;
	PublishModel model;
	void addText(DragEvent ev)
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
			if (FileUtil.isTXT(f))
			{
				String text = FileUtil.readFileIntoString(f.getAbsolutePath());		
				setHTML(text);
				ev.consume();
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resBundle)
	{
		model = new PublishModel(this);
//		discussion.setId("id");
		doc = new PublishDocument(this);
		setupDropPane();
		//Hypothesis---------
		species.setItems(EDLParsingHelper.speciesList);
		species.getSelectionModel().selectFirst();
		celltype.setItems(EDLParsingHelper.cellTypes);
		celltype.getSelectionModel().selectFirst();
		technology.setItems(EDLParsingHelper.technologyList);
		technology.getSelectionModel().selectFirst();
		DropUtil.makeFileDropPane(hypothesis, this::addText);
		//Research---------
		researchAnchor.getChildren().add(querier);
		AnchorPane.setBottomAnchor(querier, 10d);
		AnchorPane.setTopAnchor(querier, 10d);
		AnchorPane.setLeftAnchor(querier, 10d);
		AnchorPane.setRightAnchor(querier, 10d);

		//Methods---------
		setupXMLTree();
		setupSOPList();
		
		//Checkpoint---------
		auth.setOnAction(e -> showDropLabel());
		scheduled.setOnAction(e -> showDropLabel());
		resources.setOnAction(e -> showDropLabel());
		qc.setOnAction(e -> showDropLabel());
		lucky.setOnAction(e -> showDropLabel());
	
//		String surface = "model5";		Mosaic Pane seems to be brittle around "bad" input here.
		
		//Results --------- there is an overlay of an ImageView and TableView, so show/hide panes on selection change
		setupResults();
		
		//Annotate --------- 
		setupAnnotate();

		
		//Analysis --------- 
		setupAnalysis();
		setupMosaic();
		setupReferences();
		
//		String agendaUrl = "https://docs.google.com/document/d/1VT5hmAjFJSIQT_1YsJpF6ELrzmJ1iT2Lng0s-DyZnSg/edit?ts=563105e1";
//		agendaPage.getEngine().load(agendaUrl);
	}
	private void setupReferences() {
		
		final String RESOURCE = "../table/referenceList/ReferenceList.fxml";
	    final String STYLE = "genelistStyles.css";
		URL res = getClass().getResource(RESOURCE);
	    FXMLLoader referenceLoader = new FXMLLoader(res);
		try {
			BorderPane pane = referenceLoader.load();
			referencesAnchor.getChildren().add(pane);
			AnchorPane.setTopAnchor(pane, 5.);
			AnchorPane.setLeftAnchor(pane, 5.);
			AnchorPane.setBottomAnchor(pane, 5.);
			AnchorPane.setRightAnchor(pane, 5.);
			} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void setupAnnotate() {		// FLUOMICS
		
		DropUtil.makeFileDropPane(annotationTable, ev -> 
		{  importResults(ev.getDragboard().getFiles(), allRows, columnNames);  } );
		
		H1N1Column.visibleProperty().bind(H1N1.selectedProperty());
		H3N2Column.visibleProperty().bind(H3N2.selectedProperty());
		H5N1Column.visibleProperty().bind(H5N1.selectedProperty());
		
		hr12.selectedProperty().addListener((newValue) -> timePointFilter(newValue));
		CheckBox[] checks = { hr12, hr24, hr48, hr72, hr96 };
		for ( CheckBox ck : checks)
			ck.selectedProperty().addListener((newValue) -> timePointFilter(newValue));

		sigLabel.setStyle("-fx-font-size: 11");
		foldChangeLabel.setStyle("-fx-font-size: 11");

	}
	
	List<String> columnNames = new ArrayList<String>();
	List<ResultsRow> allRows = new ArrayList<ResultsRow>();
	
	private void timePointFilter(Observable newValue) {
		CheckBox[] checks = { hr12, hr24, hr48, hr72, hr96 };
		StringBuilder bldr = new StringBuilder();
		for ( CheckBox ck : checks)
		{
			if (ck.isSelected())
				bldr.append(ck.getId().substring(2)).append("H");
		}
		String desiredTimePoints = bldr.toString();
		System.out.println("timePointFilter: " + desiredTimePoints);
		int nRows = allRows.size();
		for (int i=0; i< nRows; i++)
		{
//			Gene rec = allRows.get(i);
//			String timeFld = rec.get("time");
//			if (desiredTimePoints.contains(timeFld))
//				annotationTable.getItems().add(rec);
		}


	}
	List<ResultsRow> resultRows = new ArrayList<ResultsRow>();
	List<String> resultColumnNames = new ArrayList<String>();

	private void setupResults() {
		resultsTable.getItems().clear();
		DropUtil.makeFileDropPane(resultsTable, ev -> 
			{  importResults(ev.getDragboard().getFiles(), resultRows, resultColumnNames); 
			resultsTable.getItems().addAll(resultRows); });		
		new BorderPaneAnimator(fluomicsContainer, toggleLeftButton, Side.LEFT, false, 200);
	}
	//----------------------------------------------------------------------------------
	public void start()
	{ 
		anchors = new ArrayList<AnchorPane>();
        for (int i = 0; i < tocTabPane.getTabs().size(); i++) 
        	anchors.add((AnchorPane)tocTabPane.getTabs().get(i).getContent());
		TabPaneDetacher.create().makeTabsDetachable(tocTabPane);
	}
				
	//-------------------------------------------------------------------------------------				
	public void setActiveTab(String active)
	{
		for (Tab tab : tocTabPane.getTabs())
			if (tab.getText().equals(active))	
			{
				tocTabPane.getSelectionModel().select(tab);
				break;
			}
	}


	//-------------------------------------------------------------------------------------				
	private void showDropLabel()
	{
		boolean showIt = auth.isSelected() && scheduled.isSelected() && resources.isSelected() && lucky.isSelected() && qc.isSelected();
		droplabel.setVisible(showIt);
	}
	
	//-------------------------------------------------------------------------------------				
	@FXML private void betweenness()
	{
		System.out.println("betweenness");
		System.out.println("");
		betweennessColumn.setVisible(true);
		analysisTable.getColumns();
	}
	
	@FXML private void cluster()
	{
		System.out.println("cluster");
		clusterColumn.setVisible(true);

	}
		
	@FXML private void toggleLeft()
	{
		System.out.println("toggleLeft");
		clusterColumn.setVisible(true);

	}
		
	@FXML private void overRep()
	{
		overRepColumn.setVisible(true);
		System.out.println("overRep");
}
	
	private void setupSOPList()
	{
		soplist.setItems(EDLParsingHelper.getSOPLinks());
		soplist.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
			{
				SOPLink link = soplist.getSelectionModel().getSelectedItem();
				if (link != null)
					StringUtil.launchURL(link.getUrl());
			}
		});
	}
	
	/** Mapping of element id's to labels for later reference when serializing Mosaic*/
	private java.util.Map<String, Label> clientMap = new java.util.HashMap<>();
	//-------------------------------------------------------------------------------------				

//	private void importResults(List<File> fList, TableView table) {
//		for (File f : fList)
//				APMSParsingHelper.addTXTFilesToResults(f, table);
//	}

	private void importResults(List<File> fList, List<ResultsRow> list, List<String> columnNames) {
		for (File f : fList)
				APMSParsingHelper.addTXTFilesToResults(f, list, columnNames);
	}

	@FXML private void filterPrey() {
		
	}
	@FXML private void  removeContaminants() {
		
	}
	@FXML private void  prescore() {
		
	}
	@FXML private void  score() {
		
	}
	//-------------------------------------------------------------------------------------				
	private void setupMosaic()
	{
		mosaicPane = new MosaicPane<Region>();
		mosaicAnchor.getChildren().add(mosaicPane);
		mosaicPane.prefHeightProperty().bind(mosaicAnchor.heightProperty());
		mosaicPane.prefWidthProperty().bind(mosaicAnchor.widthProperty());
		
		String[] model = { 
						"Network , 	0, 		0, 		0.50, 	1",  
						"Proteins , 	0.50, 	0, 		0.50, 	0.33", 
						"Interactions, 0.5, 	0.33, 	0.50, 	0.33",  
						"Procedures, 		.5, 	0.66, 	0.50, 	0.33"
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
	//--------------------------------------------------------------------------------

	public Label getLabel(Color color, String id) {
		Label label = new Label(id);
		label.setTextFill(Color.WHITE);
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16d));
		String style = "-fx-background-color: #" + color.toString().substring(2, 8).toUpperCase() + 
						";-fx-alignment:center;-fx-text-alignment:center;";
		label.setStyle(style);
		label.setManaged(false);
		return label;
	}
	

	boolean verbose = false;
	
	
	VBox bigView = new VBox();
	//--------------------------------------------------------------------------------
	@FXML private void doShowListView()
	{		
		bigView.getChildren().clear();
		tocTabPane.getTabs().clear();
		for (AnchorPane anchor : anchors)
		{
			if (anchor != null)
			{
				ScrollPane scroller = new ScrollPane(anchor);
				TitledPane pane = new TitledPane(anchor.getId(), scroller);
				bigView.getChildren().add(pane);
			}
		}				
		ScrollPane mainscroller = new ScrollPane(bigView);

		tocTabPane.setVisible(false);
		bigView.setVisible(true);
		borderPane.setCenter(mainscroller);
	}

	//--------------------------------------------------------------------------------
	@FXML private void doShowTabView()
	{
		bigView.getChildren().clear();
		tocTabPane.getTabs().clear();
		for (AnchorPane anchor : anchors)
		{
			String titledText = anchor.getId();
			Tab t = new Tab();
			t.setContent(anchor);
			t.setText(titledText);;
			tocTabPane.getTabs().add(t);
			anchor.setVisible(true);
		}
		tocTabPane.setVisible(true);
		bigView.setVisible(false);
		borderPane.setCenter(tocTabPane);
	}

	//--------------------------------------------------------------------------------
	@FXML private void doShowMosaic()
	{
		System.out.println("doShowMosaic");
	}

	//--------------------------------------------------------------------------------
	@FXML private void doShowWizard()
	{
		System.out.println("doShowWizard");
	}

	//--------------------------------------------------------------------------------
	@FXML private void doPlotAll()
	{
		
		System.out.println("doPlotAll   	 NEEDS THREADING");
//		graphVBox.getChildren().clear();
//		graphVBox.setPrefWidth(750);
//		graphVBox.setBorder(Borders.dashedBorder);
//		model.processSegmentTables(graphVBox, segments.getItems());
		
		}
	//--------------------------------------------------------------------------------
	@FXML void doHistogramProfiles()	
	{	
		System.out.println("doHistogramProfiles: ");	
//		graphVBox.getChildren().clear();
//		graphVBox.setPrefWidth(1450);
//		resultsplitter.setDividerPosition(0, 0.9);
////		graphVBox.setBorder(Borders.dashedBorder);
//		model.profileHistograms(graphVBox, segments.getItems());
	}

	//--------------------------------------------------------------------------------
//	@FXML private void doPlot()
//	{
//		ObservableList<IntegerDataRow> data = resultsTable.getItems();
//		if (data == null) return;
//		Segment activeSeg = segments.getSelectionModel().getSelectedItem();
//		CSVTableData model = activeSeg == null ? null : activeSeg.getData();
//		if (model != null)
//		{
//			graphVBox.getChildren().clear();
//			model.generateRawHistogramCharts(graphVBox);
//		}
//	}
	//--------------------------------------------------------------------------------
//	@FXML private void doViz()
//	{
//		System.out.println("doViz");
//	}
	//--------------------------------------------------------------------------------
//	@FXML private void doViz2D()
//	{
//		System.out.println("doViz2D");
//	}
	//--------------------------------------------------------------------------------
//	@FXML private void doPlot2D()
//	{
//		System.out.println("doPlot2D");
//		
//		ObservableList<IntegerDataRow> data = resultsTable.getItems();
//		if (data == null) return;
//		Segment activeSeg = segments.getSelectionModel().getSelectedItem();
//		if (activeSeg != null)
//		{
//			CSVTableData model = activeSeg.getData();
//			model.getImages().clear();
//			graphVBox.getChildren().clear();
//			model.generateScatters(graphVBox);
//		}
//	}

	//--------------------------------------------------------------------------------
	private void setupDropPane()
	{
		DropUtil.makeFileDropPane(borderPane, this::addFiles);
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
			if (FileUtil.isTXT(f))
			{
				APMSParsingHelper.addTXTFilesToResults(f, resultsTable);
			}
			if (FileUtil.isCSV(f))
			{
//				EDLParsingHelper.addCSVFilesToSegments(f, segments);
			}
			if (f.isDirectory())
			{		
//				if (f.getName().toUpperCase().contains("GATE"))
//					EDLParsingHelper.addCSVFilesToSegments(f, segments);
//				else
				{
					doc.setMethodsFilePath(f.getAbsolutePath());
					fileTree.setRoot(f);				// traverse down the file system tree, adding everything	
//					EDLParsingHelper.setEDLDirectory(f, this);
					if (xmlTreeRoot == null)
					{
						xmlTreeRoot = (XMLTreeItem) xmlTree.getRoot();
						if (xmlTreeRoot != null)
						{
							org.w3c.dom.Node node = xmlTreeRoot.getChildren().get(0).getValue();
							xmlTreeRoot.setValue(node);
						}
					}
				}
				break;			//  add the first directory, then break
			}
		}
		
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
		fileTreeBox.getChildren().add(fileTree);
        filterText.setPromptText("Filter methods tree ...");
        filterText.textProperty().addListener((observable, oldValue, newValue) -> filterChanged(newValue));
        xmlTreeRoot = null;
	}

	private void filterChanged(String filter)
	{
		if (filter.isEmpty()) xmlTree.setRoot(xmlTreeRoot);
		else
		{
			XMLTreeItem filteredRoot = new XMLTreeItem();
			buildFilteredTree(xmlTreeRoot, filteredRoot, filter);
			xmlTree.setRoot(filteredRoot);
		}
	}
// TODO -- this doesn't work correctly.  XMLTools.bug in nodeContains
	void buildFilteredTree(XMLTreeItem item, XMLTreeItem filtered, String s)
	{
		if (StringUtil.isEmpty(s) || item == null ) return;
		org.w3c.dom.Node node = item.getValue();
		if (XMLTools.nodeContains(node, s.toUpperCase()))
		{
			XMLTreeItem filteredChild = new XMLTreeItem(item);
			filteredChild.setExpanded(true);
			filtered.getChildren().add(filteredChild);
			for (TreeItem<?> itemChild : item.getChildren())
				buildFilteredTree((XMLTreeItem)itemChild, filteredChild, s);
		}
	}

	//--------------------------------------------------------------------------------
// Analysis commands
	
	@FXML	private TreeTableColumn<Population, String> analysisColumn;
	@FXML	private TreeTableColumn<Population, String> countColumn;
	@FXML	private TreeTableColumn<Population, String> markerColumn;
	@FXML	private TreeTableColumn<Population, String> rangeColumn;
//	@FXML	private WebView agendaPage;

	private void setupAnalysis()
	{
		betweennessColumn.setVisible(false);
		clusterColumn.setVisible(false);
		overRepColumn.setVisible(false);
//		betweennessColumn.setCellValueFactory(value);
//		clusterColumn.setCellValueFactory(value);
//		overRepColumn.setCellValueFactory(value);
//		AnchorPane canvas = new AnchorPane();
//		canvas.setPrefHeight(3000);
//		canvasVbox.getChildren().add(canvas);
//		canvas.setBorder(Borders.dashedBorder);

		
		//these two list commented out because it wasn't clear what they are for.
//		interrogateList.setItems(FXCollections.observableArrayList(EDLParsingHelper.interrog));
//		interrogateList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//		visualizeList.setItems(FXCollections.observableArrayList(EDLParsingHelper.viz));
//		visualizeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
}
	class Step
	{
		String name;
		ProgressStatus status;
	}

	//--------------------------------------------------------------------------------
	// a cell definition for a list of items.  If the first char is a digit,
	// interpret that as a ProgressStatus icon
	
	public class StepCell extends ListCell<String> {

	     public StepCell() {    }
	       
	     @Override protected void updateItem(String item, boolean empty) {
	         // calling super here is very important - don't skip this!
	         super.updateItem(item, empty);
	           
	         ProgressStatus status = ProgressStatus.NONE;
	         if (item != null && item.length() > 0)
	        {
	        	char c =  item.charAt(0);
	        	if (Character.isDigit(c))
	        	{
	        		item = item.substring(1);
	        		status = ProgressStatus.valueOf(c - '0');
	        	}
		        setText(item == null ? "" : item);
        		
	         }
	         Text icon = status.getIcon();
	         icon.setFill(status.getColor());
	         setGraphic(item == null ? null : icon);

	     }
	 }

	//--------------------------------------------------------------------------------
	public	String getActiveTab()	{ return tocTabPane.getSelectionModel().getSelectedItem().getText(); }

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
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Document");
		File file = chooser.showOpenDialog(PublishController.getStage());
		if (file == null)		return;			// open was canceled		
		
		Document w3cdoc = null;
		if (FileUtil.isCSV(file))
		{
//			EDLParsingHelper.addCSVFilesToSegments(file, segments);
		}
		else if (FileUtil.isXML(file))
		{
			try {
				w3cdoc = FileUtil.openXML(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (w3cdoc != null)
				doc.install(w3cdoc);
		}
	}
	@FXML void save()		{	if (doc != null) doc.save();		}
	@FXML void saveas()		{	if (doc != null) doc.saveas();		}
	@FXML void doClose()	{	if (doc != null) doc.save();		}	//TODO -- pbly obsolete if we install a close handler!
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
	//@formatter:off
	@FXML void doUndo()		{	System.out.println("doUndo");	}
	@FXML void doRedo()		{	System.out.println("doRedo");	}
	@FXML void doCut()		{	System.out.println("doCut");	}
	@FXML void doCopy()		{	System.out.println("doCopy");	}
	@FXML void doPaste()	{	System.out.println("doPaste");	}
	@FXML void doCompare()	{ 	System.out.println("doCompare");	}
	
	
	@FXML void doBatch()	{	System.out.println("doBatch: ");	}
	@FXML void doMonitor()	{	System.out.println("doMonitor: ");	}
	@FXML void doConfigure(){  	System.out.println("doConfigure: ");		}
	public Scene getScene()	{	return top.getScene();	}

}
