package database.forms;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Product
{
	private StringProperty sku;
	private StringProperty desc;
	private StringProperty amt;
	private DoubleProperty unitPrice;

	//----------------------------------------------------------------------------
	public Property getProperty(String name)
    {
    	if ("sku".equals(name))  return sku;
    	if ("desc".equals(name))  return desc;
    	if ("amt".equals(name))  return amt;
    	if ("unitPrice".equals(name))  return unitPrice;
    	return null;
    }

	//----------------------------------------------------------------------------
    public StringProperty skuProperty() {    
    	if (sku == null) {   sku = new SimpleStringProperty();   }
        return sku;
    }
    public String getSKU() 				{   	return sku != null ? skuProperty().get() : "";  }
    public void setSKU(String name) 	{      skuProperty().set(name);   }

    
    public StringProperty descProperty() 
    {    
    	if (desc == null)    desc = new SimpleStringProperty();  
        return desc;
    }
    public String getDesc() 			{   	return desc != null ? descProperty().get() : "";  }
    public void setDesc(String name) 	{      descProperty().set(name);   }

    
    public StringProperty amtProperty() {    
    	if (amt == null) {   amt = new SimpleStringProperty();   }
        return amt;
    }
    public String getAmt() 				{   	return amt != null ? amtProperty().get() : "";  }
    public void setAmt(String name)	 	{      amtProperty().set(name);   }

 
    public DoubleProperty unitPriceProperty() {    
    	if (unitPrice == null) {   unitPrice = new SimpleDoubleProperty();   }
        return unitPrice;
    }
    public double getUnitPrice() 		{   	return unitPrice != null ? unitPriceProperty().get() : 0;  }
    public void setUnitPrice(double d) 	{      unitPriceProperty().set(d);   }

    
    //----------------------------------------------------------------------------
   public static Product findSKU(String sku)
    {
    	for (Product p : catalog)
    		if (sku.compareToIgnoreCase(p.getSKU()) == 0)
    			return p;
    	return null;
    }
     //----------------------------------------------------------------------------
    public static Product  makeProduct()
    {
    	Product item = new Product();
    	item.setSKU("--SKU--");
    	item.setDesc("------- description -------");
    	item.setAmt("1 unit");
    	item.setUnitPrice(0);
    	return item;
    }
    public static Product  makeProduct(String sku, String desc, String amt, Double price)
    {
    	Product item = new Product();
    	item.setSKU(sku);
    	item.setDesc(desc);
    	item.setAmt(amt);
    	item.setUnitPrice(price);
    	return item;
    }
    
    //----------------------------------------------------------------------------
    public static List<Product> catalog;

    public static List<Product> makeDummyCatalog()
    {
        catalog = makeCatalog();
        catalog.add(makeProduct("001", "Canned Tuna", "8 oz.", 3.49));
        catalog.add(makeProduct("002", "Canned Soup", "12 oz.", 2.09));
        catalog.add(makeProduct("003", "Canned Beer", "12 oz.", 6.45));
        catalog.add(makeProduct("004", "Spinach", "1 lb.", 4.49));
        catalog.add(makeProduct("005", "Hamburger", "1 lb.", 7.89));
        return catalog;
    }
    
    public static List<Product> makeCatalog()
    {
    	return new ArrayList<Product>();
    }
    
     
 
}
