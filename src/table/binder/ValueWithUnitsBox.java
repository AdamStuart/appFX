package table.binder;

import gui.NumberField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.Unit;
import model.ValueUnitRecord;
import util.NodeUtil;
import util.StringUtil;

public class ValueWithUnitsBox extends HBox
{
	private TextField txtEntry;
	private ChoiceBox<Unit> unitEntry;
	private Label prompt;
	BindingsController controller; 
	int promptWidth = 60;
	int textWidth = 80;
	int unitWidth = 55;
	
	//------------------------------------------------------------------------------
	public ValueWithUnitsBox(String id, BindingsController c)
	{
		this(id, id, c);
	}
	
	public ValueWithUnitsBox(String id, String promptString, BindingsController c)
	{
		super(10);
		setId(id);
		controller = c;
		prompt = new Label(promptString);
		NodeUtil.forceWidth(prompt, promptWidth);
		prompt.getStyleClass().add("prompt");
		prompt.setTranslateX(4);		// scoot it in and down a tad
		prompt.setTranslateY(6);
		txtEntry = new NumberField();
		txtEntry.setId(id + ".fld");
		NodeUtil.forceWidth(txtEntry, textWidth);
		txtEntry.getStyleClass().add("numeric");
		
		unitEntry = new ChoiceBox<Unit>();
		unitEntry.setId(id + ".unit");
		NodeUtil.forceWidth(unitEntry, unitWidth);
		unitEntry.setItems(Unit.getNames());
		unitEntry.getSelectionModel().select(Unit.IN);
		ReadOnlyObjectProperty<Unit> units = unitEntry.getSelectionModel().selectedItemProperty();
		units.addListener( (obs, old, newV) -> 	{ controller.setUnits(getId(), unitEntry.getSelectionModel().getSelectedItem());});
		
	  	 NumberBinding binding = Bindings.createDoubleBinding(() -> getValue());
	     NodeUtil.invalOnActionOrFocusLost(txtEntry, binding); 
	     binding.addListener((obs, oldVal, newVal) ->  
	     	{  if (oldVal != newVal)	controller.setValue(getId(), (double) newVal);  
	     	System.out.println("setValue  "+ getId());
	     	});
//		createCommitBinding(txtEntry, controller);	
//		txtEntry.focusedProperty().addListener((obs, old, isFocused)-> { 
//			if (! isFocused) {	controller.setValue(getId(), getValue()); }  });
//		txtEntry.setOnAction(e -> {controller.setValue(getId(), getValue()); } );

		getChildren().addAll(prompt, txtEntry, unitEntry);
	}
//===================================================================

//	public void install(ValueUnitRecord rec)		{	install(rec.getVal(), rec.getUnit());	}
//	public ValueUnitRecord extract(double d, Unit u){	return new ValueUnitRecord(getValue(), getUnit());		}

	
	public void install(ValueUnitRecord rec)
	{
		txtEntry.setText(String.format("%.2f", rec.getVal()));
		int idx = unitEntry.getItems().indexOf(rec.getUnit());
		if (idx >= 0)
			unitEntry.getSelectionModel().select(idx);
	}
	
	//------------------------------------------------------------------------------
	public Double getValue()		{		return StringUtil.toDouble(txtEntry.getText());	}
	public Unit getUnit()			{		return unitEntry.getSelectionModel().getSelectedItem();	}
	public void setValue(Double d)	{		txtEntry.setText(d.toString());	}
	public void setUnit(Unit u)		{		unitEntry.getSelectionModel().select(u);	}
//------------------------------------------------------------------------------
//
	private void createCommitBinding(TextField textField, BindingsController control) 
	{
	  	 NumberBinding binding = Bindings.createDoubleBinding(() -> getValue());
	     NodeUtil.invalOnActionOrFocusLost(textField, binding); 
	     binding.addListener((obs, oldVal, newVal) ->  {  if (oldVal != newVal)	controller.setValue(getId(), (double) newVal);    });
   }
}
