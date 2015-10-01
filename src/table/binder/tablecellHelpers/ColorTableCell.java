package table.binder.tablecellHelpers;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import table.binder.Rect;


public class ColorTableCell<MyColorable> extends TableCell<MyColorable, Color>
{
	private ColorPicker colorPicker;

	private ColorPicker createPicker()
	{
		colorPicker = new ColorPicker();

		colorPicker.setOnAction(evt ->
		{
			ColorPicker cp = (ColorPicker) evt.getSource();
			Color cw = (Color) cp.getValue();
			cw = cp.getValue();
			getTableView().getSelectionModel().select(getTableRow().getIndex());
			int idx = getTableView().getSelectionModel().getSelectedIndex();
			MyColorable rec = getTableView().getItems().get(idx);
			if (rec instanceof Rect)
			{
				((Rect)rec).setColor(cw);
			}
		});
		return colorPicker;
	}

	protected void updateItem(Color value, boolean empty)
	{
		super.updateItem(value, empty);
		if (empty)
		{
			setGraphic(null);
			return; // http://stackoverflow.com/questions/25532568/javafx-tableview-delete-issue
		}

		if (colorPicker == null)
		{
			colorPicker = createPicker();
			colorPicker.setUserData(value);
		}

		colorPicker.setValue(value);
		setGraphic(colorPicker);
	}

	// };

	// }

}