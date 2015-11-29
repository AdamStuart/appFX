package publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.CSVTableData;

//--------------------------------------------------------------------------------
public class Segment
{
	String id;
	File csvFile;
	CSVTableData data;
	
	Segment(String inID, File inFile)
	{
		id = inID;
		csvFile = inFile;
		if (csvFile != null)		// read table
			data = readBadCSVfile(csvFile);		// its actually tab-separated
	}
	
	private CSVTableData readBadCSVfile(File f)
	{
		CSVTableData tableData = new CSVTableData();
		try
		{
			FileInputStream fis = new FileInputStream(f);
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			List<ObservableList<String>> idata = tableData.getData();
			String line = null;
			
			line = br.readLine();		// first line is text labels, but not in columns
			line = br.readLine();
			String[] row = line.split("\t"); 
			int len = row.length;
			for (int i=0; i<len; i++)
				tableData.getData().add(FXCollections.observableArrayList());

			while (line != null) {
//				System.out.println(line);
				row = line.split("\t");  
				if (row.length != len)	throw new IllegalArgumentException();		// there must be the same number of tabs in every row
				for (int i = 0; i< row.length; i++)
				{
					idata.get(i).add(row[i]);
					System.out.println(row[i]);
				}
				line = br.readLine();
			}
		 
			br.close();
		}
		catch (Exception e)	{ e.printStackTrace();	}
		
		return tableData;
	}
	public String toString()		{	return id + ": " + csvFile.getName();		}
	public CSVTableData getData()	{	return data;		}
}
//--------------------------------------------------------------------------------
