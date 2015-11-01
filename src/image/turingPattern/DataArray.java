package image.turingPattern;

import java.awt.Dimension;
import java.util.Formatter;
import java.util.Locale;

public class DataArray
{
	private double[][] d;
	protected Dimension siz;
	public int getWidth() { return siz.width;	}
	public int getHeight() { return siz.height;	}
	public Dimension getSize() {		return siz;	}

	public DataArray(Dimension sz) {
		d = new double[sz.width][sz.height];
		siz = sz;
		for (int i =0; i< siz.width; i++)
			for (int j =0; j< siz.height; j++)
				d[i][j] = 0;
	}

	public double get(int i, int j)
	{
		if (i < 0 || j < 0)							return Double.NaN;
		if (i >= siz.width || j >= siz.height)		return Double.NaN;
		return d[i][j];
	}

	public void set(double val, int i, int j)
	{
		if (i < 0 || j < 0)							return;
		if (i >= siz.width || j >= siz.height)		return;
		d[i][j] = val;
	}

	public void add(double val, int i, int j)
	{
		if (i < 0 || j < 0)							return;
		if (i >= siz.width || j >= siz.height)		return;
		d[i][j] += val;
	}

	public void add(DataArray other)
	{
		if (!siz.equals(other.siz))					return;
		for (int i = 0; i < siz.width; i++)
			for (int j = 0; j < siz.height; j++)
				d[i][j] += other.get(i, j);
	}

	public void add(DataArray vals, int i, int j)
	{
		if (i < 0 || j < 0)							return;
		if (i >= siz.width || j >= siz.height)		return;
		d[i][j] += vals.get(i, j);
	}

	public double diff(DataArray other, int i, int j)
	{
		if (i < 0 || j < 0)							return Double.NaN;
		if (i >= siz.width || j >= siz.height)		return Double.NaN;
		return d[i][j] - other.get(i, j);
	}

	public double sum()	{		return sum(0, 0, siz.width, siz.height);	}
	
	public double sum(int x, int y, int w, int h)
	{
		double total = 0;
		for (int i = x; i < x + siz.width; i++)
			for (int j = y; j < y + siz.height; j++)
				total += get(i, j);
		return total;
	}

	public String descriptor()	{		return descriptor(0, 0, siz.width, siz.height);	}

	public String descriptor(int x, int y, int w, int h)
	{
		double total = 0, var = 0, min = Double.MAX_VALUE, max =Double.MIN_VALUE;
		for (int i = x; i < x + siz.width; i++)
			for (int j = y; j < y + siz.height; j++)
			{
				double d = get(i, j);
				total += d;
				var += d * d;
				if (d > max)	{ max = d;	}
				if (d < min)	{ min = d;	}
			}
		return "Total: " + total + ", var:" + var + ", [" + min + ", " + max + "]";
	}

	public double variance()
	{
		return sum(0, 0, siz.width, siz.height);
	}

	public double variance(int x, int y, int w, int h)
	{
		double total = 0;
		double mean = sum(x,y,w,h) / (w * h);
		for (int i = x; i < x + siz.width; i++)
			for (int j = y; j < y + siz.height; j++)
			{
				double d = get(i, j) - mean;		// mean is assumed 0
				total += d * d;
			}
		return total / (w * h);
	}

	public void normalize(double min, double max)
	{
		double range = (max - min); // range /= 2 ?
		if (range <= 0)
		{
			System.err.println("div 0");
			return;
		}
		for (int i = 0; i < siz.width; i++)
			for (int j = 0; j < siz.height; j++)
			{
				double a = d[i][j];
				a -= min;
				a = 2 * a / range - 1;			// 2 is the range width, -1 is the offset
				d[i][j] = a;
			}
	}

	public String toString()	{	return d[0][0] + ", " + d[0][1] + ", " + d[1][0] + ", " + d[1][1];	}
	// ---------------------------------------------------------------
	public String dump()
	{
		int x = Math.min(siz.width, 20);  
		int y = Math.min(siz.height, 20);  
		StringBuffer b = new StringBuffer();

		for (int i = 0; i < x; i++)
		{
			for (int j = 0; j < y; j++)
			{
				String s = String.format("%.2f     ", d[i][j]);
				b.append(s);
			}
			b.append("\n");
		}
		return b.toString();
	}
}