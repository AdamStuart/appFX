package table.binder;

public class ValueUnitRecord
{
	double val;
	Unit unit;
	
	public ValueUnitRecord(Double value, Unit unit2)
	{
		setVal(value);
		setUnit(unit2);
	}
	public void setVal(double d)	{ val = d;	}
	public void setUnit(Unit u)	{ unit = u;	}
	public double getVal()			{ return val;	}
	public Unit getUnit()			{ return unit;	}
}
