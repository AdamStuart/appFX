package image.turingPattern;

import java.awt.Dimension;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

//===============================================================================================
public class PixelGrid extends DataArray
{
    private ColorPool pool;		// the color table in an array
	//---------------------------------------------------------------
	public PixelGrid(Dimension sz)  { 		super(sz);  }
	public Color getColor(int i, int j)  {		return pool.fromValue(get(i,j));	}
//---------------------------------------------------------------
	boolean preload = true;

	void init(ColorPool inPool, String seedRef) 
	{
		pool = inPool;
		Image seed = null;  
		if (preload && seedRef != null)
		{
			try 
			{
				seed =  new Image(seedRef);
			}
			catch(Exception e)
			{
				System.err.println("loadImage failed" + "; msg:  " +  e.getMessage() + " " + seedRef);
			}
		}
		PixelReader rdr = null;
		if (seed != null)
			rdr = seed.getPixelReader();

		for (int i = 0; i < siz.width; i++)
			for (int j = 0; j < siz.height; j++) 
			{
				if (seed != null)
				{
					Color c = rdr.getColor(i, j);
					double val = colorToDouble(c);
					set(val, i, j);
				}
				else set(getSeedValue(i, j), i, j);
			}
	}
	
	private double colorToDouble(Color c) 
	{
		double r = c.getRed();
		double g = c.getGreen();
		double b = c.getBlue();
		return  ((r + g + b) / 3);		
	}
	
	double getSeedValue(int i, int j)	{		  return 2 * Math.random() - 1;		}
	//---------------------------------------------------------------
	void update(TuringPatternList patterns) 
	{
		double smallest = 100000, largest = -100000000;	// keep track of range for renormalizing
		if (patterns.getSize() <= 0) return;
		if (patterns.getNActive() <= 0) return;
		
		for (int i = 0; i < siz.width; i++)
			for (int j = 0; j < siz.height; j++) 
			{
				TuringPattern bestPattern = patterns.getBestPatternAt(i, j);    // best = least variation at this coordinate
				if (bestPattern == null) {
					System.out.println("best == null");
					continue;
				}
				
				double sign = (bestPattern.getActivator().get(i,j) > bestPattern.getInhibitor().get(i,j)) ? 1 : -1;
				double stepSize = bestPattern.getStepSize();
				add(sign * stepSize, i, j);
				
				double val = get(i,j);		// keep track of range for renormalizing
				largest = Math.max(largest, val);
				smallest = Math.min(smallest, val);
			}
		if (largest - smallest > 0.00000001)		// if they are equal, the normalization trashes the data with NaNs
			normalize(smallest, largest);
		
	}
}
