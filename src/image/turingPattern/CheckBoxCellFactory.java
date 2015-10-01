package image.turingPattern;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

public class CheckBoxCellFactory<TuringPattern, T> implements
		Callback<TableColumn<TuringPattern, T>, TableCell<TuringPattern, T>> {
	@Override
	public TableCell<TuringPattern, T> call(TableColumn<TuringPattern, T> p) {
		return new CheckBoxTableCell<>();
	}
}
