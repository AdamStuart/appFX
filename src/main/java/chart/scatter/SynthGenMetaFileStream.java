package chart.scatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SynthGenMetaFileStream
{
	SynthGenController controller;

	public SynthGenMetaFileStream(SynthGenController ctlr)
	{
		controller = ctlr;
	}
	public SynthGenMetaFileStream(Object ctlr)		// a hack for Knowble.ScatterChartController
	{
	}

	// -----------------------------------------------------------------
	static public String EXT = ".synthdef";
	
	public void writeDefFile(String filePath)
	{
		Formatter outFile = null;
		try
		{
			if (filePath.endsWith(EXT))
				filePath = filePath + EXT;
			outFile = new Formatter(filePath);
			for (SynthGenRecord row : controller.getSynthGenTable().getItems())
				row.toFormattedString(outFile);

		} catch (FileNotFoundException fileNotFoundException)
		{
			System.err.println("Error creating file.");
		} catch (FormatterClosedException formatterClosedException)
		{
			System.err.println("Error writing to file.");
		} finally
		{
			if (outFile != null)
				outFile.close();
		}
	}

	// -----------------------------------------------------------------
	public ObservableList<SynthGenRecord> readDefFile(String file)
	{
		ObservableList<SynthGenRecord> list = FXCollections.observableArrayList();
		Scanner inFile = null;
		try
		{
			File f = new File(file);
			if (f.isFile())
			{
				inFile = new Scanner(f);
				int id, ct;
				double meanX, meanY, cvX, cvY;
	
				while (inFile.hasNext())
				{
					id = inFile.nextInt();
					ct = inFile.nextInt();
					meanX = inFile.nextDouble();
					meanY = inFile.nextDouble();
					cvX = inFile.nextDouble();
					cvY = inFile.nextDouble();
	
					SynthGenRecord p = new SynthGenRecord(id, ct, meanX, meanY, cvX, cvY);
					list.add(p);
				}
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
