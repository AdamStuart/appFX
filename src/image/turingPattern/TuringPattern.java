package image.turingPattern;

import java.awt.Dimension;
import java.awt.Point;

import javafx.beans.property.Property;

public class TuringPattern 

{
	private DataArray activator;
	private DataArray inhibitor;
	private DataArray variations;

	private Point activatorRadius, inhibitorRadius;

	private int variationSamplingRadius;
	private double stepsize;
	private Dimension sz;
	private boolean isActive; 
	private String id, name;
	private int internalCounter = 0;

	// ----------------------------------------------------------------------------
	TuringPattern(Dimension canvasSize) {
		sz = new Dimension(canvasSize);
		activator = new DataArray(sz);
		inhibitor = new DataArray(sz);
		variations = new DataArray(sz);
	}

	TuringPattern(Dimension dim, Point activateR, Point inhibitR, int vsr, double ss) {
		this(dim);
		activatorRadius = activateR;
		inhibitorRadius = inhibitR;
		variationSamplingRadius = vsr;
		stepsize = ss;
		isActive = true;
	}

	// ----------------------------------------------------------------------------

	TuringPattern(String s) {
		double version = StringTools.readDoubleAfter(s, "version");
		if (Math.abs(version - 0.1) > 0.001)
			System.err.println("version wrong");
		else
			setString(s);
	}

	// ----------------------------------------------------------------------------
	public String toString()
	{
		StringBuffer s = new StringBuffer("TuringPattern v0.1 ");
		s.append(StringTools.pointToString(activatorRadius));
		s.append( StringTools.pointToString(inhibitorRadius));
//		s.append(getVarSamplingRadius()).append(", ").append(stepsize);
		return s.toString();
	}

	// ----------------------------------------------------------------------------
	public void setString(String s)
	{
		id = StringTools.readStringAfter(s, "id");
		name = StringTools.readStringAfter(s, "name");
		activate(StringTools.readBoolAfter(s, "active"));
		sz = StringTools.readDimensionAfter(s, "sz");
		activatorRadius = StringTools.readPointAfter(s, "actvRad");
		inhibitorRadius = StringTools.readPointAfter(s, "inhibRad");
		setVarSamplingRadius(StringTools.readIntAfter(s, "varRadius"));
		setStepSize(StringTools.readDoubleAfter(s, "stepsize"));
		internalCounter = (StringTools.readIntAfter(s, "counter"));
	}
	// ----------------------------------------------------------------------------
	public String getString()
	{
		StringBuffer s = new StringBuffer("TuringPattern v0.1 ");
		s.append("id: " + id);
		s.append("name: " + name);
		s.append("active: " + Boolean.toString(isActive));
		s.append("sz: " + StringTools.dimToString(sz));
		s.append("actvRad: " + StringTools.pointToString(activatorRadius));
		s.append("inhibRad: " + StringTools.pointToString(activatorRadius));
		s.append("varRadius: " + getVarSamplingRadius());
		s.append("stepsize: " + getStepSize());
		s.append("counter: " + getCount());
		return s.toString();
	}
	
	// ----------------------------------------------------------------------------
	public int getCount()				{ 		return internalCounter;	}
	public String getId()				{ 		return id;	}
	public String getName()				{ 		return name;	}
	public void setName(String s)		{ 		name = s;	}
	public DataArray getVariations()	{		return variations;	}
	public DataArray getActivator()		{		return activator;	}
	public DataArray getInhibitor()		{		return inhibitor;	}

	public void setStepSize(double d)	{		stepsize = d;	}
	public double getStepSize()			{		return stepsize;	}

	public boolean isActive()			{		return isActive;	}
	public void activate(boolean b)		{		isActive = b;	}

	public void setVarSamplingRadius(int i)	{		variationSamplingRadius = i;	}
	public int getVarSamplingRadius()	{		return variationSamplingRadius;	}

	public double getActivatorX() {		return activatorRadius.getX();			}
	public double getActivatorY() {		return activatorRadius.getY();			}
	public void setActivator(Point pt) {		activatorRadius = pt;			}

	public double getInhibitorX() {		return inhibitorRadius.getX();			}
	public double getInhibitorY() {		return inhibitorRadius.getY();			}
	public void setInhibitor(Point pt) {		inhibitorRadius = pt;			}

	// ----------------------------------------------------------------------------
	public void set(int a, int b, int c, int d,  double e )
	{
		setActivator(new Point(a, b));
		setInhibitor(new Point(c, d));
		setStepSize(e);
	}
	// ----------------------------------------------------------------------------

	void step(PixelGrid inGrid)
	{
		diffuse(inGrid);
		combine(inGrid);
		internalCounter++;
	}

	// ----------------------------------------------------------------------------
	private void diffuse(PixelGrid inGrid)
	{
		blur(inGrid, activator, activatorRadius);
		blur(inGrid, inhibitor, inhibitorRadius);
	}

	private void blur(DataArray source, DataArray dest, Point radius)
	{
		DataArray tmp = new DataArray(new Dimension(sz.width, sz.height));
		horlineblur(source, tmp, radius.x);
		vrtlineblur(tmp, dest, radius.y);
	}

	private void horlineblur(DataArray source, DataArray dest, int radius)
	{
		for (int j = 0; j < sz.height; ++j)
		{
			double total = 0;
			for (int di = -radius; di <= radius; di++)
				total += (di >= 0 && di < source.getWidth()) ? source.get(di, j) : 0;
			total /= (radius + 1);
			dest.set(total, 0, j);			//

			for (int i = 1; i < sz.width; i++)
			{
				total -= (i - radius - 1 >= 0) ? source.get(i - radius - 1, j) : 0;
				total += (i + radius < sz.width) ? source.get(i + radius, j) : 0;
				dest.set(total, i, j);			// / (radius * 2 + 1)
			}
		}
	}

	private void vrtlineblur(DataArray source, DataArray dest, int radius)
	{
		for (int i = 0; i < sz.width; i++)
		{
			double total = 0;
			for (int dj = -radius; dj <= radius; dj++)
				total += (dj >= 0) ? source.get(i, dj) : 0;

			dest.set(total / (radius * 2 + 1), i, 0);
			for (int j = 1; j < sz.height; j++)
			{
				total -= (j - radius - 1 >= 0) ? source.get(i, j - radius - 1) : 0;
				total += (j + radius < sz.height) ? source.get(i, j + radius) : 0;
				dest.set(total / (radius * 2 + 1), i, j);
			}
		}
	}

	// ----------------------------------------------------------------------------
	void combine(PixelGrid theGrid)
	{
		DataArray tempArray = new DataArray(theGrid.getSize());			// this could be a static -- until multithreading 
//		System.out.println(inputOutput.dump());
		
		horlinecombine(tempArray, variationSamplingRadius);
		vrtlinecombine(variations, variationSamplingRadius);
		variations.add(tempArray);
	}

	private void horlinecombine(DataArray dest, int radius)
	{
		for (int j = 0; j < sz.height; ++j)
		{
			double total = 0;
			for (int di = -radius; di <= radius; di++)
				total += (di >= 0) ? Math.abs(activator.diff(inhibitor, di, j)) : 0;

			dest.set(total / (radius * 2 + 1), 0, j);
			for (int i = 1; i < sz.width; i++)
			{
				int x = i - radius - 1;
				double delta = Math.abs(activator.diff(inhibitor, x, j));
				total -= (i - radius - 1 >= 0) ? delta : 0;
				x = i + radius;
				total += (x < sz.width) ? Math.abs(activator.diff(inhibitor, x, j)) : 0;
				dest.set(total / (radius * 2 + 1), i, j);
			}
		}
	}

	private void vrtlinecombine(DataArray dest, int radius)
	{

		for (int i = 0; i < sz.width; ++i)
		{
			double total = 0;
			for (int dj = -radius; dj <= radius; dj++)
				total += (dj >= 0) ? Math.abs(activator.diff(inhibitor, i, dj)) : 0;
			dest.set(total / (radius + 1), i, 0);

			for (int j = 1; j < sz.height; j++)
			{
				int y = j - radius - 1;
				total -= (y >= 0) ? Math.abs(activator.diff(inhibitor, i, y)) : 0;
				y = j + radius;
				total += (j + radius < sz.height) ? Math.abs(activator.diff(inhibitor, i, y)) : 0;
				dest.set(total / (radius * 2 + 1), i, j);
			}
		}
	}

}
