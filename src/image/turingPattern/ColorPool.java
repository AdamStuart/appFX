package image.turingPattern;

import java.util.ArrayList;

import javafx.scene.paint.Color;

public class ColorPool extends ArrayList<Color> {

//	final float SIZ = 50.f;
//	
//	String[] cols = new String[] {"0x0069f6", "0x1F8A70", "0x", "0x", "0x" };
	String[] p0 = new String[] { "0x000000", "0xFFFFFF" };
	String[] p1 = new String[] { "0x000000", "0x1F8A70", "0x8DDB39", "0x0069f6","0xFD7400", "0xFFFFFF" };
	String[] p2 = new String[] { "0x000000", "0x8D3B39", "0xFD7400", "0xFFFFFF" };
	String[] p3 = new String[] {"0x495Abf", "0xb0bc20", "0xa68bdf", "0x735439", "0xbf6836" };
	String[] p4 = new String[] {"0x10445A", "0xABDDF3", "0x528DA6", "0x5a3307", "0xA67F52" };
	String[] p5 = new String[] {"0x34B30A", "0xBD8915", "0xA65257", "0x5D54BD", "0x1FB386" };
	String[] p6 = new String[]  {"0x000000", "0x00FFFF", "0x0000FF", "0x80FFFF" };
	String[] p7 = new String[] {"0x000000", "0x00FF00", "0x0000FF", "0xFFFFFF" };
	String[] p8 = new String[] {"0x000000", "0xFF0000", "0x00FF00", "0x0000FF", "0xFFFFFF" };
	String[] p9 = new String[] {"0x96A10B", "0x6B6EeF", "0xD4B424", "0x7790D9	", "0x0B5AA1" };
	
	//-------------------------------------------------------------------------------
	public void nextColorSet()
	{
		index = (index + 1) % palettes.length;
		load();
		Object[] array = toArray();
		if (array != null && array.length > 0 && chooser != null)
			chooser.setColors(array);
	}
//-------------------------------------------------------------------------------
	private ColorChooser chooser;

	public void setChooser(ColorChooser inChooser )
	{
		chooser = inChooser;
	}
	
	//-------------------------------------------------------------------------------
	int nSteps = 64;
	int index = 0;
	String[][] palettes;
			
	public ColorPool()
	{
		super();
		palettes = new String[][]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 };
		index = 0;
		load();
	}
	
	public void load()
	{
		clear();
		String[] cols = palettes[index];
		nSteps = 256 / (cols.length-1);
		for (int i = 0; i < cols.length - 1; i++) 
		{
			Color start = Color.RED;
			Color end = Color.CYAN;
			try
			{
				start = Color.web(cols[i]);
				end = Color.web(cols[i + 1]);
			}
			catch (Exception e) {	System.err.println("bad color def");}
			
			double denom = 1.0 / nSteps;
			for (int j = 0; j < nSteps; j++) 
			{
				double f = (double) j * denom;
				Color c = new Color(
						start.getRed() + f * (end.getRed() - start.getRed()), 
						start.getGreen() + f * (end.getGreen() - start.getGreen()),
						start.getBlue() + f * (end.getBlue() - start.getBlue()),	1.0);

				add(c);
			}
		}
	}
	//-------------------------------------------------------------------------------
	public String[][] asStringPairs()
	{
		int nRows = size();
		String[][] rows = new String[nRows][];
		for (int i = 0; i<nRows; i++)
		{
			Color c = get(i);
			rows[i] = new String[]{ "", colorToString(c, false) };
		}
		return rows;
	}
	//-------------------------------------------------------------------------------
	
	public void dump() 
	{
		for (int i=0; i<size(); i++)
		{
			Color c = get(i);
			System.out.print(colorToString(c, false) + ", ") ;
		}
		System.out.println();
	}
	//-------------------------------------------------------------------------------
	
	private String twoCharHex(double i)
	{
		if (i >= 1.0) return "FF";
		if (i <= 0.0) return "00";
		String tmp = ('0' + Integer.toHexString((int)((256 * i)-1)));
		if (tmp.length() > 2) 	tmp = tmp.substring(1);
		return tmp.toUpperCase();
	}
	//-------------------------------------------------------------------------------
		
	private String colorToString(Color c, boolean alpha)
	{
		StringBuffer tmp = new StringBuffer(alpha ? "0x" : "#");			
		if (alpha) tmp.append(twoCharHex(c.getOpacity()));
		tmp.append(twoCharHex(c.getRed()));
		tmp.append(twoCharHex(c.getGreen()));
		tmp.append(twoCharHex(c.getBlue()));
		return tmp.toString(); 
	}
//-------------------------------------------------------------------------------
	// we want to be able to look up colors from any value, assuming we preset the range of values
	
	public Color fromValue(double val) 
	{
		int idx = (int)(normalize(val) * size());
		if (idx >= size()) idx--;
		return get(idx);
	}

	double min = 0; 
	double max = 100;
	double range = 100;
	
	public void setRange(double minVal, double maxVal)
	{
		min = minVal; max = maxVal; range = (maxVal - minVal);
	}
	
	public double normalize(double d)
	{
		if (d < min)  return 0;
		if (d > max)  return 1;
		return (d - min) / range;
	}
}
