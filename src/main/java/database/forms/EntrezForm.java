package database.forms;

import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.Borders;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import publish.CriterionBox;
import util.FileUtil;
import util.StringUtil;
import xml.XMLElement;

// A form that can send queries to a public bibliographic database
// http://www.ncbi.nlm.nih.gov/books/NBK25501/

public class EntrezForm extends VBox
{
	static int SPACING = 10;
	RadioButton r1;
	TableView<EntrezRecord> resultsTable;
	VBox queryContent = new VBox(10);
	TextArea abstField = new TextArea();
	ListView<EntrezQuery> savedSearches = new ListView<EntrezQuery>();
	public ObservableList<EntrezRecord> getItems() { return resultsTable.getItems();	}
	
	public EntrezForm()
	{
		super(SPACING);
		setBorder(Borders.blueBorder1);
		Button search = new Button("Search");
		search.setOnAction(e -> {  search();  });
//		dialogPane.setContent(dlogContent);
		ObservableList<String> fieldList = FXCollections.observableArrayList(fields);

		CriterionBox line = new CriterionBox(fieldList, true, queryContent);	
		CriterionBox line2 = new CriterionBox(fieldList, false, queryContent);	
		line.set("Author", "Hennig");
		line2.set("Journal", "Cytometry");
		
		queryContent.setPadding(new Insets(10, 10, 10, 10));
		queryContent.getChildren().addAll(search, line, line2);
		abstField.setWrapText(true);
		savedSearches.setOnMouseClicked(ev -> {
			if (ev.getClickCount() ==2)
			{
				EntrezQuery query = savedSearches.getSelectionModel().getSelectedItem();
				search(query);
			}
		});
		resultsTable = new TableView<EntrezRecord>();
		resultsTable.setPadding(new Insets(10, 10, 10, 10));
		TableColumn<EntrezRecord, TableView> col0 = new TableColumn<EntrezRecord, TableView>("PMID");
		TableColumn<EntrezRecord, TableView> col1 = new TableColumn<EntrezRecord, TableView>("Author");
		TableColumn<EntrezRecord, TableView> col2 = new TableColumn<EntrezRecord, TableView>("Title");
		TableColumn<EntrezRecord, TableView> col3 = new TableColumn<EntrezRecord, TableView>("PubDate");
//		TableColumn<EntrezRecord, TableView> col4 = new TableColumn<EntrezRecord, TableView>("Status");
		TableColumn<EntrezRecord, TableView> col5 = new TableColumn<EntrezRecord, TableView>("Source");
//		TableColumn<EntrezRecord, TableView> col6 = new TableColumn<EntrezRecord, TableView>("Location");
		col1.setPrefWidth(150);
		col2.setPrefWidth(500);
		col3.setPrefWidth(150);
		resultsTable.getColumns().addAll(col0, col1, col2, col3, col5);		//col4, , col6
		// TODO init columns
	
		resultsTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> {selChanged(val);});
		col0.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("pmid"));
		col1.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("author"));
		col2.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("title"));
		col3.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("pubdate"));
//		col4.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("status"));
		col5.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("source"));
//		col6.setCellValueFactory(new PropertyValueFactory<EntrezRecord, TableView>("issue"));

		SplitPane headersplit =  new SplitPane(queryContent, savedSearches);
		SplitPane split =  new SplitPane(resultsTable, abstField);
		split.setOrientation(Orientation.VERTICAL);
		split.setDividerPosition(0, 0.8);
		VBox.setVgrow(split, Priority.ALWAYS);

		SplitPane pageSplit = new SplitPane(headersplit, split);
		pageSplit.setOrientation(Orientation.VERTICAL);
		pageSplit.setDividerPosition(0, 0.4);
		VBox.setVgrow(pageSplit, Priority.ALWAYS);

		VBox.setVgrow(resultsTable, Priority.ALWAYS);
		getChildren().add(pageSplit);
		
	}
		//------------------------------------------------------------------------------
	public XMLElement getXML()
	{
		XMLElement elem = new XMLElement("Entrez");
		for (EntrezRecord rec : getItems())
			elem.addElement(rec.getElement());
		return null;
	}
	
	public void setXML(org.w3c.dom.Element e)
	{
		NodeList nodes = e.getChildNodes();
		for (int i=0; i< nodes.getLength(); i++)
		{
			EntrezRecord rec = new EntrezRecord((Element)e);
			System.out.println("TEST ME");
			getItems().add(rec);
		}
	}
	//------------------------------------------------------------------------------

	private void selChanged(Number val)
	{
		if (val.intValue() < 0) return;
		EntrezRecord rec = getItems().get((int)val);
		if (rec != null)
			rec.fetch();
		abstField.setText(rec.getAbstract());
	}

	public void search()
	{
//		String url = extract();
		EntrezQuery query = new EntrezQuery(extract());
		savedSearches.getItems().add(query);
		search(query);
	}
	
	public void search(EntrezQuery query)
	{	
		String url = EUTILS + "esearch.fcgi?db=pubmed&term=" + query.getName();
		String result = StringUtil.callURL(url, false);
		System.out.println(result);
		Document doc = FileUtil.convertStringToDocument(result);
		if (doc != null)
		{
//			Element ids = doc.getElementById();
//			System.out.println("" + ids);
			
			NodeList nodes = doc.getChildNodes();
			StringBuilder buff = new StringBuilder(EUTILS + "esummary.fcgi?db=pubmed&id=");
			for (int i=0; i< nodes.getLength(); i++)
			{
				org.w3c.dom.Node node = nodes.item(i);
				if ("eSearchResult".equals(node.getNodeName()))
				{
					NodeList children = node.getChildNodes();
					for (int j=0; j< children.getLength(); j++)
					{
						if ("IdList".equals(children.item(j).getNodeName()))
						{
							NodeList ids = children.item(j).getChildNodes();
							int idsLen = ids.getLength();
							for (int k=0; k< idsLen; k++)
								buff.append(ids.item(k).getTextContent() + ",");
							buff.setLength(buff.length()-1);
							String summaryReq = buff.toString();
							System.out.println(summaryReq);
							getSummaryRecords(summaryReq);

						}
					}
				}
			}
		}
	}
	
	private void getSummaryRecords(String summaryReq)
	{
		String summary = StringUtil.callURL(summaryReq, false);		// TODO put in task
		System.out.println(summary);
		xmlToSummary(summary, null, getItems());
	}
	
	static public void xmlToSummary(String xmlResult, StringBuilder builder, List<EntrezRecord> items)
	{
		Document summaryDoc = FileUtil.convertStringToDocument(xmlResult);
		if (summaryDoc != null)
		{
			NodeList sums = summaryDoc.getChildNodes();
			int sumsLen = sums.getLength();
			for (int m=0; m< sumsLen; m++)
			{
				org.w3c.dom.Node aSum = sums.item(m);
				if (aSum.getNodeName().equals("eSummaryResult"))
				{
					if (items != null) items.clear();
					NodeList docSum = aSum.getChildNodes();
					for (int n=0; n< docSum.getLength(); n++)
					{
						org.w3c.dom.Node e = docSum.item(n);
						if (e != null && e instanceof Element)
						{
							EntrezRecord rec = new EntrezRecord((Element)e);
							System.out.println(rec.toString());
							if (builder != null)
								builder.append(rec.toString());
							if (items != null)
								items.add(rec);
						}
					}
				}
			}
		}
	}

	//-----------------------------------------------------------------
	public static String EUTILS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";


	public String extract()		// for saving, we don't want the full url
	{
		StringBuilder buf = new StringBuilder();
		collectSearchTerms(buf);
		return buf.toString();
	}
	
	//------------------------------------------------------------------------------

	private void collectSearchTerms(StringBuilder buf)
	{
		for (Node line : queryContent.getChildren())
		{
			if (line instanceof HBox)
			{
				try
				{
					HBox lineBox = (HBox) line;
					ChoiceBox<String> fldName = (ChoiceBox<String>) lineBox.getChildren().get(0);
					ComboBox<String> contentBox = (ComboBox<String>) lineBox.getChildren().get(1);
					String fld = fldName.getSelectionModel().getSelectedItem();
					String content = contentBox.getEditor().getText();  //getSelectionModel().getSelectedItem();
					if (StringUtil.anyEmpty(fld, content))  continue;
						
					String space2plus = content.replace(' ', '+');
					buf.append(space2plus);
					if (!fld.startsWith("All"))
						buf.append("[" + fld.toLowerCase() + "]");		// TODO lookup field name
					buf.append("+");
				}
				catch( Exception e) {}
			}
		}
		buf.setLength(buf.length()-1);		// remove trailing +		
	}

	//------------------------------------------------------------------------------
	public void setXML( XMLEventFactory  werk, List<XMLEvent> steps)
	{
		String query = extract();
		if (!StringUtil.isEmpty(query) )
		{
			steps.add(werk.createStartElement( "", "", "Active"));
			steps.add(werk.createCData(query));
			steps.add(werk.createEndElement( "", "", "Active"));
		}
		if (savedSearches.getItems().size() > 0)
		{
			steps.add(werk.createStartElement( "", "", "History"));
			for (EntrezQuery q : savedSearches.getItems())
				q.addXML(werk, steps);
			steps.add(werk.createEndElement( "", "", "History"));
		}
		
		if (getItems().size() > 0)
		{
			ObservableList<EntrezRecord> items = getItems();
			for (EntrezRecord item : items)
			{
				if (item.getPMID() != null) 
				{
					steps.add(werk.createStartElement( "", "", "Item"));
					steps.add(werk.createAttribute("PMID", item.getPMID()));
					steps.add(werk.createEndElement( "", "", "Item"));
				}
			}
		}
	}
	public void setXML(org.w3c.dom.Node elem)
	{
		// set Active fields
		// set saved search history
		NodeList nodes = elem.getChildNodes();
		int sz = nodes.getLength();
		for (int i=0; i<sz; i++)
		{
			org.w3c.dom.Node child = nodes.item(i);
			if ("Query".equals(child.getNodeName()))
			{
				EntrezQuery q = new EntrezQuery(child.getAttributes());
				// set actvie field from this
			}
			if ("History".equals(child.getNodeName()))
			{
				NodeList historynodes = child.getChildNodes();
				int sz2 = historynodes.getLength();
				for (int j=0; j<sz2; j++)
				{
					org.w3c.dom.Node query = nodes.item(j);
					if ("Query".equals(query.getNodeName()))
					{
						EntrezQuery q = new EntrezQuery(query.getAttributes());
						savedSearches.getItems().add(q);
					}

				}
			}
		}
		// restore PMIDs to records
		
	}

	//------------------------------------------------------------------------------
	//http://www.ncbi.nlm.nih.gov/books/NBK25501/
	String[] fields = new String[] {"Affiliation",
	"All Fields",
	"Author",	"Author - Corporate",	"Author - First",	"Author - Full",	"Author - Identifier",	"Author - Last",
	"Book",
	"Date - Completion",	"Date - Create",	"Date - Entrez",	"Date - MeSH",	"Date - Modification",	"Date - Publication",
	"EC/RN Number",
	"Editor",	"EditorFilter",	"Filter",
	"Grant Number",
	"ISBN",
	"Investigator",	"Investigator - Full",
	"Issue",
	"Journal",
	"Language",
	"Location ID",
	"MeSH Major Topic",	"MeSH Subheading",	"MeSH Terms",
	"Other Term",
	"Pagination",
	"Pharmacological Action",
	"Publication Type",
	"Publisher",
	"Secondary Source ID",
	"Subject - Personal Name",
	"Supplementary Concept",
	"Text Word",
	"Title",
	"Title/Abstract",
	"Transliterated Title",
	"Volume" };


}