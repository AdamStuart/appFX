package table.binder.tablecellHelpers;

import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.scene.control.TableCell;

public class BadgeTableCell<BadgeColumn> extends TableCell<BadgeColumn, Boolean>
{
	GlyphIcons icon;
	public BadgeTableCell(GlyphIcons i)
	{
		icon = i;
	}
	
	public void updateItem(Boolean value, boolean empty)
	{
		super.updateItem(value, empty);
		if (empty)
		{
			setGraphic(null);
			return; // http://stackoverflow.com/questions/25532568/javafx-tableview-delete-issue
		}
		setGraphic(value ? GlyphsDude.createIcon(icon)  : null );
	}
}