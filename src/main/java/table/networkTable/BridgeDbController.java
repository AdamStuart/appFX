package table.networkTable;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import model.AttributeValue;
import util.FileUtil;
import util.StringUtil;

public class BridgeDbController implements Initializable
{
	@FXML ChoiceBox<String> organism;
	@FXML private Button search;
	@FXML private Button match;
	@FXML private Button bridgeDB;
	@FXML private Button attributes;
	@FXML private Button sources;
	@FXML private Button targets;
	@FXML private CheckBox allRows;
	@FXML private CheckBox allColumns;
	@FXML private TextArea inputText; 
	@FXML private TableView<AttributeValue> resultsTable; 

//	@FXML ChoiceBox<String> system;
	@FXML TableView<DataSourceRecord> sourceTable;
	@FXML ListView<String> targetList;
	@FXML TableColumn<DataSourceRecord, String> nameCol;
	@FXML TableColumn<DataSourceRecord, String> systemCol;
	@FXML TableColumn<DataSourceRecord, String> urlCol;
	@FXML TableColumn<DataSourceRecord, String> usageCol;
	@FXML TableColumn<DataSourceRecord, String> exampleCol;
	@FXML TableColumn<DataSourceRecord, String> entityCol;
	@FXML TableColumn<DataSourceRecord, String> speciesCol;
//	@FXML TableColumn<DataSourceRecord, String> entityTypeCol;
	@FXML TableColumn<DataSourceRecord, String> uriCol;
	@FXML TableColumn<DataSourceRecord, String> regexCol;
	@FXML TableColumn<DataSourceRecord, String> officialNameCol;
	@FXML TableColumn<DataSourceRecord, String> targetsCol;
	@FXML TableColumn<DataSourceRecord, String> gravityStrCol;
	
	@FXML TableColumn<AttributeValue, String> attributeCol;
	@FXML TableColumn<AttributeValue, String> valueCol;

	//----------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		organism.setItems(getSpeciesList());

		organism.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> 
		{ 
			String species = organism.getItems().get(val.intValue());
			System.out.println("setting species to " + species);
			setSpeciesInfo(species); 
		}); 
		
		sourceTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> { sourceChangeHandler(val); }); 
		nameCol.setCellValueFactory( cell -> cell.getValue().nameProperty());
		systemCol.setCellValueFactory( cell -> cell.getValue().systemProperty());
		urlCol.setCellValueFactory( cell -> cell.getValue().siteProperty());
		usageCol.setCellValueFactory( cell -> cell.getValue().usageProperty());
		exampleCol.setCellValueFactory( cell -> cell.getValue().exampleProperty());
		entityCol.setCellValueFactory( cell -> cell.getValue().entityProperty());
		speciesCol.setCellValueFactory( cell -> cell.getValue().exclusiveSpeciesProperty());
//		entityTypeCol.setCellValueFactory( cell -> cell.getValue().idtypeProperty());
//		gravityStrCol.setCellValueFactory( cell -> cell.getValue().gravityStrProperty());
		uriCol.setCellValueFactory( cell -> cell.getValue().uriProperty());
		regexCol.setCellValueFactory( cell -> cell.getValue().patternProperty());
		officialNameCol.setCellValueFactory( cell -> cell.getValue().fullnameProperty());
//		targetsCol.setCellValueFactory( cell -> cell.getValue().supportedSystemsStrProperty());
		organism.getSelectionModel().select(9);
		nameCol.setPrefWidth(200);
		speciesCol.setPrefWidth(100);
		targetsCol.setPrefWidth(400);		// has a long string of target systems
		targetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		targetList.getSelectionModel().selectedItemProperty().addListener(ev->{ targetSelectionChanged(ev);});

		attributeCol.setCellValueFactory( cell -> cell.getValue().attributeProperty());
		valueCol.setCellValueFactory( cell -> cell.getValue().valueProperty());
     	}
	//----------------------------------------------------------------------------------
	
	private String getSelectedSource()
	{
		DataSourceRecord rec =sourceTable.getSelectionModel().getSelectedItem();
		return (rec != null) ? rec.getName() : "";
				
	}
	//----------------------------------------------------------------------------------
	private void targetSelectionChanged(Observable ev) {	// FIXME NOT TRIGGERING ON CMD CLICK DESELECTION
		
//		List<String> selectedTargets = targetList.getSelectionModel().getSelectedItems();
//		resultsTable.getColumns().clear();
//		String selSrc  = getSelectedSource();
//		if (selSrc.length() > 0)
//		{
//			resultsTable.getColumns().add(new TableColumn(selSrc));
//			for (String col : selectedTargets)
//			{
//				TableColumn column = new TableColumn(col);
//				resultsTable.getColumns().add(column);
//			}
//		}
	}
	public void start()
	{ 
	}
//----------------------------------------------------------------------------------
	static String BDB = "http://webservice.bridgedb.org/";
	public ObservableList<String> getSpeciesList()
	{
		ObservableList<String> speciesList = FXCollections.observableArrayList();
		String urlStr = BDB + "contents";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(response);
		for (String s : response.split("\n"))
		{
			if (s.length() > 0)
			{
				String[] flds = s.split("\t");
				speciesList.add(flds[0] + " (" + flds[1] + ")");
			}
		}
		return speciesList;
	}
	//--------------------------------------------------------------------------------
	SimpleStringProperty species = new SimpleStringProperty();

	private void setSpeciesInfo(String newSpecies)
	{
//		if (species.indexOf(" (") > 0)
//			species = species.substring(0, species.indexOf(" ("));
		species.set(newSpecies);
		readDataSources();	
		readTargetSources(getSpeciesShort());
	}

	//--------------------------------------------------------------------------------
	private void sourceChangeHandler(Number newSelectedIndex)
	{
		int idx = newSelectedIndex.intValue();
		if (idx < 0) return;
//		targetList.getItems().clear();
//		DataSourceRecord rec = sourceTable.getItems().get(idx);
//		rec.checkSupportMap(getSpeciesShort(), this);
//		String supp = rec.getSupportedSystemsStr();
//		System.out.println("supported targets: " + supp);
//		for (String s : supp.split(" "))
//			targetList.getItems().add(systemToNameLookup.get(s));
	}
	
//	//--------------------------------------------------------------------------------
	@FXML public void doJoin()
	{
		
	}
	
	@FXML public void doSearch()
	{
		String input = inputText.getText(); 		if (input.trim().length() == 0) return;
		String species = getSpeciesShort();			if (StringUtil.isEmpty(species)) return;
		String source = getSelectedSource();		if (StringUtil.isEmpty(source)) return;
		String target = getSelectedTarget();		if (StringUtil.isEmpty(target)) return;
		resultsTable.getItems().clear();
		String lines[] = input.split("\n");
		for (String line : lines)		
		{
			int tab = line.indexOf("\t");
			if (tab > 0)
				line = line.substring(0,tab);
			if (!line.trim().isEmpty())
			{
//				testStringOnAllPatterns(line);
				String urlStr = BDB + species + "/xrefs/" + nameToSystemLookup.get(source) + "/" + line;
				String response = StringUtil.callURL(urlStr, true);
				String mappedId = lookup(response, target);
				resultsTable.getItems().add(new AttributeValue(line, mappedId));
			}
		}
	}

	private String lookup(String response, String target) {
		String lines[] = response.split("\n");
		for (String line : lines)		
		{
			String [] flds = line.split("\t");
			if (flds.length == 2)
				if (flds[1].equals(target))
					return flds[0];
		}
		return null;
	}
	
	
	private String getSelectedTarget() {
		return targetList.getSelectionModel().getSelectedItem();
	}
	void testStringOnAllPatterns(String test)
	{
		for (DataSourceRecord rec : allDataSources)
			rec.patternMatch(test);
	}
	 

	private Map<String, String> parseSearchResponse(String inID, String inTarget, String response)
	{
		Map<String, String> results = new HashMap<String, String>();
		for (String line : response.split("\n"))
		{
			String [] parts = line.split("\t");
			if (parts.length == 2)			// expecting 2 columns back: id + src
			{
				String id = parts[0];
				String src = parts[1];
				if (src.equals(inTarget))
					results.put(inID, id);
			}
		}
		return results;
	}

	//----------------------------------------------------------------------------------
	ObservableList<DataSourceRecord> allDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> minimalDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> matchingDataSources = FXCollections.observableArrayList();
	
	public void readDataSources()
	{
		allDataSources.clear();
		minimalDataSources.clear();
		matchingDataSources.clear();
		List<String> lines = FileUtil.readFileIntoStringList("/Users/adamtreister/Desktop/datasourcesSubset.txt");
		for (String line : lines)			// read in all DataSourceRecords unfiltered
		{
			if (line.trim().length() == 0) continue;
			if (line.startsWith("name\tsystem")) continue;		// ignore first line as column heads
			DataSourceRecord rec= new DataSourceRecord(line);
			allDataSources.add(rec);
		}
		buildSourceMap(allDataSources);		// be able to convert system <-> name
		loadSourceTable();					// filter full set down to a few and populate the table
	}
	@FXML private void doMatch()
	{
//		generateMatchingSourcesFields();
		
		String s = inputText.getText();
		match(s);
	}
	private void generateMatchingSourcesFields()
	{
//		ObservableList<DataSourceRecord> visibleDataSources = FXCollections.observableArrayList();
//		visibleDataSources.addAll(sourceTable.getItems());
//		int i = 0;
//		StringBuilder builder = new StringBuilder();
//		for (DataSourceRecord rec : visibleDataSources)
//		{
//			System.err.println("checkSupportMapping " + i++);
//			rec.checkSupportMap(getSpeciesShort(), this);
//			builder.append(rec.getSystem() + "\t" + rec.getSupportedSystemsStr() + "\n");
//		}
//		FileUtil.writeTextFile(new File("/Users/adamtreister/Desktop/"), 
//				"join.txt", builder.toString());
	}
	
	public void match(String s)
	{
		loadSourceTable();
//		matchingDataSources.clear();
//		for (DataSourceRecord rec : allDataSources)
//			if (rec.patternMatch(s))
//				matchingDataSources.add(rec);
//		sourceTable.setItems(matchingDataSources);
		System.out.println("sourceTable has  -> " + sourceTable.getItems().size() + " matches");
	}

	//--------------------------------------------------------------------------------
	// builds a string of all targets supporting this source
	
	public String targetsForRec(DataSourceRecord dataSource, String species) {
		String systemSrc = dataSource.getSystem();
		StringBuilder builder = new StringBuilder();		//systemSrc + "\t"
		for (String targ : speciesTargets)
		{
			String targSys = nameToSystemLookup.get(targ);
			if (targSys == null) continue;
			String sys = dataSource.getSystem();
//			if (targSys.equals(systemSrc)) continue;		if this is removed its harder to compare the targets for any given source
			String compatReq = "isMappingSupported/" + systemSrc + "/" + sys;
			String response = bridgeDbcall(compatReq);
			boolean supported = response.toLowerCase().contains("true");
			if (supported) 
				builder.append(targSys + " ");
		}
		System.out.println(systemSrc + " -> " + builder.toString());
		return builder.toString();
	}
	//--------------------------------------------------------------------------------
	private String bridgeDbcall(String command)
	{
//		System.out.println(command);
		String species = organism.getValue();
		int idx = species.indexOf(" (");
		if (idx > 0) species = species.substring(0, idx);
		String urlStr =BDB + species + "/" + command;
		return StringUtil.callURL(urlStr, true);
	}

	//--------------------------------------------------------------------------------
	HashMap<String, String> systemToNameLookup;
	HashMap<String, String> nameToSystemLookup;
	
	private void buildSourceMap(List<DataSourceRecord> sources) {
		systemToNameLookup= new HashMap<String, String>();
		nameToSystemLookup= new HashMap<String, String>();
		for (DataSourceRecord rec : sources)
		{
			String name = rec.getName();
			String sys=rec.getSystem();
			systemToNameLookup.put(sys, name);
			nameToSystemLookup.put(name, sys);
		}
	}
	//--------------------------------------------------------------------------------
	private boolean allRowsVisible() 	{ return allRows.isSelected();  }
	private boolean allColumns() 		{ return allColumns.isSelected();  }
	@FXML private void showAllRows() 	{ loadSourceTable();  }
	@FXML private void showAllColumns() { loadSourceTable();   }
	
	//--------------------------------------------------------------------------------
	private void loadSourceTable() 
	{   
		ObservableList<DataSourceRecord> items = sourceTable.getItems();
		items.clear();
//		items.addAll(allRows() ? allDataSources : minimalDataSources);

		String input =  inputText.getText().trim();
		items.addAll(allDataSources.stream()
				.filter(isSpeciesSpecific(getSpeciesLatin()))
//				.filter(positveGravity(allRowsVisible()))
				.filter(inputMatchesPattern(input))
//				.filter(hasAvailableTargets())
				.collect(Collectors.toList()));

		if (items.size() > 0) 
			sourceTable.getSelectionModel().select(0);

		boolean showAll = allColumns();
		TableColumn[] optionalCols = new TableColumn[]{urlCol, usageCol, entityCol, uriCol, regexCol,officialNameCol,gravityStrCol, targetsCol };
		for (TableColumn col : optionalCols)
			col.setVisible(showAll);
	}
	Predicate<DataSourceRecord> nameEmpty = p -> p.getName() != null;
	public static Predicate<DataSourceRecord> isSpeciesSpecific() {
	    return rec -> StringUtil.isEmpty(rec.getExclusiveSpecies()); }

	public static Predicate<DataSourceRecord> isSpeciesSpecific(String species) {
	    return rec -> {
	    	String excl = rec.getExclusiveSpecies();
	    	return StringUtil.isEmpty(excl) || species.contains(excl);   };
	}
	
//	private static Predicate<DataSourceRecord> positveGravity(boolean allRows) {
//		return rec -> allRows || rec.gravity() > 0;
//	}
	
	private static Predicate<DataSourceRecord> inputMatchesPattern(String input) {
		return rec -> {
			if (input.trim().isEmpty()) return true;
			return rec.patternMatch(input);
		};
	}
//	private static Predicate<DataSourceRecord> hasAvailableTargets() {
//		return rec -> rec.anySupportedSystems();
//	}

	//--------------------------------------------------------------------------------
	// called when the organism is set, filters down targets for that species
	ObservableList<String> speciesTargets = FXCollections.observableArrayList();
	
	public void readTargetSources(String species)
	{
		if (StringUtil.isEmpty(species)) return;
		String urlStr = BDB + species + "/targetDataSources";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(urlStr + "\n\n" + response+ "\n\n");
		speciesTargets.clear();
		targetList.getItems().clear();
		for (String s : response.split("\n"))
			if (!s.trim().isEmpty())
				speciesTargets.add(s);
		targetList.getItems().addAll(speciesTargets);
	}

	//--------------------------------------------------------------------------------
	public String getSpecies()		{ 		return species.get();} 

	public String getSpeciesShort()		{ 	
		String s = getSpecies(); 
		if (s == null) return "";
		int idx = s.indexOf(" (");
		return s.substring(0, idx);
	} 
	
	public String getSpeciesLatin()		{ 	
		String s = getSpecies(); 
		if (s == null) return "";
		int idx = s.indexOf(" (");
		return s.substring(idx, s.indexOf(")"));
	} 
	//--------------------------------------------------------------------------------
//	@FXML private void doAttributeSet()	{	bridgeDBcall("attributeSet");	}
//	private void doSources()		{		readDataSources(getSpecies());			}			// bridgeDBcall("sourceDataSources");
//	private void doTargets()		{		bridgeDBcall("targetDataSources");	}
	//--------------------------------------------------------------------------------

//	//--------------------------------------------------------------------------------
//	@FXML private void doSearch()
//	{
//		String keys = listItemsToString(inputList);
//		if (keys.length() == 0) return;
//		String lines[] = keys.split("\n");
//		for (String line : lines)
//			if (line.trim().length() > 0)
//				bridgeDBcall("search/" + line);
//	}
//	
//	
//	private String listItemsToString(ListView<String> aList) {
//		StringBuilder str = new StringBuilder();
//		for (String a : aList.getItems())
//			str.append(a + "\n");
//		return str.toString();
//	}
//	

}
