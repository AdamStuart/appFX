package publish;

import javafx.beans.property.SimpleStringProperty;

public class ResultsRow {

	SimpleStringProperty label = new SimpleStringProperty("");
	public SimpleStringProperty labelProperty()	{ return label;	};
	public 	String  getLabel()					{ return label.get();	};
	public 	void setLabel(String s)				{ label.set(s);	};

	SimpleStringProperty name = new SimpleStringProperty("");
	public SimpleStringProperty nameProperty()	{ return name;	};
	public 	String  getName()					{ return name.get();	};
	public 	void setName(String s)				{ name.set(s);	};

	SimpleStringProperty id = new SimpleStringProperty("");
	public SimpleStringProperty idProperty()	{ return id;	};
	public 	String  getId()						{ return id.get();	};
	public 	void setId(String s)				{ id.set(s);	};

	SimpleStringProperty time = new SimpleStringProperty("");
	public SimpleStringProperty timeProperty()	{ return time;	};
	public 	String  getTime()					{ return time.get();	};
	public 	void setTime(String s)				{ time.set(s);	};

	SimpleStringProperty idList = new SimpleStringProperty("");
	public SimpleStringProperty idListProperty(){ return idList;	};
	public 	String  getIdList()					{ return idList.get();	};
	public 	void setIdList(String s)			{ idList.set(s);	};

	public ResultsRow(String[] split) {
		setLabel(split[0]);
		setId(split[1]);
		setName(split[2]);
		setTime(split[3]);
		setIdList(split[4]);
	}

}
