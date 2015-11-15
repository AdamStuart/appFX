package container.publish;

import icon.GlyphsDude;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class CriterionBox extends HBox
{
	static int SPACING = 10;
	VBox parent;
	ObservableList<String> fields;
	ChoiceBox<String> fieldBox;
	ComboBox<String> contentBox;
	
	public CriterionBox(ObservableList<String> flds, boolean isFirst, VBox parentVBox)
	{
		super(12);
		fields = flds;
		parent = parentVBox;
		fieldBox = new ChoiceBox<String>(fields);
		fieldBox.getSelectionModel().clearAndSelect(1);
		contentBox  = new ComboBox<String>();
		contentBox.setEditable(true);
		Button plusButton = GlyphsDude.plusButton();
		plusButton.setStyle("-fx-background-radius: 20; ");
		plusButton.setOnAction(event -> {	addLine(this); });
		Pane spacer = new Pane();
		spacer.setPrefSize(10,20);
		contentBox.setPrefWidth(150);
		getChildren().addAll(fieldBox, contentBox, spacer, plusButton );  //verbBox, 
		if (!isFirst)  
		{
			Button minusButton = GlyphsDude.minusButton();
			minusButton.setStyle("-fx-background-radius: 20; ");
			minusButton.setOnAction(event -> {	removeLine(this); });
			getChildren().addAll(minusButton);
		}
	}
		
	public void set(String fieldName, String content)
	{
		fieldBox.getSelectionModel().select(fieldName);		
		contentBox.getEditor().setText(content);			
	}
	
	private void addLine(HBox predecessor)
	{
		int myIndex = parent.getChildren().indexOf(this);
		CriterionBox line = new CriterionBox(fields, false, parent);	
		parent.getChildren().add(1 + myIndex, line);
	
//		double lineheight = 36;  
//		Stage stage = (Stage)(parentBox.getScene().getWindow());
//		double curHght =  stage.getHeight();
//		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght+lineheight);
//		anim.play();
	
	}
	
	private void removeLine(HBox line)
	{
		parent.getChildren().remove(line);
//		double lineheight = line.getHeight() + SPACING;
//		Stage stage = (Stage) getScene().getWindow();
//		double curHght = 20 + stage.getHeight();
//		WindowSizeAnimator anim = new WindowSizeAnimator(stage, curHght, curHght-lineheight);
//		anim.play();
	}

}
