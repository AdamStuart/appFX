package table.binder.tablecellHelpers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import table.binder.Rect;
import table.binder.Unit;

// ------------------------------------------------------------------------------
public class ChoiceBoxTableCell extends TableCell<Rect, Unit>
{

	private final ChoiceBox<Unit> box;
	private boolean selectRowOnClick = true;
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
			TableView tv = getTableView();
			if (selectRowOnClick)
			{
				tv.getSelectionModel().select(getTableRow().getIndex());
				tv.edit(tv.getSelectionModel().getSelectedIndex(), column);
			} else
			{
				tv.edit(getTableRow().getIndex(), column);
			}
		});
		this.box.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			if (isEditing())
			{
				commitEdit(newValue);
			}
		});
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	public ChoiceBoxTableCell selectRowOnClick(boolean selectRowOnClick)
	{
		this.selectRowOnClick = selectRowOnClick;
		return this;
	}

	@Override protected void updateItem(Unit item, boolean empty)
	{
		super.updateItem(item, empty);

		setText(null);
		if (empty || item == null)
		{
			setGraphic(null);
		} else
		{
			this.box.setValue(item);
			this.setGraphic(this.box);
		}
	}

}
