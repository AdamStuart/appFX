package table.binder.tablecellHelpers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import model.stat.Unit;
import table.binder.Rect;
// TODO -- Deprecate??  I think this is now included in FX
// ------------------------------------------------------------------------------
public class ChoiceBoxTableCell extends TableCell<Rect, Unit>
{

	private final ChoiceBox<Unit> box;
//	private boolean selectRowOnClick = true;
	public ChoiceBoxTableCell(TableColumn<Rect, Unit> column, ObservableList<Unit> choiceList)
	{
		this(column, choiceList, false);
	}
	
	public ChoiceBoxTableCell(TableColumn<Rect, Unit> column, ObservableList<Unit> choiceList, boolean squared)
	{
		box = new ChoiceBox<Unit>();
		box.setItems(FXCollections.observableArrayList(Unit.values()));
		box.disableProperty().bind(column.editableProperty().not());
		box.showingProperty().addListener(event ->
		{
			TableView<Rect> tv = getTableView();
			TableViewSelectionModel<Rect> sel = tv.getSelectionModel();
			int index = getTableRow().getIndex();
			boolean wasSelected = sel.isSelected(index);
			boolean val = box.showingProperty().getValue();
			if (!val) return;
			
			System.out.println(" index = " + index + (val ? " true" : " false"));
			if (wasSelected)	tv.edit(index, column);
			else 				sel.select(index); 
			tv.requestFocus();
		});
		box.valueProperty().addListener((obs, old, newValue) -> {	if (isEditing())	commitEdit(newValue);	});
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

//	public ChoiceBoxTableCell selectRowOnClick(boolean sel)
//	{
//		selectRowOnClick = sel;
//		return this;
//	}

	@Override protected void updateItem(Unit item, boolean empty)
	{
		super.updateItem(item, empty);

		setText(null);
		if (empty || item == null)			setGraphic(null);
		else					{			box.setValue(item);			setGraphic(box);		}
	}

}
