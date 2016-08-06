package table.networkTable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

public class InspectorController implements Initializable{

	@FXML private ChoiceBox<StrokeOption> strokes;
	@FXML private ChoiceBox<ArrowShapeOption> srcArrow;
	@FXML private ChoiceBox<ArrowShapeOption> targetArrow;
	@FXML private ChoiceBox<Shape> shapes;
	
	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		populateArrowChoices();
		populateStrokeChoices();
		populateShapeChoices();
	}

	private void populateShapeChoices() {
		for (Shape opt : Shape.values())
			shapes.getItems().add(opt);
	}

	private void populateStrokeChoices() {
		for (StrokeOption opt : StrokeOption.values())
			strokes.getItems().add(opt);
	}

	private void populateArrowChoices() {
		for (ArrowShapeOption opt : ArrowShapeOption.values()) {
			srcArrow.getItems().add(opt);
			targetArrow.getItems().add(opt);
		}
	}

}
