package table.networkTable;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import model.AttributeValue;

public class AbstractTableRow
{
	public AbstractTableRow(String i, String nam)
	{
		setId(i);
		setName(nam);
	}
	protected SimpleStringProperty id = new SimpleStringProperty();
	protected SimpleStringProperty name = new SimpleStringProperty();
	protected SimpleBooleanProperty selected = new SimpleBooleanProperty();
	protected List<AttributeValue> properties;

	public SimpleStringProperty idProperty()	{ return id;	};
	public 	String  getId()			{ return id.get();	};
	public 	void setId(String s)	{ id.set(s);	};

	public SimpleStringProperty nameProperty()	{ return name;	};
	public 	String  getName()			{ return name.get();	};
	public 	void setName(String s)	{ name.set(s);	};

	public SimpleBooleanProperty selectedProperty()	{ return selected;	};
	public 	boolean  getSelected()			{ return selected.get();	};
	public 	void setSelected(boolean s)	{ selected.set(s);	};
}
