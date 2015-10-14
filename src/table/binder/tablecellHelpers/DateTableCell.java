package table.binder.tablecellHelpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class DateTableCell<Rec> extends TableCell<Rec, LocalDate>
{

	// https://gist.github.com/james-d/9776485
	// public static class DateTableCell extends TableCell<Rect, LocalDate>

	private final DateTimeFormatter formatter;
	private final DatePicker datePicker;

	public DateTableCell()
	{

		formatter = DateTimeFormatter.ISO_DATE; // ofPattern("DD-MMMM-YYYY") ;
		datePicker = new DatePicker();

		// Commit edit on Enter and cancel on Escape.
		// Note that the default behavior consumes key events, so we must
		// register this as an event filter to capture it.
		// Consequently, with Enter, the datePicker's value won't yet have been
		// updated, so commit will sent the wrong value. So we must update it ourselves
		// from the editor's text value.

		datePicker.setOnKeyPressed((event) ->		{
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB)
			{
				try
				{
					LocalDate val = datePicker.getConverter().fromString( datePicker.getEditor().getText());
					datePicker.setValue(val);
					commitEdit(LocalDate.from(datePicker.getValue()));
				} catch (Exception e)
				{}
			}
			if (event.getCode() == KeyCode.ESCAPE)		cancelEdit();
		});
		final StringConverter<LocalDate> defaultConverter = datePicker.getConverter();
		
		datePicker.setConverter(new StringConverter<LocalDate>()
		{
			@Override public String toString(LocalDate value)	{	return defaultConverter.toString(value);	}

			@Override public LocalDate fromString(String text)
			{
				try
				{
					return defaultConverter.fromString(text);
				} catch (DateTimeParseException ex)
				{
					System.err.println("HelloDatePicker: " + ex.getMessage());
					throw ex;
				}
			}
		});

		// Modify default mouse behavior on date picker:
		// Don't hide popup on single click, just set date
		// On double-click, hide popup and commit edit for editor
		// Must consume event to prevent default hiding behavior, so
		// must update date picker value ourselves.

		// Modify key behavior so that enter on a selected cell commits the edit
		// on that cell's date.

		datePicker.setDayCellFactory(picker ->
		{
			DateCell cell = new DateCell();
			cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event ->
			{
				datePicker.setValue(cell.getItem());
				if (event.getClickCount() == 2)
				{
					datePicker.hide();
					commitEdit(LocalDate.from(cell.getItem()));
				}
				event.consume();
			});
			cell.addEventFilter(KeyEvent.KEY_PRESSED, event ->
			{
				if (event.getCode() == KeyCode.ENTER)
					commitEdit(LocalDate.from(datePicker.getValue()));
			});
			return cell;
		});
		contentDisplayProperty().bind(
						Bindings.when(editingProperty()).then(ContentDisplay.GRAPHIC_ONLY)
										.otherwise(ContentDisplay.TEXT_ONLY));
	}
//----------------------------------------------------------------
	@Override public void updateItem(LocalDate inDate, boolean empty)
	{
		super.updateItem(inDate, empty);
		if (inDate == null)
			inDate = LocalDate.now();
		if (empty || inDate == null)
		{
			setText(null);		
			setGraphic(null);				// if you see fields left after the row is deleted, this is missing
		} 
		else
		{
			String text = "";
			try
			{
				text = formatter.format(inDate);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				text = "Error";
			}
			setText(text); 
			setGraphic(datePicker);
		}
	}
	//----------------------------------------------------------------
	@Override public void startEdit()
	{
		super.startEdit();
		if (!isEmpty())
		{
			LocalDate item = getItem();
			if (item != null)
				datePicker.setValue(getItem());
		}
	}

}
