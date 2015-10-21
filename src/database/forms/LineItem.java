package database.forms;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class LineItem
{
	IntegerProperty status;				// replace with an enum NEW, ON_ORDER, ON_BACKORDER, DELIVERED, CONSUMED  ??
	IntegerProperty qty;
	StringProperty sku;
	StringProperty desc;
	StringProperty amt;
	DoubleProperty unitPrice;
	DoubleProperty extPrice;
	InvoiceForm parentForm;
	
    @Override      public String toString() {         return getDesc();    }

    public LineItem(InvoiceForm form)
    {
    	parentForm = form;
    	extPriceProperty().addListener(observable -> { 		parentForm.retotal();	});
   }
    
        public static LineItem  makeLineItem(InvoiceForm form)
    {
    	LineItem item = new LineItem(form);
    	item.setQty(1);
    	item.setSKU("--SKU--");
    	item.setDesc("------- description -------");
    	item.setUnitPrice(0);
    	item.setExtPrice(0);
    	return item;
    }
    
    public static LineItem  makeLineItem(Product p, InvoiceForm form)
    {
    	LineItem item = new LineItem(form);
    	item.setQty(1);
    	item.fill(p);
    	return item;
    }
    
    
    public void fill(Product p)
    {
    	if (p == null) 
    	{
        	setDesc("");
        	setAmt("");
        	setUnitPrice(0);
    	}
    	else
    	{
    		setSKU(p.getSKU());
    		setDesc(p.getDesc());
    		setAmt(p.getAmt());
    		setUnitPrice(p.getUnitPrice());
    	}
    	recalc();
    }
    
    public void recalc()
    {
    	setExtPrice(getUnitPrice() * getQty());
    	parentForm.retotal();			// binding in line 31 not working??
    }
//--------------------------------------------------------------------------------------------    
    public Property<?> getProperty(String name)
    {
    	if ("qty".equals(name))  return qty;
    	if ("sku".equals(name))  return sku;
    	if ("desc".equals(name))  return desc;
    	if ("amt".equals(name))  return amt;
    	if ("unitPrice".equals(name))  return unitPrice;
    	if ("extPrice".equals(name))  return extPrice;
    	return null;
    }
    
  //--------------------------------------------------------------------------------------------    
    public IntegerProperty statusProperty() 
    {    
    	if (status == null)   status = new SimpleIntegerProperty();  
        return status;
    }
    public int getStatus() 				{    return status != null ? statusProperty().get() : 0;  }
    public void setStatus(int q) 		{      statusProperty().set(q);   }

    //--------------------------------------------------------------------------------------------    
    public IntegerProperty qtyProperty() 
    {    
    	if (qty == null)   qty = new SimpleIntegerProperty();  
        return qty;
    }
    public int getQty() 				{    return qty != null ? qtyProperty().get() : 0;  }
    public void setQty(int q) 			{      qtyProperty().set(q);   }

    //--------------------------------------------------------------------------------------------    
    public StringProperty skuProperty() {    
    	if (sku == null) {   sku = new SimpleStringProperty();   }
        return sku;
    }
    public String getSKU() 				{   	return sku != null ? skuProperty().get() : "";  }
    public void setSKU(String name) 	{      skuProperty().set(name);   }

    //--------------------------------------------------------------------------------------------    
    public StringProperty descProperty() 
    {    
    	if (desc == null)    desc = new SimpleStringProperty();  
        return desc;
    }
    public String getDesc() 			{   	return desc != null ? descProperty().get() : "";  }
    public void setDesc(String name) 	{      descProperty().set(name);   }

    
    //--------------------------------------------------------------------------------------------    
    public StringProperty amtProperty() {    
    	if (amt == null) {   amt = new SimpleStringProperty();   }
        return amt;
    }
    public String getAmt() 				{   	return amt != null ? amtProperty().get() : "";  }
    public void setAmt(String name)	 	{      amtProperty().set(name);   }

 
    //--------------------------------------------------------------------------------------------    
    public DoubleProperty unitPriceProperty() {    
    	if (unitPrice == null) {   unitPrice = new SimpleDoubleProperty();   }
        return unitPrice;
    }
    public double getUnitPrice() 		{   	return unitPrice != null ? unitPriceProperty().get() : 0;  }
    public void setUnitPrice(double d) 	{      unitPriceProperty().set(d);   }

    //--------------------------------------------------------------------------------------------    
    public DoubleProperty extPriceProperty() {    
    	if (extPrice == null)    extPrice = new SimpleDoubleProperty();   
        return extPrice;
    }
    public double getExtPrice() 		{   return extPrice != null ? extPriceProperty().get() : 0;  }
    public void setExtPrice(double d) 	{   extPriceProperty().set(d);   }


   //   
//    
//    public BooleanProperty selectedProperty() {   
//    	if (selected == null) {   selected = new SimpleBooleanProperty();         }
//        return selected;
//    }
//
//    public boolean isSelected() {   return selected != null ? selectedProperty().get() : false;      }
//    public void setSelected(boolean selected) {  selectedProperty().set(selected);     }
//

 
}