package table.networkTable;

import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
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
	public boolean equals(NodeRecord other) {	return getName().equals(other.getName());}
	// ---------------------------------------------------------
	double[] coexpression = null;
	HashMap<String, Double> coexpressionMap = null;
	HashMap<String, Double> normalized = null;
	Range range = new Range();			// for now its always 0-1
	public Range getRange() {		return range;	}
	
	public void buildCoexpressionArray(List<NodeRecord> items) {
		if (coexpression == null)
		{
			int siz = items.size();
			coexpression = new double[siz];
			for (int i = 0; i< siz; i++)
				coexpression[i] = (items.get(i).equals(this)) ? 1 : 0.01 * i;
		}
	}
	
	public HashMap<String, Double> buildCoexpressionMap(List<NodeRecord> items, int mode, double base) //, double val
	{
		buildCoexpressionArray(items);
		if (coexpressionMap == null)
		{
			int siz = items.size();
			coexpressionMap = new HashMap<String, Double>();
			for (int i = 0; i< siz; i++)
			{
				NodeRecord rec = items.get(i);
				boolean identity = rec.equals(this);
				boolean random = mode == 0;
				double val = (identity ? 1. : (random ? Math.random() :  (base + i * 0.02)));
				coexpressionMap.put(rec.getName(), val );	
			}
		}
		return coexpressionMap;
	}
//	private HashMap<String, Double> getCoexpressionMap() {
//		return coexpressionMap;
//	}

	public void resetCoexpression() { coexpression = null;	coexpressionMap = null;	}
	public String toString()	{ return  getName(); }

}
