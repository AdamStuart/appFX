//package table.binder.tablecellHelpers;
//
//import javafx.scene.control.TableCell;
//import javafx.scene.control.TextField;
//import table.binder.Rect;
//import util.StringUtil;
//
//// ------------------------------------------------------------------------------
//
//class EditingCell extends TableCell<Rect, Double>
//{
//
//	private TextField textField;
//
//	public EditingCell()
//	{}
//
//	@Override public void startEdit()
//	{
//		if (!isEmpty())
//		{
//			super.startEdit();
//			createTextField();
//			setText(null);
//			setGraphic(textField);
//			textField.selectAll();
//		}
//	}
//
//	@Override public void cancelEdit()
//	{
//		super.cancelEdit();
//
//		setText("" + getItem());
//		setGraphic(null);
//	}
//
//	@Override public void updateItem(Double item, boolean empty)
//	{
//		super.updateItem(item, empty);
//
//		if (empty)		{			setText(null);			setGraphic(null);		} 
//		else if (isEditing())
//		{
//			if (textField != null)
//				textField.setText(getString());
//			setText(null);
//			setGraphic(textField);
//		} 
//		else			{			setText(getString());		setGraphic(null);		}
//	}
//
//	private void createTextField()
//	{
//		textField = new TextField(getString());
//		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//		textField.focusedProperty().addListener((a,b,c) ->
//			 {	if (!c) commitEdit(StringUtil.toDouble(textField.getText())); });
//	}
//
//	private String getString()
//	{
//		return getItem() == null ? "" : getItem().toString();
//	}
//}
//
