package table.networkTable;

import javafx.beans.property.SimpleStringProperty;

public class NodeRecord extends AbstractTableRow
{
	public NodeRecord(String id2, String nam)
	{
		super(id2, nam);
	}

	SimpleStringProperty description = new SimpleStringProperty();
	
	public NodeRecord(String id, String nam, String desc)
	{
		super(id, nam);
		setDescription(desc);
	}
	
	public SimpleStringProperty descriptionProperty()	{ return description;	};
	public 	String  getDescription()			{ return description.get();	};
	public 	void setDescription(String s)	{ description.set(s);	};

}
