package image.turingPattern;

import java.awt.Dimension;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

//===============================================================================================
public class PixelGrid extends DataArray
{
//	DataArray tmp;
//    WritableImage wImage;
    PixelWriter pixelWriter;

	int counter = 1;
	ColorPool pool;		// the color table in an array
	int  res = 1;
	//---------------------------------------------------------------
	public PixelGrid(Dimension sz)  { 		super(sz);  }
//	public DataArray getGrid() 		{		return grid;	}
//	public DataArray getTemp() 		{		return tmp;	}
	public Color getColor(int i, int j)  {		return pool.fromValue(get(i,j));	}
//---------------------------------------------------------------
	boolean preload = true;

	void init(ColorPool inPool, String seedRef) 
	{
		pool = inPool;
//		wImage = new WritableImage(siz.width, siz.height);		
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
				for (int j = 0; j < siz.height; j++) {
					if (seed != null)
					{
						Color c = rdr.getColor(i, j);
						double val = colorToDouble(c);
						set(val, i, j);
					}
					else
						set(getSeedValue(i, j), i, j);
		}

	}
	
	private double colorToDouble(Color c) 
	{
		double r = c.getRed();
		double g = c.getGreen();
		double b = c.getBlue();
		return  ((r + g + b) / 3);		
	}
	
	double getSeedValue(int i, int j)
	{
//		return (i >= j && i % 7 == 0) || (j % 100 == i % 150) ? -1 : 1; 
		  return 2 * Math.random() - 1;
//		return 1-((Math.abs(i / (float) num) - 0.5 < 0.4 * Math.random()) && (Math.abs(j / (float) num) >  0.45 * Math.random()) ?  1: 0);

	}
	//---------------------------------------------------------------
	void update(TuringPatternList patterns) 
	{
//		if (1 < 3) return;
		double smallest = 100000, largest = -100000000;		// keep track of range for renormalizing
		if (patterns.getSize() <= 0) return;
		if (patterns.getNActive() <= 0) return;
		
		for (int i = 0; i < siz.width; i++)
			for (int j = 0; j < siz.height; j++) 
			{
				TuringPattern bestPattern = patterns.getBestPatternAt(i, j);    // best is least variation at this coordinate
				if (bestPattern == null) {
					System.out.println("best == null");
					continue;
				}
				
				double sign = (bestPattern.getActivator().get(i,j) > bestPattern.getInhibitor().get(i,j)) ? 1 : -1;
				double stepSize = bestPattern.getStepSize();
				add(sign * stepSize, i, j);
				
				// keep track of range for renormalizing
				double val = get(i,j);
				largest = Math.max(largest, val);
				smallest = Math.min(smallest, val);
			}
		if (largest - smallest > 0.00000001)		// if thay are equal, the normalization trashes the data with NaNs
		{
//			System.out.println();
//			System.out.println("Before " + grid.descriptor());
			normalize(smallest, largest);
//			System.out.println("normalize");
//			System.out.println("Grid.update exiting");
//			System.out.println(grid.dump());
		}
		
	}

	//---------------------------------------------------------------

}
