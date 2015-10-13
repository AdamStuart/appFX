package table.binder;

import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import model.Unit;
import model.ValueUnitRecord;

/**
 * Created by pedro_000 on 2/11/2015. 
 * calculation logic migrated from BindingsController, AST 2/19/2015.
 * refactored to use ValueUnitRecords to encapsulate units
 */
public class Rect
{
//	public static final Rect DEFAULT = new Rect(1, Unit.IN, 1, Unit.IN, Unit.IN);
	private final SimpleObjectProperty<ValueUnitRecord> width = new SimpleObjectProperty<ValueUnitRecord>(new ValueUnitRecord(1., Unit.IN));
	private final SimpleObjectProperty<ValueUnitRecord> height = new SimpleObjectProperty<ValueUnitRecord>(new ValueUnitRecord(1., Unit.IN));
	private final SimpleObjectProperty<ValueUnitRecord> area = new SimpleObjectProperty<ValueUnitRecord>(new ValueUnitRecord(1., Unit.IN));
	private final SimpleBooleanProperty fixedHeight = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty selected = new SimpleBooleanProperty(true);
	private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<Color>();
	private final SimpleObjectProperty<LocalDate> dueDate  = new SimpleObjectProperty<LocalDate>();

	//------------------------------------------------------------------------------
	final private BindingsController controller;
//	public void setController(BindingsController c)				{		controller = c;	}

	//------------------------------------------------------------------------------
	public Rect(BindingsController c)	{
		this(c, 1, Unit.IN, 1, Unit.IN, Unit.IN);
	}

	public Rect(BindingsController ct, double w, Unit widthUnits, double h, Unit heightUnits, Color c)	{
		this(ct, w, widthUnits, h, heightUnits, heightUnits,c, 5);
	}

	public Rect(BindingsController c, double w, Unit widthUnits, double h, Unit heightUnits, Unit areaUnits)	{
		this(c, w, widthUnits, h, heightUnits, heightUnits, Color.VIOLET, 5);
	}
	
	public Rect(BindingsController ct, double w, Unit widthUnits, double h, Unit heightUnits, Unit areaUnits, Color c, long nDays)
	{
		controller = ct;
		setWidth(w);		setWidthUnits(widthUnits);
		setHeight(h);		setHeightUnits(heightUnits);
		setAreaUnits(areaUnits); 
		color.set(c);
		dueDate.set(LocalDate.now().plusDays(nDays));
		recalcArea();
		
		width.addListener(ev -> { update(false); } );
		height.addListener(ev -> { update(false);} );
		area.addListener(ev -> { update(true);} );
		color.addListener(ev -> { update(false);} );
	}
	//------------------------------------------------------------------------------
	public void setVal(String id, double d)
	{
		if ("width".equals(id))  		setWidth(d);
		else if ("height".equals(id))  	setHeight(d);
		else if ("area".equals(id))  	setArea(d);
		update("area".equals(id));
	}
	//------------------------------------------------------------------------------
	public void setUnits(String id, Unit un)
	{
		if ("width".equals(id))  
		{
			Unit oldVal = getWidthUnits();
			double oldWidth = getWidthVal();
			double ratio = un.getPerMeter() / oldVal.getPerMeter();
			setWidthUnits(un);
			setWidth(oldWidth * ratio);
		}
		else if ("height".equals(id))  
		{
			Unit oldVal = getHeightUnits();
			double oldHeight = getHeightVal();
			double ratio = un.getPerMeter() / oldVal.getPerMeter();
			setHeight(oldHeight * ratio);
			setHeightUnits(un);
		}
		else if ("area".equals(id)) 
		{
			Unit oldVal = getAreaUnits();
			double oldArea = getAreaVal();
			double ratio = un.getPerMeter() / oldVal.getPerMeter();
			setArea(oldArea * ratio * ratio);		// ratio squared
			setAreaUnits(un);
		}
		update("area".equals(id));
	}
	//------------------------------------------------------------------------------
	private boolean updating = false;
	private void update(boolean isArea)
	{
		if (controller != null && !updating) 
		{
			updating = true;
			if (isArea) areaChanged(true);
			else recalcArea();
			controller.install();
			updating = false;
		}
	}

	// @formatter:off
	//------------------------------------------------------------------------------
	public void setSelected(boolean b)    		{       selected.set(b);    }
	public void setSelected(SimpleBooleanProperty w)  { selected.set(w.getValue());    }
	public boolean getSelected()    			{       return selected.getValue();    }
	public BooleanProperty selectedProperty()    {      return selected;    }

	public void setColor(Color c)    			{       color.set(c);    }
	public Color getColor()    					{       return color.get();    }
	public ObjectProperty<Color> colorProperty() {      return color;    }

    public ObjectProperty<ValueUnitRecord> widthRecordProperty()    {        return width;    }
    public ObjectProperty<ValueUnitRecord> heightRecordProperty()   {        return height;    }
    public ObjectProperty<ValueUnitRecord> areaRecordProperty()    	{        return area;    }

    public String getWidthAndUnits()		{		return String.format("%.2f %s", getWidthVal(),  getWidthUnits().asString()); }
    public String getHeightAndUnits()		{		return String.format("%.2f %s", getHeightVal(),  getHeightUnits().asString()); }
    public String getAreaAndUnits()			{		return String.format("%.2f %s^2", getAreaVal(),  getAreaUnits().asString()); }
    
    public void setWidth(double w)    		{        width.getValue().setVal(w);    }
    public void setWidthInMeters(double m)  {        setWidth(m * getWidthUnits().getPerMeter());    }
    public void setWidth(DoubleProperty w)  {        width.getValue().setVal(w.getValue());    }
    public double getWidthVal()    			{        return width.getValue().getVal();    }
    public double getWidthInMeters()    	{        return getWidthVal() / getWidthUnits().getPerMeter();    }
    public void setWidthUnits(Unit unit)    {        width.getValue().setUnit(unit);    }
    public Unit getWidthUnits()    			{        return width.getValue().getUnit();    }

    public void setHeight(double h)    		{        height.getValue().setVal(h);    }
    public void setHeightInMeters(double h) {        setHeight(h * getHeightUnits().getPerMeter());    }
    public void setHeight(DoubleProperty h) {        height.getValue().setVal(h.getValue());    }
    public double getHeightVal()    		{        return height.getValue().getVal();    }
    public double getHeightInMeters()    	{        return getHeightVal() / getHeightUnits().getPerMeter();    }
    public void setHeightUnits(Unit unit)   {        height.getValue().setUnit(unit);    }
    public Unit getHeightUnits()    		{        return height.getValue().getUnit();    }

    public void setArea(double a)   		{        area.getValue().setVal(a);    }
    public void setAreaInMeters(double m)   {        setArea(m * getAreaUnits().getPerMeter() * getAreaUnits().getPerMeter());    }
    public void setArea(DoubleProperty a)   {        area.getValue().setVal(a.getValue());    }
    public double getAreaVal()    			{        return area.getValue().getVal();    }
    public double getAreaInMeters()    		{        return getAreaVal() / (getAreaUnits().getPerMeter() * getAreaUnits().getPerMeter());    }
    public void setAreaUnits(Unit unit)    	{        area.getValue().setUnit(unit);    }
    public Unit getAreaUnits()   			{        return area.getValue().getUnit();    }

	public void setDueDate(LocalDate newValue)	{		dueDate.set( newValue);	}
	public LocalDate getDueDate()			{		return dueDate.get();	}

	@Override public String toString()	{ return "[" + getWidthAndUnits() + ", " + getHeightAndUnits() + "]"; }
	// @formatter:on
	// ------------------------------------------------------------------------------
	public void recalcArea()
	{
		double w = getWidthInMeters();
		double h = getHeightInMeters();
		Unit areaUnit = getAreaUnits();
		double areaSq = areaUnit.getPerMeter() * areaUnit.getPerMeter();		 // square the units
		double a = (w * h) * areaSq;
		setArea(a);
	}

	public boolean equals(Rect other)
	{
		if (other == null) return false;
		if (getWidthAndUnits().equals(other.getWidthAndUnits()))
			if (getHeightAndUnits().equals(other.getHeightAndUnits()))
				if (getAreaAndUnits().equals(other.getAreaAndUnits()))
					return true;
		return false;
	}
	// ------------------------------------------------------------------------------
	// If the area is edited, there is an ambiguity about whether to adjust width, height or both.
	// The parent passes in changeTheWidth, based on whether height or width was last edited.
	boolean maintainAspectRatio = true;
	
	public void areaChanged(boolean changeTheWidth)
	{
		fixedHeight.set(changeTheWidth);
		Unit areaUnit = getAreaUnits();
		double h, w, a;
		a = getAreaVal() * areaUnit.getPerMeter() * areaUnit.getPerMeter();		// square the area unit
		Unit widthUnits = getWidthUnits();
		Unit heightUnits = getHeightUnits();
		if (maintainAspectRatio)
		{
			double aspectRatio = getWidthInMeters() / getHeightInMeters();
			w = Math.sqrt(a * aspectRatio);
			h = a / w;
			setWidth(w / widthUnits.getPerMeter());
			setHeight(h /heightUnits.getPerMeter());
		}
		else if (fixedHeight.get())
		{
			h = getHeightVal() * heightUnits.getPerMeter();
			w = (a / h) / widthUnits.getPerMeter();
			setWidth(w);
		} 
		else
		{
			w = getWidthVal() * widthUnits.getPerMeter();
			h = (a / w) / heightUnits.getPerMeter();
			setHeight(h);
		}
	}

	// ------------------------------------------------------------------------------

}
