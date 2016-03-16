package table.networkTable;

import javafx.beans.property.SimpleStringProperty;

public class EdgeRecord extends AbstractTableRow
{
	SimpleStringProperty interaction = new SimpleStringProperty("is_a");	
	NodeRecord source, target;
	
	public EdgeRecord(String id, NodeRecord node1, NodeRecord node2, String interaction)
	{
		super(id, "");
		setInteraction(interaction);
		source = node1;
		target = node2;
	}
	
	public SimpleStringProperty interactionProperty()	{ return interaction;	};
	public 	String  getInteraction()			{ return interaction.get();	};
	public 	void setInteraction(String s)	{ interaction.set(s);	};



}
