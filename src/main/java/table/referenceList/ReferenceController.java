package table.referenceList;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import database.forms.EntrezQuery;
import gui.DraggableTableRow;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import model.IController;
import model.bio.BiopaxRecord;
import model.bio.ISpeciesSpecific;
import model.bio.PathwayRecord;
import model.bio.ReferenceListRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;


public class ReferenceController extends TableController<BiopaxRecord> implements Initializable, IController, ISpeciesSpecific  {

    private static final DataFormat BIOPAX_MIME_TYPE = new DataFormat("application/x-java-biopax");

    @FXML private TableView<BiopaxRecord> theTable;
	@FXML private TableColumn<BiopaxRecord, String> refCol;
	@FXML private TableColumn<BiopaxRecord, String> dbidCol;
	@FXML private TableColumn<BiopaxRecord, String> dbCol;
	@FXML private TableColumn<BiopaxRecord, String> authorCol;
	@FXML private TableColumn<BiopaxRecord, String> titleCol;
	@FXML private TableColumn<BiopaxRecord, String> sourceCol;
	@FXML private TableColumn<BiopaxRecord, String> yearCol;

	@FXML private Button search;
	@FXML private TextField searchBox;

	static TableRow<PathwayRecord> thisRow = null;
//	private IController parentController;
//	public void setParentController(IController c)	{ parentController = c;	}
	private String state;
	public void setState(String s) { 	state = s; }
	public String getState() 	{ 	return state; 	}

	static public void showPMIDInfo(String pmid)
	{
	   String text = EntrezQuery.getPubMedAbstract(pmid);
	   if (StringUtil.hasText(text))
	   {  
		   Alert a = new Alert(AlertType.INFORMATION, text);
		   a.setHeaderText("PubMed Abstract");
		   a.getDialogPane().setMinWidth(600);
		   a.setResizable(true);
		   a.showAndWait();
	   }
	}
	//---------------------------------------------------------------------------
	public static String tooltip = "Double click reference to fetch its abstract";
	//---------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		if (theTable != null)
			setupReferenceTable();
	}
	
	//---------------------------------------------------------------------------
	
	private List<BiopaxRecord> getScoredReferences(String url) {
		List<BiopaxRecord> list = new ArrayList<BiopaxRecord>();
		url = url.replace(" ", "%20");
		String result = StringUtil.callURL(url, false);
		result = result.replaceAll("\t\t", "\n");

		try {
			Document doc = FileUtil.convertStringToDocument(result);
			if (doc != null)
			{
				NodeList nodes = doc.getElementsByTagName("ns1:result");
				int sz = nodes.getLength();
				for (int i=0; i<sz; i++)
				{
					BiopaxRecord rec = new BiopaxRecord(nodes.item(i));
					list.add(rec);
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}
	
	public static List<BiopaxRecord> getRefs(String url) {
		List<BiopaxRecord> list = new ArrayList<BiopaxRecord>();
		url = url.replace(" ", "%20");
		String result = StringUtil.callURL(url, false);
		result = result.replaceAll("\t\t", "\n");

		try {
			Document doc = FileUtil.convertStringToDocument(result);
			if (doc != null)
			{
				NodeList nodes = doc.getElementsByTagName("ns1:pathways");
				int sz = nodes.getLength();
				for (int i=0; i<sz; i++)
				{
					BiopaxRecord rec = new BiopaxRecord(nodes.item(i));
					list.add(rec);
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}

	public void viewReferenceByIndex(String idx) {
		if (StringUtil.isInteger(idx))
		{
			int i = StringUtil.toInteger(idx);
			BiopaxRecord rec = theTable.getItems().get(i);
			viewReference(rec, false);
		}
	}
	
	public void viewReference(BiopaxRecord rec, boolean edit) { 	showPMIDInfo(rec.getId());	}
	
	public void getInfo(DataFormat mimetype, String a, String colname, MouseEvent ev) {
//		System.out.println("getInfo: " + a + " Fetching: " + ev);	
		if (StringUtil.isInteger(a))
		{
			int idx = StringUtil.toInteger(a);
			BiopaxRecord rec = theTable.getItems().get(idx);
			boolean edit = ev.isShiftDown();
			viewReference(rec, edit);
		}
	}

	
	   //---------------------------------------------------------------------------
	public static String REFERENCE_QUERY = "https://";
//	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML public void doSearch()	
	{

		String text = searchBox.getText().trim();
		if (StringUtil.isEmpty(text)) 			return;
//		String queryText = "", speciesComponent = "";
//		if (StringUtil.hasText(text))
//			queryText = "query=" + text;
//		String selection = species.getSelectionModel().getSelectedItem();
//		if ( !"Any".equals(selection))
//			speciesComponent = "species=" + selection.replace(" ", "%20")  + "&";
				
		String query = REFERENCE_QUERY;
//		query += speciesComponent + queryText;
		System.out.println(query);
		theTable.getItems().clear();
		List<BiopaxRecord> paths = getScoredReferences(query);
		theTable.getItems().addAll(paths);
		
	}
	
	public void openByReference(String ref) {
		for (BiopaxRecord rec : theTable.getItems())
			if (ref.equals(rec.getId()))
			{
			}
	}
   //---------------------------------------------------------------------------
	public static final DataFormat REFERENCE_MIME_TYPE = new DataFormat("application/x-java-serialized-reference");

	private void setupReferenceTable()
	{
//		System.out.println("setupReferenceTable");

		refCol.setUserData("T");		allColumns.add(refCol);
		dbidCol.setUserData("T");		allColumns.add(dbidCol);
		dbCol.setUserData("T");			allColumns.add(dbCol);
		authorCol.setUserData("L");		allColumns.add(authorCol);
		titleCol.setUserData("T");		allColumns.add(titleCol);
		sourceCol.setUserData("T");		allColumns.add(sourceCol);
		yearCol.setUserData("T");		allColumns.add(yearCol);
		makeSeparatorColumn();
		columnTable.getItems().addAll(allColumns);
		
		refCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("rfid"));
		dbidCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("id"));
		dbCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("db"));
		authorCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("authors"));
		titleCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("title"));
		sourceCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("source"));
		yearCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("year"));
		theTable.setRowFactory((a) -> {  return new DraggableTableRow<BiopaxRecord>(theTable, BIOPAX_MIME_TYPE, this, referenceListRecord);   });

		}
	TableColumn separatorColumn = new TableColumn();
	protected void makeSeparatorColumn() {
		allColumns.add(separatorColumn);  
		separatorColumn.setPrefWidth(0);  
		separatorColumn.setText("--------");		// TODO horizontal line in TableCell
	}
//	private List<TableColumn<BiopaxRecord,?>> allColumns = new ArrayList<TableColumn<BiopaxRecord,?>>();
	private ReferenceListRecord referenceListRecord;		// the model

	protected void processFile(File file) {
		ReferenceListRecord record = ReferenceListRecord.readReferenceList(file);
		for (BiopaxRecord ref : record.getReferences())
			theTable.getItems().add(ref);			// TODO check for extant
	}

	@Override
	public Species getSpecies() {
		return Species.Human;  // parentController == null ? Species.Human : parentController.getSpecies();		// TODO should assertNonNull parentController?
	}
	@Override public void reorderColumns(int a, int b) 
	{	
		TableColumn col = allColumns.remove(a);
		allColumns.add(b, col);
	}

	@Override
	public void createTableRecord() {
		referenceListRecord = new ReferenceListRecord("ReferenceList");
	}

}
