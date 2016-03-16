package table.networkTable;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class NetworkRecord extends AbstractTableRow
{
	SimpleStringProperty type = new SimpleStringProperty();
	SimpleIntegerProperty nNodes = new SimpleIntegerProperty();
	SimpleIntegerProperty nEdges = new SimpleIntegerProperty();
	NetworkRecord(String id, String nam)
	{
		super(id, nam);
	}

	public SimpleStringProperty typeProperty()		{ return type;	};
	public 	String  getType()						{ return type.get();	};
	public 	void setType(String s)					{ type.set(s);	};

	public SimpleIntegerProperty nNodesProperty()	{ return nNodes;	};
	public 	Integer  getNNodes()					{ return nNodes.get();	};
	public 	void setNNodes(Integer s)				{ nNodes.set(s);	};

	public SimpleIntegerProperty nEdgesProperty()	{ return nEdges;	};
	public 	Integer  getNEdges()					{ return nEdges.get();	};
	public 	void setNEdges(Integer s)				{ nEdges.set(s);	};
}
