package publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import model.dao.CSVTableData;
import model.dao.IntegerDataRow;
import util.StringUtil;

//--------------------------------------------------------------------------------

public class Segment
{
	String id;
	File csvFile;
	CSVTableData data;
//	public static String[] colNames = new String[] { "Id", "Pos", "X", "Y", "Size", "CD3", "CD25", "CD4", "CD19", "CD38", "CD39",  "CD161", "CD27" };
	
	public Segment(String inID, File inFile)
	{
		id = inID;
		csvFile = inFile;
		if (csvFile != null)		// read table
		{
			data = readBadCSVfile(csvFile);		// its actually tab-separated
//			if (data != null)
//				data.setColumnNames(Arrays.asList(colNames));
		}
	}
	
	//--------------------------------------------------------------------------------
	public String getName()			{	return id;	}
	public String toString()		{	return id + ": " + csvFile.getName();		}
	public CSVTableData getData()	{	return data;		}

	//--------------------------------------------------------------------------------
	private CSVTableData readBadCSVfile(File f)
	{
		CSVTableData tableData = new CSVTableData(f.getName());
//		tableData.setColumnNames(Arrays.asList(colNames));		now is read from first line in readFile.68
		readFile(f, tableData);
		if (tableData.getData().size() == 0) 
			return null;
	
		tableData.calculateRanges();
		tableData.generateHistograms();
		tableData.calculateStats();
		System.out.println(tableData.getName() + " has row count: " + tableData.getCount());
		return tableData;
	}
	
	private void readFile(File f, CSVTableData tableData)
	{
	
		int lineCt = 0;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			line = br.readLine();		// first line is text labels
			String[] row = line.split("\t"); 
			tableData.setColumnNames(Arrays.asList(row));
			
			line = br.readLine();
			row = line.split("\t"); 
			int len = row.length;
			System.out.println( len + " columns");
//			if (len != 13)
//			{
//				tableData.getData().clear();
//				System.out.println( "CSV file with " + len + " columns was skipped: " + f.getName());
//			}
//			else 
			while (line != null) 
			{
				row = line.split("\t");  
				if (row.length != len)	throw new IllegalArgumentException();		// there must be the same number of tabs in every row
				IntegerDataRow dataRow = new IntegerDataRow(row.length); 
				for (int i = 0; i< row.length; i++)
					dataRow.set(i, StringUtil.toInteger(row[i]));				// (wrongly) swallows exceptions if non integers are here
				tableData.getData().add(dataRow);
				line = br.readLine();
				lineCt++;
			}
		 
			br.close();
		}
		catch (Exception e)	{ e.printStackTrace();	}
		System.out.println( lineCt + " lines");
	}
}
//--------------------------------------------------------------------------------
