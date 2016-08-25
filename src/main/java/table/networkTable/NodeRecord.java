package table.networkTable;

import java.util.HashMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import model.Range;

public class NodeRecord extends AbstractTableRow
{
	public NodeRecord(String id2, String nam)
	{
		super(id2, nam);
	}

	SimpleStringProperty description = new SimpleStringProperty();
	
	public NodeRecord(String id, String abbrev, String nam, String desc)
	{
		super(id, abbrev);
		setDescription(desc);
	}
	
	public SimpleStringProperty descriptionProperty()	{ return description;	};
	public 	String  getDescription()					{ return description.get();	};
	public 	void setDescription(String s)				{ description.set(s);	}
	public boolean equals(NodeRecord other) {	return description.equals(other.getDescription());}
	// ---------------------------------------------------------
	double[] coexpression = null;
	HashMap<String, Double> coexpressionMap = null;
	HashMap<String, Double> normalized = null;
	Range range = new Range();			// for now its always 0-1
	public Range getRange() {		return range;	}
	
	public void buildCoexpressionArray(ObservableList<NodeRecord> items) {
		if (coexpression == null)
		{
			int siz = items.size();
			coexpression = new double[siz];
			for (int i = 0; i< siz; i++)
			{
				coexpression[i] = (items.get(i).equals(this)) ? 1 : 0.1 * i;
			}
		}
	}
	public HashMap<String, Double> buildCoexpressionMap(ObservableList<NodeRecord> items, double val) {
		
		buildCoexpressionArray(items);
		if (coexpressionMap == null)
		{
			int siz = items.size();
			coexpressionMap = new HashMap<String, Double>();
			for (int i = 0; i< siz; i++)
			{
				NodeRecord rec = items.get(i);
				coexpressionMap.put(rec.getName(), (rec.equals(this)) ? 1. : val + 0.1 * i);
			}
		}
		return coexpressionMap;
	}
	private HashMap<String, Double> getCoexpressionMap() {
		return coexpressionMap;
	}

	public void resetCoexpression() { coexpression = null;	}


}
