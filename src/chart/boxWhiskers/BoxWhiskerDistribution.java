package chart.boxWhiskers;

public class BoxWhiskerDistribution
{
	// we need five y values for each x.  
	// this object stores that array.
	
	// the order is min, 25th percentile, 50th percentile, 75th percentile, max  
	// so values can be assumed to increase
	
	double[] vals = new double[6];
	public BoxWhiskerDistribution(double[] d)
	{
		for (int i=1; i< 6; i++)
			vals[i]= d[i];
	}
	public double getVal(int i)	{		return vals[i];	}
	public double[] getVals()	{		return vals;	}

}
