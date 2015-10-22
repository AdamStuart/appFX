package chart.scatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
//-----------------------------------------------------------------
import model.XYDataRow;
import model.XYModel;
import services.RandomSequenceGenerator;

public class SynthFileStream
{
	Formatter outFile = null;
	public Formatter getFormatter()	{ return outFile;	}
	int eventNumBase = 0;

	public SynthFileStream()
	{
	}
	
	public void setOutFilepath(String path)
	{
		try
		{
			outFile = new Formatter(new File(path));
		} 
		catch (FileNotFoundException fileNotFoundException)		{		System.err.println("Error creating file.");		} 
		catch (FormatterClosedException formatterClosedException){		System.err.println("Error writing to file.");		}
	}
	//-----------------------------------------------------------------
	
	public void generate(ObservableList<SynthGenRecord> rows)
	{
		for (SynthGenRecord r : rows)
			writePopulation(r.getMeanX(), r.getMeanY(), r.getCvX(), r.getCvY(), r.getCount(), r.getId());
	}


	public void writePopulation(SynthGenRecord r)
	{
		writePopulation(r.getMeanX(), r.getMeanY(), r.getCvX(), r.getCvY(), r.getCount(), r.getId());
	}
	
	public void writePopulation(double xTarget, double yTarget, double xStdev, double yStdev, int nPts, int dataset)
	{
		try
		{
			for (int i=0; i<nPts; i++)
			{
				Point2D randPt = RandomSequenceGenerator.randomNormal(xTarget, xStdev, yTarget, yStdev);
				outFile.format("%f\t%f\t%d\t%d\n", randPt.getX(), randPt.getY(), eventNumBase + i, dataset);
			}
		}
		finally
		{
			eventNumBase += nPts;
		}
	}
	
	//-----------------------------------------------------------------
	public void writePoint(double x, double y, int id, int dataset)
	{
		try
		{
				outFile.format("%f\t%f\t%d\t%d\n", x, y, id, dataset);
		}
		catch (Exception e) { e.printStackTrace();}
	}

	public void writePoint(double x, double y)
	{
		 writePoint(x, y,0,0);
	}
	
	//-----------------------------------------------------------------
//	public void make5events()
//	{
//		for (int i=1; i<= 5; i+= 1)
//			writePoint(i,1);
//	}
//	
//	public void make10events()
//	{
//		for (int i=10; i<= 50; i+= 10)
//		{
//			writePoint(i,10);
//			writePoint(i,10);
//		}
//	}
//	
//	public void makeDiagEvents()
//	{
//		for (int i=10; i<= 50; i+= 10)
//			writePoint(i,i);
//	}
//	
//	
	public void close()
	{
		if (outFile != null)
			outFile.close();
		
	}
	//-----------------------------------------------------------------
	// Box-Mueller method to generate values in a normal distribution
	// http://en.wikipedia.org/wiki/Normal_distribution#Generating_values_from_normal_distribution

	public static Point2D randomNormal(SynthGenRecord rec)
	{
		return RandomSequenceGenerator.randomNormal(rec.getMeanX(), rec.getCvX(), rec.getMeanY(), rec.getCvY());
	}

	//-----------------------------------------------------------------
	public XYModel readFromTextFile(File file, double targetX, double targetY)
	{
		XYModel rows = new XYModel(targetX, targetY);
		Scanner inFile = null;
		try
		{
			inFile = new Scanner(file);
//			int fileid = inFile.nextInt();
//			double targetX = inFile.nextDouble();
//			double targetY = inFile.nextDouble();
			
			while (inFile.hasNext())
			{
				double x = inFile.nextDouble();
				double y = inFile.nextDouble();
				int id = inFile.nextInt();
				int dataset = inFile.nextInt();

				XYDataRow p = new XYDataRow(x, y, id, dataset);
				rows.add(p);
			}
		} catch (FileNotFoundException e)
		{
			System.err.println("Error opening file.");
			e.printStackTrace();
		} catch (NoSuchElementException e)
		{
			System.err.println("Error in file record structure");
			e.printStackTrace();
		} catch (IllegalStateException e)
		{
			System.err.println("Error reading from file.");
			e.printStackTrace();
		} finally
		{
			if (inFile != null)
				inFile.close();
		}
		return rows;
	}

	
}
