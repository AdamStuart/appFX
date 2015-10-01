package chart.fancychart;

import java.text.DecimalFormat;

import model.DataItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class DoubleEditingCell extends TableCell<DataItem, Number>
{
	protected TextField textField;
	protected int nDecimalDigits;
	
	public DoubleEditingCell(int nDigits)
	{
		nDecimalDigits =  (nDigits < 0 || nDigits > 3) ? 2 : nDigits;
	}
	
	@Override public void startEdit() {
		if (!isEmpty()) 
		{
			super.startEdit();
			createTextField();
			setText(null);
			setGraphic(textField);
			textField.selectAll();
		}
	}
	
	@Override public void cancelEdit() {
		super.cancelEdit();
		setText(getItem().toString());
		setGraphic(null);
	}
	
	@Override public void updateItem(final Number item, final boolean empty) {
		super.updateItem(item, empty);
	
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}
	
	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(getWidth() - getGraphicTextGap() * 2);
		final ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
			@Override public void changed(final ObservableValue<? extends Boolean> value, final Boolean oldValue,
					final Boolean newValue) {  if (!newValue) 	setValue();		}
		};
		textField.focusedProperty().addListener(changeListener);
		textField.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override public void handle(final KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) 	setValue();	
				if (event.getCode().equals(KeyCode.TAB)) 	setValue();	
			}
		});
	}
	
	private void setValue() {
		try {
			final double input = Double.valueOf(textField.getText());
			commitEdit(input);
		} catch (final NumberFormatException exception) {
			System.err.println(exception.getMessage());
			cancelEdit();
		}
	}
    public static final DecimalFormat[] FORMATS =  { new DecimalFormat("0"), new DecimalFormat("0.0"), new DecimalFormat("0.00"), new DecimalFormat("0.000") } ;

	private String getString() {
		return (getItem() == null) ? "": FORMATS[nDecimalDigits].format(getItem());
		}
	}
	
