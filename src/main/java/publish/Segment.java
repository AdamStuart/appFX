package publish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import model.CSVTableData;
import model.IntegerDataRow;
import util.StringUtil;

//--------------------------------------------------------------------------------

public class Segment
{
	String id;
	File csvFile;
	CSVTableData data;
	public static String[] colNames = new String[] { "Id", "Pos", "X", "Y", "Size", "CD3", "CD25", "CD4", "CD19", "CD38", "CD39",  "CD161", "CD27" };
	
	Segment(String inID, File inFile)
	{
		id = inID;
		csvFile = inFile;
		if (csvFile != null)		// read table
		{
			data = readBadCSVfile(csvFile);		// its actually tab-separated
			data.setColumnNames(Arrays.asList(colNames));
		}
	}
	
	private CSVTableData readBadCSVfile(File f)
	{
		CSVTableData tableData = new CSVTableData(f.getName());
		tableData.setColumnNames(Arrays.asList(colNames));
		readFile(f, tableData);
		tableData.calculateRanges();
		tableData.generateHistograms();
		tableData.calculateStats();
		System.out.println(tableData.getName() + "has row count: " + tableData.getCount());
		
		
		return tableData;
	}
	public String getName()			{	return id;	}
	public String toString()		{	return id + ": " + csvFile.getName();		}
	public CSVTableData getData()	{	return data;		}
	
	private void readFile(File f, CSVTableData tableData){
	
	int lineCt = 0;
	try
	{
		FileInputStream fis = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		
		line = br.readLine();		// first line is text labels, but not in columns
		line = br.readLine();
		String[] row = line.split("\t"); 
		int len = row.length;
		System.out.println( len + " columns");
		if (len != 13)
		{
			System.out.println( len + "CSV file with " + len + " columns was skipped.");
		}
		else while (line != null) {
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
