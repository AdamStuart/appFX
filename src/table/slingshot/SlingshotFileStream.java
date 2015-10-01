package table.slingshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SlingshotFileStream
{
	private SlingshotController controller;
	
	public SlingshotFileStream(SlingshotController ctrlr)
	{
		controller = ctrlr;
	}
	
	public void writeToTextFile(ObservableList<SlingshotDataRow> list, String filePath)
	{
		Formatter outFile = null;
		try
		{
			outFile = new Formatter(filePath);
			writeFirstLine(outFile);
			for (SlingshotDataRow row : list)
				row.toFormattedString(outFile);

		} catch (FileNotFoundException fileNotFoundException)		{			System.err.println("Error creating file.");		} 
		catch (FormatterClosedException formatterClosedException)	{			System.err.println("Error writing to file.");	} 
		finally
		{
			if (outFile != null)
				outFile.close();
		}
	}
	//-----------------------------------------------------------------
//	double targetX, targetY;
//	int index;
	 // targetX=939.49; targetY=494.8 ; index=30;
	private void parseFirstLine(String s)
	{
		int idx = s.indexOf('=') + 1;
		int endx = s.indexOf(';', idx);
		float targetX = Float.parseFloat(s.substring(idx, endx));
		idx = s.indexOf('=', endx) + 1;
		endx = s.indexOf(';', idx);
		float targetY =  Float.parseFloat(s.substring(idx, endx));
		idx = s.indexOf('=', endx) + 1;
		endx = s.indexOf(';', idx);
		int index = Integer.parseInt(s.substring(idx, endx));
		controller.setParameters(targetX, targetY, index);
	}
	
	private void writeFirstLine(Formatter outFile)
	{
//		controller.readTarget();
		outFile.format("targetX=%f; targetY=%f; index=%d;\n", controller.getTargetX(), controller.getTargetY(), controller.getIndex());
	}
	//-----------------------------------------------------------------

	public ObservableList<SlingshotDataRow> readFromTextFile(String file)
	{
		ObservableList<SlingshotDataRow> list = FXCollections.observableArrayList();
		Scanner inFile = null;
		try
		{
			File f = new File(file);
			 if (!f.exists())
			 {
				System.err.println("Error opening file.");
			 }
			inFile = new Scanner(new File(file));
			String firstLine = inFile.nextLine();   // targetX=939.49; targetY=494.8 ; index=30;
			parseFirstLine(firstLine);
			while (inFile.hasNext())
			{
				int inId = inFile.nextInt();
				long longDate = inFile.nextLong();
				String s = SlingshotDataRow.restoreSpaces(inFile.next());

				double stats11[] = new double[11];
				for (int i=0; i< 11; i++)
					stats11[i] = inFile.nextDouble();

				SlingshotDataRow p = new SlingshotDataRow(inId, s, longDate, stats11);
				list.add(p);
			}
		} catch (FileNotFoundException e)
		{
			System.err.println("Error opening file.");
			e.printStackTrace();
		} catch (NoSuchElementException d)
		{
			System.err.println("Error in file record structure");
			d.printStackTrace();
		} catch (IllegalStateException d)
		{
			System.err.println("Error reading from file.");
			d.printStackTrace();
		} finally
		{
			if (inFile != null)
				inFile.close();
		}
		return list;
	}
}
