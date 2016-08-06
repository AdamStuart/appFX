package table.networkTable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import util.FileUtil;
import util.StringUtil;

public class BridgeDbController implements Initializable
{
	@FXML private Button bridgeDB;
	@FXML private Button attributes;
	@FXML private Button sources;
	@FXML private Button targets;
	@FXML private CheckBox allRows;
	@FXML private CheckBox allColumns;
	@FXML private TextArea inputBridgeDB; 
	@FXML private TextArea outputBridgeDB; 

	@FXML ChoiceBox<String> organism;
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
	@FXML TableColumn<DataSourceRecord, String> gravityStrCol;
	
	//----------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		organism.setItems(getSpeciesList());

		organism.getSelectionModel().selectedIndexProperty().addListener(ev -> 
		{ 
			int idx = organism.getSelectionModel().getSelectedIndex();
			String species =organism.getItems().get(idx);
			setSpeciesInfo(species); 
			species = species.substring(0, species.indexOf(" ("));
			System.out.println("setting species to " + species);
			}); 
		sourceTable.getSelectionModel().selectedIndexProperty().addListener(ev -> { setTargetForSource(); }); 
//		TableColumn[] allCols = new TableColumn[]{nameCol, systemCol, urlCol, patternCol, 
//				exampleCol, entityCol, speciesCol, entityTypeCol,
//				uriCol, regexCol,officialNameCol };
		nameCol.setCellValueFactory( cell -> cell.getValue().nameProperty());
		systemCol.setCellValueFactory( cell -> cell.getValue().systemProperty());
		urlCol.setCellValueFactory( cell -> cell.getValue().siteProperty());
		usageCol.setCellValueFactory( cell -> cell.getValue().usageProperty());
		exampleCol.setCellValueFactory( cell -> cell.getValue().exampleProperty());
		entityCol.setCellValueFactory( cell -> cell.getValue().entityProperty());
		speciesCol.setCellValueFactory( cell -> cell.getValue().exclusiveSpeciesProperty());
//		entityTypeCol.setCellValueFactory( cell -> cell.getValue().idtypeProperty());
		gravityStrCol.setCellValueFactory( cell -> cell.getValue().gravityStrProperty());
		uriCol.setCellValueFactory( cell -> cell.getValue().uriProperty());
		regexCol.setCellValueFactory( cell -> cell.getValue().patternProperty());
		officialNameCol.setCellValueFactory( cell -> cell.getValue().fullnameProperty());
		organism.getSelectionModel().select(9);
		nameCol.setPrefWidth(200);
		speciesCol.setPrefWidth(100);
		targetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		targetList.getSelectionModel().selectedItemProperty().addListener((obs,ov,nv)->{
//			targetList.setItems(targetList.getSelectionModel().getSelectedItems());
//        });

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

	private void setSpeciesInfo(String species)
	{
//		if (species.indexOf(" (") > 0)
//			species = species.substring(0, species.indexOf(" ("));
		readDataSources(species);	
		readTargetSources(species);
	}

	//--------------------------------------------------------------------------------
	private void setTargetForSource()
	{
		int idx = sourceTable.getSelectionModel().getSelectedIndex();
	}
	
	public void doSearch()
	{
		String id = inputBridgeDB.getText();
		int idx = id.indexOf("\n");
		if (idx > 0)
			id = id.substring(0, idx);
		String species = getSpecies();
		if (!id.trim().isEmpty())
		{
			String urlStr = BDB + species + "/search/" + id + "?limit=1";
			String response = StringUtil.callURL(urlStr, true);
			parseSearchResponse(id, response);
		}
	}
	
	private void parseSearchResponse(String inID, String response) {
		for (String line : response.split("\n"))
		{
			if (line.contains(inID))
			{
				String [] parts = line.split("\t");
				if (parts.length > 1)
				{
					if (parts[1].equals(inID))
					{
						int listIndex = sourceTable.getItems().indexOf(parts[1]);
						if (listIndex >= 0)
						{
							sourceTable.getSelectionModel().select(listIndex);
							sourceTable.scrollTo(listIndex);
						}
						else
						{
							sourceTable.getSelectionModel().clearSelection();
							sourceTable.scrollTo(0);
						}
					}
				}
			}
		}
		
	}

	//----------------------------------------------------------------------------------
	List<DataSourceRecord> allDataSources = FXCollections.observableArrayList();;
	List<DataSourceRecord> minimalDataSources = FXCollections.observableArrayList();;
	
	public void readDataSources(String fullSpecies)
	{
		allDataSources.clear();
		minimalDataSources.clear();
		List<String> lines = FileUtil.readFileIntoStringList("/Users/adamtreister/Desktop/datasources.txt");
		for (String line : lines)
		{
			if (line.trim().length() == 0) continue;
			DataSourceRecord rec= new DataSourceRecord(line);
			String exclusive = rec.getExclusiveSpecies();
			allDataSources.add(rec);
			if (rec.speciesIncluded(fullSpecies))
			{
				if (rec.gravity() > 0 || exclusive.length() > 0) 
					minimalDataSources.add(rec);
			}
		}
		loadSourceTable();
	}
	
	private boolean allRows() { return allRows.isSelected();  }
	private boolean allColumns() { return allColumns.isSelected();  }
	@FXML private void showAllRows() {   loadSourceTable();  }
	@FXML private void showAllColumns() { loadSourceTable();   }
	
	private void loadSourceTable() 
	{   
		sourceTable.getItems().clear();
		sourceTable.getItems().addAll(allRows() ? allDataSources : minimalDataSources);
		
		boolean showAll = allColumns();
		TableColumn[] optionalCols = new TableColumn[]{urlCol, usageCol, exampleCol, entityCol, uriCol, regexCol,officialNameCol,gravityStrCol };
		for (TableColumn col : optionalCols)
			col.setVisible(showAll);
	}

	
	public void readTargetSources(String species)
	{
		if (species.indexOf(" (") > 0)
			species = species.substring(0, species.indexOf(" ("));
		String urlStr = BDB + species + "/targetDataSources";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(response);
		targetList.getItems().clear();
		for (String s : response.split("\n"))
		{
			if (!s.trim().isEmpty())
				targetList.getItems().add(s);
		}
	}
	//--------------------------------------------------------------------------------
	private void bridgeDBcall(String command)
	{
		System.out.println(command);
		String id = command.substring(command.indexOf("/") + 1);
		String species = organism.getValue();
		int idx = species.indexOf(" (");
		if (idx > 0) species = species.substring(0, idx);
		String urlStr =BDB + species + "/" + command;
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(response);
		outputBridgeDB.appendText( "\n===========================\n" + species + "/" + command + "\n\n");
		for (String line : response.split("\n"))
		{
			String[] flds = line.split("\t");
			if (flds.length > 1)
				if (id.equals(flds[0]))
					outputBridgeDB.appendText("\n" + line);
		}
	}
	public String getSpecies()		{ 		return organism.getSelectionModel().getSelectedItem();} 
	public String getSpeciesShort()		{ 	String s = getSpecies(); 
											int idx = s.indexOf(" (");
											return s.substring(0, idx);} 
	//--------------------------------------------------------------------------------
	@FXML private void doAttributeSet()	{	bridgeDBcall("attributeSet");	}
	@FXML private void doSources()	{		readDataSources(getSpecies());			}			// bridgeDBcall("sourceDataSources");
	@FXML private void doTargets()	{		bridgeDBcall("targetDataSources");	}
	//--------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------
	@FXML private void doBridgeDB()
	{
		String keys = inputBridgeDB.getText().trim();
		if (keys.length() == 0) return;
		String lines[] = keys.split("\n");
		for (String line : lines)
			if (line.trim().length() > 0)
				bridgeDBcall("search/" + line);
	}

	//--------------------------------------------------------------------------------
	@FXML private void setSpecies()
	{
		String keys = inputBridgeDB.getText().trim();
		if (keys.length() == 0) return;
		String lines[] = keys.split("\n");
		for (String line : lines)
			bridgeDBcall("search/" + line);
	}

	//---------------------------------------------- BridgeDB panel


}
