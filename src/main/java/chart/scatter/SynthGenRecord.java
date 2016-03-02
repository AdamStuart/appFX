package chart.scatter;

import java.util.Formatter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SynthGenRecord
{
	private static final long serialVersionUID = 1754243336147L;

	IntegerProperty id = new SimpleIntegerProperty();
	IntegerProperty count = new SimpleIntegerProperty();

	DoubleProperty meanX = new SimpleDoubleProperty();
	DoubleProperty meanY = new SimpleDoubleProperty();
	DoubleProperty cvX = new SimpleDoubleProperty();
	DoubleProperty cvY = new SimpleDoubleProperty();

	public SynthGenRecord(int inId,int inCount, double x, double y, double cX, double cY)
	{
		id.set(inId);
		count.set(inCount);
		meanX.set(x);
		meanY.set(y);
		cvX.set(cX);
		cvY.set(cY);
	}

	public void toFormattedString(Formatter outFile)
	{
		outFile.format("%d %d %f %f %f %f\n", getId(), getCount(), getMeanX(), getMeanY(), getCvX(), getCvY());
	}


	// @formatter:off
	public int getId()				{		return id.getValue();	}
	public int getCount()			{		return count.getValue();	}
	public double getMeanX()		{		return meanX.getValue();				}
	public double getMeanY()		{		return meanY.getValue();				}
	public double getCvX()			{		return cvX.getValue();				}
	public double getCvY()			{		return cvY.getValue();				}

	public void setId(int i)				{		id.setValue(i);	}
	public void setCount(int i)				{		count.setValue(i);	}
	public void setMeanX(double d)			{		meanX.setValue(d);				}
	public void setMeanY(double d)			{		meanY.setValue(d);				}
	public void setCvX(double d)			{		cvX.setValue(d);				}
	public void setCvY(double d)			{		cvY.setValue(d);				}

	public IntegerProperty getIdProperty()			{		return id;	}
	public IntegerProperty getCountProperty()		{		return count;	}
	public DoubleProperty getMeanXProperty()		{		return meanX;				}
	public DoubleProperty getMeanYProperty()		{		return meanY;				}
	public DoubleProperty getCvXProperty()			{		return cvX;				}
	public DoubleProperty getCvYProperty()			{		return cvY;				}

}
