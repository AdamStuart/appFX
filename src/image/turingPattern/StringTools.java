package image.turingPattern;

import java.awt.Dimension;
import java.awt.Point;

public class StringTools {

	
	static public double readDoubleAfter(String s, String prompt)
	{
		try
		{
			int idx = s.indexOf(prompt);
			if (idx < 0)	return Double.NaN;
			int start = idx + prompt.length() + 2;
			int end =  s.indexOf(' ', start);
			double d = Double.parseDouble(s.substring(start, end));
			return d;
		}
		catch (Exception e) 	
		{  System.err.println("parsing error"); }
		return Double.NaN;
	}
	
	public static String readStringAfter(String s, String prompt)
	{
		try
		{
			int idx = s.indexOf(prompt);
			if (idx < 0)	return null;
			int start = idx + prompt.length() + 2;
			int end =  s.indexOf(' ', start);
			return s.substring(start, end).trim();
		}
		catch (Exception e) 	{  System.err.println("parsing error"); }
		return null;
		
	}
	public static int readIntAfter(String s, String prompt)
	{
		try
		{
			double d = Integer.parseInt(readStringAfter(s, prompt));
		}
		catch (Exception e) 	
		{  System.err.println("parsing error"); }
		return -1;
	}
	
	public static boolean readBoolAfter(String s, String prompt)
	{
		int idx = s.indexOf(prompt);
		if (idx < 0)	return false;
		int start = idx + prompt.length() + 2;
		int end =  s.indexOf(' ', start);
		return "true".equals(s.substring(start, end));
	}
	
	public static Point readPointAfter(String s, String prompt)
	{
		int idx = s.indexOf(prompt);
		if (idx < 0) return new Point(0,0);
		int start = idx + prompt.length() + 3;
		int end =  s.indexOf(')'-1, start);
		String contents = s.substring(start, end);
		int commaLoc = contents.indexOf(",");
		int x = Integer.parseInt(contents.substring(0,commaLoc).trim());
		int y = Integer.parseInt(contents.substring(commaLoc+1).trim());
		return new Point(x, y);
	}
	public static Dimension readDimensionAfter(String s, String prompt)
	{
		Point pt = readPointAfter(s, prompt);
		if (pt == null) return null;
		return new Dimension(pt.x, pt.y);
	}
	
	
	static public String pointToString(Point pt) {
		return "( " + pt.x + ", " + pt.y + " )";
	}
	static public String dimToString(Dimension d) {
		return "( " + d.width + ", " + d.height + " )";
	}

}
