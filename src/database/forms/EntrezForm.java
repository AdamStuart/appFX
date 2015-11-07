package database.forms;

import gui.WindowSizeAnimator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.StringUtil;

//http://www.ncbi.nlm.nih.gov/books/NBK25501/
// A form that can send queries to a public bibliographic database

public class EntrezForm extends Region
{
	VBox box;
	int SPACING = 10;
	RadioButton r1;
	public EntrezForm()
	{
		super();
		box = new VBox(SPACING);

		Button search = new Button("Search");
		search.setOnAction(e -> {  search();  });
		VBox dlogContent = new VBox(30);
		dlogContent.setPadding(new Insets(50, 50, 10, 10));
		dlogContent.getChildren().addAll( box);
//		dialogPane.setContent(dlogContent);

		HBox line = makeCriteriaLine(fields, 0);	
		HBox line2 = makeCriteriaLine(fields, 1);	
		box.getChildren().addAll(search, line, line2);
		getChildren().add(dlogContent);
	}
		//------------------------------------------------------------------------------

	public void search()
	{
		String url = extract();
		String result = StringUtil.callURL(url);
		System.out.println(result);
		
	}
	
	private String extract()
	{
		StringBuffer buf = new StringBuffer("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=");
//		boolean firstClause = true;
		for (Node line : box.getChildren())
		{
//				if (!firstClause)
//					buf.append(r1.isSelected() ? " AND " : " OR ");
//			firstClause = false;

			if (line instanceof HBox)
			{
				try
				{
					HBox lineBox = (HBox) line;
					ChoiceBox<String> fldName = (ChoiceBox<String>) lineBox.getChildren().get(0);
					ChoiceBox<String> verbBox = (ChoiceBox<String>) lineBox.getChildren().get(1);
					ComboBox<String> contentBox = (ComboBox<String>) lineBox.getChildren().get(2);
					String fld = fldName.getSelectionModel().getSelectedItem();
					String content = contentBox.getEditor().getText();  //getSelectionModel().getSelectedItem();
					
					if (StringUtil.anyEmpty(fld, content))  continue;
						
					String plus = content.replace(' ', '+');
					buf.append(plus);
					if (!fld.startsWith("All"))
						buf.append("[" + fld.toLowerCase() + "]");
					buf.append("+");
				}
				catch( Exception e) {}
			}
		}
		buf.setLength(buf.length()-1);		// remove trailing +
		System.out.println(buf.toString());
		return buf.toString();
	}


	//------------------------------------------------------------------------------
	HBox makeCriteriaLine(String[] fieldList, final int index)
	{
		String[] verbs = new String[] { "is like", "is not like", "is", "is not",  "contains", "does not contain"   };
		ObservableList<String> verbList = FXCollections.observableArrayList();
		for (String v : verbs)
			verbList.add(v);
		
		
		HBox line = new HBox(6);
		ObservableList<String> strs = FXCollections.observableArrayList(fieldList);
		ChoiceBox<String> fieldBox = new ChoiceBox<String>(strs);
		fieldBox.getSelectionModel().clearAndSelect(1);
//		ChoiceBox<String> verbBox = new ChoiceBox<String>(verbList);
//		verbBox.getSelectionModel().clearAndSelect(0);
		ComboBox<String> contentBox  = new ComboBox<String>();
		contentBox.setEditable(true);
		Button plusButton = new Button("+");
		plusButton.setStyle("-fx-background-radius: 20; ");
		plusButton.setOnAction(event -> {	addLine(line); });
		Pane spacer = new Pane();
		spacer.setPrefSize(40,20);
		contentBox.setPrefWidth(150);
		line.getChildren().addAll(fieldBox, contentBox, spacer, plusButton );  //verbBox, 
		if (index > 0)  
		{
			Button minusButton = new Button("-");
			minusButton.setStyle("-fx-background-radius: 20; ");
			minusButton.setOnAction(event -> {	removeLine(line); });
			line.getChildren().addAll(minusButton);
		}
		return line;
	}
	//------------------------------------------------------------------------------

	private void addLine(HBox predecessor)
	{
		int idx = 1 + box.getChildren().indexOf(predecessor);
		HBox line = makeCriteriaLine(fields, idx);	
		box.getChildren().add(idx, line);
	
		double lineheight = 36;  
		Stage stage = (Stage)(box.getScene().getWindow());
		double curHght = 20 + getHeight();
		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght+lineheight);
		anim.play();
	
	}
	
	private void removeLine(HBox line)
	{
		box.getChildren().remove(line);
		double lineheight = line.getHeight() + SPACING;
		Stage stage = (Stage) getScene().getWindow();
		double curHght = 20 + getHeight();
		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght-lineheight);
		anim.play();
	}

	//http://www.ncbi.nlm.nih.gov/books/NBK25501/
	String[] fields = new String[] {"Affiliation",
	"All Fields",
	"Author",	"Author -Corporate",	"Author -First",	"Author - Full",	"Author - Identifier",	"Author - Last",
	"Book",
	"Date - Completion",	"Date - Create",	"Date - Entrez",	"Date - MeSH",	"Date -Modification",	"Date - Publication",
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