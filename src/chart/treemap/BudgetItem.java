package chart.treemap;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.DataItem;

public class BudgetItem implements Item
{
	    private DoubleProperty size = new SimpleDoubleProperty(640.0);
	    private StringProperty label = new SimpleStringProperty();
	    private SimpleStringProperty id = new SimpleStringProperty();
	    DataItem rootItem = null;
	    SortedSet<Item> items =  new TreeSet<Item>();
	    
	    public    BudgetItem(String s, String lbel, double z)
		{
			id.set(s);
			label.set(lbel);
			size.set(z);
		}

		@Override public int compareTo(Item o)			{		return (int) (getSize() - o.getSize());	}

		@Override	public String getId()				{		return id.get();	}
		@Override	public double getSize()				{		return items.size();	}
		@Override	public String getLabel()			{		return label.get();	}
		@Override	public boolean isContainer() 		{		return items.size() > 0;	}
		@Override	public SortedSet<Item> getItems()	{		return items;	}
		public void addItem(Item i)						{		items.add(i);	}
		public void addItems(Item ... i)				{		items.addAll(Arrays.asList(i));	}
public String toString() { return label.get() + " = " + size.get() + " = " + getSize();	}
		//---------------------------------------------------------------------------
	static BudgetItem makeBudget()
	{
		BudgetItem rootItem = new BudgetItem("root", "Budget", 2000);
		BudgetItem a = new BudgetItem("a", "Budget", 200);
		BudgetItem b = new BudgetItem("b", "Defense", 200);
		BudgetItem c = new BudgetItem("c", "Environment", 200);
		BudgetItem d = new BudgetItem("d", "Health", 200);
		BudgetItem e = new BudgetItem("e", "Education", 200);
		BudgetItem f = new BudgetItem("f", "Administration", 500);
		BudgetItem g = new BudgetItem("g", "Research", 500);
		BudgetItem g1 = new BudgetItem("g1", "Biology", 100);
		BudgetItem g2 = new BudgetItem("g2", "Chemistry", 100);
		BudgetItem g3 = new BudgetItem("g3", "Economics", 100);
		BudgetItem g4 = new BudgetItem("g4", "Physics", 200);
		BudgetItem b1 = new BudgetItem("b1", "Army", 40);
		BudgetItem b2 = new BudgetItem("b2", "Navy", 50);
		BudgetItem b3 = new BudgetItem("b3", "Air Force", 30);
		BudgetItem b4 = new BudgetItem("b4", "Marines", 60);
		BudgetItem b5 = new BudgetItem("b4", "Coast Guard", 20);
		rootItem.addItems(a, b, c, d, e, f, g);
		g.addItems(g1, g2, g3, g4);
		b.addItems(b1, b2, b3, b4, b5);

		return rootItem;
	}
}


