package chart.treemap;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import chart.flexiPie.Wedge;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import model.DataItem;

public class BudgetItem implements Item
{
    private DoubleProperty amount = new SimpleDoubleProperty(640.0);
    private StringProperty label = new SimpleStringProperty();
    public DoubleProperty amountProperty() { return amount;	}
    public StringProperty labelProperty() { return label;	}
    private SimpleStringProperty id = new SimpleStringProperty();
    DataItem rootItem = null;
     List<Item> items =  new ArrayList<Item>();
    
    public    BudgetItem(String s, String lbel, double z)
	{
		id.set(s);
		label.set(lbel);
		amount.set(z);
	}

	@Override public int compareTo(Item o)			{		return (int) (getAmount() - o.getAmount());	}

	@Override	public String getId()				{		return id.get();	}
	@Override	public double getAmount()			{		return amount.get();	}
	@Override	public String getLabel()			{		return label.get();	}
	@Override	public boolean isContainer() 		{		return items.size() > 0;	}
	@Override	public  List<Item> getItems()		{		return items;	}
	public void addItem(Item i)						{		items.add(i);	}
	public void addItems(Item ... itemArray)		{		for (Item j : itemArray)	items.add(j);		}
	public String toString() { return label.get() + " = " + amount.get();	} 
	//---------------------------------------------------------------------------
	public static BudgetItem makeBudget()
	{
		BudgetItem rootItem = new BudgetItem("root", "Budget", 2000);
		BudgetItem a = new BudgetItem("a", "Foreign Aid", 200);
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
		g.addItem(g1);
		g.addItem(g2);
		g.addItem(g3);
		g.addItem(g4);
		b.addItems(b1, b2, b3, b4, b5);
		return rootItem;
	}

	
	public static TreeItem<BudgetItem> createTreeItems(BudgetItem inRoot)
	{
		TreeItem<BudgetItem> root = new TreeItem<BudgetItem>(inRoot);
		for (Item item : inRoot.getItems())
			root.getChildren().add(createTreeItems((BudgetItem)item));
		return root;
	}
}


