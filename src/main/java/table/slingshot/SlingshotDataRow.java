package table.slingshot;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Formatter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SlingshotDataRow implements Serializable
{
	private static final long serialVersionUID = 75264722956336147L;

	IntegerProperty id = new SimpleIntegerProperty();
	StringProperty name = new SimpleStringProperty();
	LocalDate date = LocalDate.now();

	DoubleProperty medianX = new SimpleDoubleProperty();
	DoubleProperty medianY = new SimpleDoubleProperty();
	DoubleProperty cvX = new SimpleDoubleProperty();
	DoubleProperty cvY = new SimpleDoubleProperty();
	DoubleProperty targetX = new SimpleDoubleProperty();
	DoubleProperty targetY = new SimpleDoubleProperty();
	DoubleProperty varianceX = new SimpleDoubleProperty();
	DoubleProperty varianceY = new SimpleDoubleProperty();
	DoubleProperty metricX = new SimpleDoubleProperty();
	DoubleProperty metricY = new SimpleDoubleProperty();
	DoubleProperty variance = new SimpleDoubleProperty();



	public SlingshotDataRow(int inId, String inName, long inDate, double medX, double medY, double cX, double cY,
					double targX, double targY, double varX, double varY, double mX, double mY, double var)
	{
		id.set(inId);
		name.set(inName);
		date = LocalDate.ofEpochDay(inDate);
		medianX.set(medX);
		medianY.set(medY);
		cvX.set(cX);
		cvY.set(cY);
		targetX.set(targX);
		targetY.set(targY);
		varianceX.set(varX);
		varianceY.set(varY);
		metricX.set(mX);
		metricY.set(mY);
		variance.set(var);
	}

	public SlingshotDataRow(int inId, String inName, long inDate, double[] stats11)
	{
		id.set(inId);
		name.set(inName);
		try
		{
			date = LocalDate.ofEpochDay(inDate);
		}
		catch (DateTimeException e) { date = LocalDate.now();	}
		

		medianX.set(stats11[0]);
		medianY.set(stats11[1]);
		cvX.set(stats11[2]);
		cvY.set(stats11[3]);
		varianceX.set(stats11[4]);
		varianceY.set(stats11[5]);
		metricX.set(stats11[6]);
		metricY.set(stats11[7]);
		variance.set(stats11[8]);
		targetX.set(stats11[9]);
		targetY.set(stats11[10]);
	}

	public void toFormattedString(Formatter outFile)
	{
		String name = noSpaces(getName());
		outFile.format("%d %d %s %f %f %f %f %f %f %f %f %f %f %f\n", getId(), getDate(), name, getMedX(), getMedY(), getCvX(), getCvY(), 
						getVarX(), getVarY(), getMetricX(), getMetricY(), getVar(), getTargX(), getTargY());
	}

//	public static ObservableList<SlingshotDataRow> getDummySlingshotRecords()
//	{
//		ObservableList<SlingshotDataRow> vals = FXCollections.observableArrayList();
//		for (int i = 1; i < 5; i++)
//		{
//			SlingshotDataRow p = new SlingshotDataRow(i, "First Test Record", LocalDate.now()
//							.toEpochDay(), 1 + Math.random(), 2 + Math.random(), 3 + Math.random(),
//							4 + Math.random(), 5 + Math.random(), 6 + Math.random(), 0 , 0,
//							7 + Math.random());
//			vals.add(p);
//		}
//		return vals;
//	}

	// @formatter:off
	public String getName()		{		return name.getValue();	}
	public long getDate()		{		return date.toEpochDay();	}
	public int getId()			{		return id.getValue();	}
	public double getVar()		{		return variance.getValue();				}
	public double getVarX()		{		return varianceX.getValue();				}
	public double getVarY()		{		return varianceY.getValue();				}
	public double getMetricX()	{		return metricX.getValue();				}
	public double getMetricY()	{		return metricY.getValue();				}
	public double getMedX()		{		return medianX.getValue();				}
	public double getMedY()		{		return medianY.getValue();				}
	public double getCvX()		{		return cvX.getValue();				}
	public double getCvY()		{		return cvY.getValue();				}
	public double getTargX()	{		return targetX.getValue();				}
	public double getTargY()	{		return targetY.getValue();				}

	public Object getNameProperty()	{		return name;	}
	public static String noSpaces(String in)		{		return in.replaceAll(" ", "%20");	}
	public static String restoreSpaces(String in)	{		return in.replaceAll("%20", " ");	}

}
