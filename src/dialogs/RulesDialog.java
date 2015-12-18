package dialogs;

import java.net.URL;

import animation.WindowSizeAnimator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.StringUtil;

//ObservableList<String> fields = FXCollections.observableArrayList();
//fields.addAll("Name", "Age", "Gender", "Date");
// String clause = new RulesDialog(fields);

public class RulesDialog extends Dialog
{
	static int SPACING = 10;
	VBox box = new VBox(SPACING);
	ObservableList<String> fieldList;
	RadioButton r1;
	
	public RulesDialog(ObservableList<String> fields)
	{
		super();
		fieldList = fields;
		setTitle("Rule Definition");
		setHeaderText("Create the WHERE clause of your search");
	    DialogPane dialogPane = getDialogPane();
//		    dialogPane.getStyleClass().add("rulesDialog");
	    

		// Set the icon (must be included in the project).
		URL res =  getClass().getResource("rules.png");
		if (res != null)
			setGraphic(new ImageView(res.toString()));

		// Set the button types.
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		box.setPadding(new Insets(20, 150, 10, 10));

//		CheckBox caseSens = new CheckBox("Case sensitive");
//		box.getChildren().addAll(caseSens);	
		
		Label l1 = new Label("Select the records where ");
		Label l2 = new Label("of these criteria hold. ");
		r1 = new RadioButton("All");
		RadioButton r2 = new RadioButton("Any");
		ToggleGroup group = new ToggleGroup();
		r1.setToggleGroup(group);
		r2.setToggleGroup(group);
		VBox andOr = new VBox(4);
		andOr.getChildren().addAll(r1,r2);
		HBox andOrLine = new HBox(10);
		HBox.setMargin(l1, new Insets(10,0,0,0));
		HBox.setMargin(l2, new Insets(10,0,0,0));
		andOrLine.getChildren().addAll(l1, andOr, l2);
		r1.setSelected(true);
		
		VBox dlogContent = new VBox();
		dlogContent.getChildren().addAll(andOrLine, box);
		dialogPane.setContent(dlogContent);

		HBox line = makeCriteriaLine(fieldList, 0);	
		HBox line2 = makeCriteriaLine(fieldList, 1);	
		box.getChildren().addAll(line, line2);

		setResultConverter(dialogButton -> {
			return (dialogButton == okButtonType) ? extract() : "";
		});

	}
	//------------------------------------------------------------------------------

	public String extract()
	{
		StringBuilder buf = new StringBuilder("WHERE ");
		boolean firstClause = true;
		for (Node line : box.getChildren())
		{
			if (!firstClause)
				buf.append(r1.isSelected() ? " AND " : " OR ");
			firstClause = false;

			if (line instanceof HBox)
			{
				try
				{
					HBox lineBox = (HBox) line;
					ChoiceBox<String> fldName = (ChoiceBox<String>) lineBox.getChildren().get(0);
					ChoiceBox<String> verbBox = (ChoiceBox<String>) lineBox.getChildren().get(1);
					ComboBox<String> contentBox = (ComboBox<String>) lineBox.getChildren().get(2);
					String fld = fldName.getSelectionModel().getSelectedItem();
					String verb = verbBox.getSelectionModel().getSelectedItem();
					String content = contentBox.getSelectionModel().getSelectedItem();
					
					if (StringUtil.anyEmpty(fld, verb, content))  continue;
					buf.append(fld).append(" ").append(verb).append(" ").append(StringUtil.singleQuote(content)).append("\n");
				}
				catch( Exception e) {}
			}
		}
		return buf.toString();
	}
	
	//------------------------------------------------------------------------------
	HBox makeCriteriaLine(ObservableList<String> fieldList, final int index)
	{
		String[] verbs = new String[] { "is like", "is not like", "is", "is not",  "contains", "does not contain"   };
		ObservableList<String> verbList = FXCollections.observableArrayList();
		for (String v : verbs)
			verbList.add(v);
		
		
		HBox line = new HBox(6);
		ChoiceBox<String> fieldBox = new ChoiceBox<String>(fieldList);
		fieldBox.getSelectionModel().clearAndSelect(1);
		ChoiceBox<String> verbBox = new ChoiceBox<String>(verbList);
		verbBox.getSelectionModel().clearAndSelect(0);
		ComboBox<String> contentBox  = new ComboBox<String>();
		contentBox.setEditable(true);
		Button plusButton = new Button("+");
		plusButton.setStyle("-fx-background-radius: 20; ");
		plusButton.setOnAction(event -> {	addLine(line); });
		Pane spacer = new Pane();
		spacer.setPrefSize(40,20);
		contentBox.setPrefWidth(150);
		line.getChildren().addAll(fieldBox, verbBox, contentBox, spacer, plusButton );
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
		HBox line = makeCriteriaLine(fieldList, idx);	
		box.getChildren().add(idx, line);
	
		double lineheight = 36;  
		Stage stage = (Stage)(box.getScene().getWindow());
		double curHght = 20 + getDialogPane().getHeight();
		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght+lineheight);
		anim.play();
	
	}
	
	private void removeLine(HBox line)
	{
		box.getChildren().remove(line);
		double lineheight = line.getHeight() + SPACING;
		Stage stage = (Stage)(getDialogPane().getScene().getWindow());
		double curHght = 20 + getDialogPane().getHeight();
		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght-lineheight);
		anim.play();
	}
}
