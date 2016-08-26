package table.slingshot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import javafx.scene.input.Dragboard;

public class FCSFileReader 
{
	private byte[] content;		// we're going to read the whole file into a byte[]
	private HashMap<String,String> textSection;
	private int textStart, textEnd, bodyStart, bodyEnd;
	private float[] xData, yData;
	String name;
	long date;
	int id;

	public int getId()			{		return id;	}
	public String getName()		{		return name;	}
	public long getDate()		{		return date;	}
	public float[] getXData()	{		return xData;	}
	public float[] getYData()	{		return yData;	}
	public String getValue(String attr) { return textSection.get(attr);		}

	static public boolean hasFCSFiles(Dragboard db)	{		return db.getFiles().stream().filter(f -> isFCS(f)).count() > 0;	}
	static public boolean isFCS(File f)				{		return f.getName().toUpperCase().trim().endsWith(".FCS");	}

	//-----------------------------------------------------------------
	
	public FCSFileReader(File file) throws FileNotFoundException
	{
		try
		{
			Path path = Paths.get(file.getAbsolutePath());
			String filename = file.getName();
			name = filename.substring(9);			// starting after SBS00000.
			date = file.lastModified();			// TODO -- convert to LocalDate.toEpochDate()
			try
			{
				assert(filename.toUpperCase().endsWith(".FCS"));
				assert("SBS".equals(file.getName().substring(0,3)));
				id = Integer.parseInt(file.getName().substring(3,8));
			}
			catch (Exception e) { id = 0; }
			content = Files.readAllBytes(path);
			readHeader();
			readText(); 
			readBody();
		} catch (IOException e)		{	e.printStackTrace();	}
	}
	//-----------------------------------------------------------------	
	private void readHeader()
	{
		String header = new String(content,0,50);
		if ("FCS3.".equals(header.substring(0,5)))
		try {
			String vals = header.substring(10);
			String tmp = vals.substring(0,8).trim();	textStart = Integer.parseInt(tmp);
			tmp = vals.substring(8,16).trim();			textEnd = Integer.parseInt(tmp);
			tmp = vals.substring(16, 24).trim();		bodyStart = Integer.parseInt(tmp);
			tmp = vals.substring(24, 32).trim();		bodyEnd = Integer.parseInt(tmp);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	int nEvents;
	int parms;
	int nBytes;
	int nRows;
	int bytesPerValue;
	boolean readFloats, readInts;
	//-----------------------------------------------------------------
	private void readText()
	{
		textSection = parseAttributes(new String(content,textStart,textEnd));
//		for (String k : textSection.keySet())
//			System.out.println(k + ": " + textSection.get(k));

		nBytes = bodyEnd - bodyStart +1 ;
		
		try
		{
			nEvents = Integer.parseInt(textSection.get("$TOT"));
			parms = Integer.parseInt(textSection.get("$PAR"));

		} catch (Exception e)
		{
			assert(e == null);
		}
		bytesPerValue = 4;			// Either Float.SIZE or Integer.SIZE
		nRows = bytesPerValue / parms;
		System.out.println("" +  nEvents * parms * bytesPerValue);
		if (nBytes != nEvents * parms * bytesPerValue)
			System.out.println("size calculation failed: " + nBytes + " != " + nEvents + " * 4 * " + parms + " (" + nEvents * 4 * parms + ")") ;
		String type = textSection.get("$DATATYPE");
		readFloats = "F".equals(type);
		readInts = "I".equals(type);
		assert(readFloats != readInts);
	}
	//-----------------------------------------------------------------	
	
	private void readBody()
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(content, 0, content.length);		// bodyStart still has to be used as an offset, given the way we access the bytebuffer
		
		int nBytes = bodyEnd-bodyStart+1;			
		int bytesPerRow = bytesPerValue * parms;
		int nValues = nBytes / bytesPerValue;
		int nRows = nBytes / bytesPerRow;
//		assert(nRows == nEvents);
		xData =  new float[nRows];
		yData =  new float[nRows];

		for (int i = 0; i < nValues; i++) 
		{
			if (readFloats)
			{
				int bytesPerFloat = Float.SIZE / Byte.SIZE;
				int idx = bodyStart + i * bytesPerFloat;
				float nextF = byteBuffer.getFloat(idx);
				if (i % parms == 0)								// we only care about the first two parameters, the rest are ignored
					xData[i / parms] = nextF;
				else if (i % parms == 1)
					yData[i / parms] = nextF;

			} else
			{
				int bytesPerInt = Integer.SIZE / Byte.SIZE;
				int idx = bodyStart + i * bytesPerInt;
				if (idx > byteBuffer.limit()-4)
					System.out.println("error");
				int nextI = byteBuffer.getInt(idx);
				if (i % parms == 0)
					xData[i / parms] = nextI;
				else if (i % parms == 1)
					yData[i / parms] = nextI;

			}
		}
	}
	//-----------------------------------------------------------------

	public static byte [] floatToByteArray (float value)	{  	     return ByteBuffer.allocate(4).putFloat(value).array();	}

	public static String bytesToHex(byte[] in) {
	    final StringBuilder builder = new StringBuilder();
	    for(byte b : in) 
	        builder.append(String.format("%02x", b));
	    return builder.toString();
	}	
	
	//-----------------------------------------------------------------
	public HashMap<String, String> parseAttributes(String s)
	{
		String delim = s.trim().substring(0, 1);
		StringTokenizer tokenizer = new StringTokenizer(s, delim);
		HashMap<String, String> map = new HashMap<String, String>();
		while (tokenizer.hasMoreTokens())
			map.put(tokenizer.nextToken(), tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "");
		return map;
	}
	//-----------------------------------------------------------------

	public double[] getStats11()
	{
		return new double[] { medianX, medianY, cvX, cvY, varX, varY, metricX, metricY, var, targetX, targetY };		
	}
	
	public void setTarget(double x, double y)		{ targetX = x;	 targetY = y;	}
	double targetX, targetY;
	double medianX, medianY;
	double cvX, cvY;
	double varX, varY;
	double metricX, metricY;
	double internalXVar, externalXVar = 0;
	double internalYVar, externalYVar = 0;
	double var = 0;
	
	//-----------------------------------------------------------------
	public void calculate()
	{	
		double sumVar = 0;
		 internalXVar = externalXVar =  internalYVar = externalYVar = 0;
		 System.out.println("target: " + targetX + ", " + targetY);
		
		float[] xcopy = xData.clone();  		// sort the arrays to get the medians
		float[] ycopy = yData.clone();  
		Arrays.sort(xcopy);
		Arrays.sort(ycopy);
		int idx = nEvents / 2;
		medianX = nEvents % 2 == 0 ?( (xcopy[idx] + xcopy[idx+1]) / 2.f ): xcopy[nEvents / 2];
		medianY = nEvents % 2 == 0 ?( (ycopy[idx] + ycopy[idx+1]) / 2.f ): ycopy[nEvents / 2];
		
		int startIdx = (int) (nEvents * .1666);			// ignore top and bottom sixth
		int endIdx = (int) (nEvents * .8333);
		
		for (int i=startIdx; i<endIdx; i++)
		{
			float x = xcopy[i];
			double dx2 = ((medianX - x) * (medianX - x));
			internalXVar += dx2;
			double dxt2 =((targetX - x) * (targetX - x));
			externalXVar += dxt2;
			
			float y = ycopy[i];
			double dy2 = ((medianY - y) * (medianY - y));
			internalYVar += dy2;
			double dt2 = ((targetY - y) * (targetY - y));
			externalYVar += dt2;
			
			sumVar += Math.sqrt(dxt2 + dt2);
		}
		
		double count = nEvents-1;
		
		varX = (float) (Math.sqrt(internalXVar)  / count);
		cvX = 100.* varX / medianX;
		metricX = (float) (100.* Math.sqrt(externalXVar)  / (count * medianX));
		
		varY = (float) (Math.sqrt(internalYVar)  / count);
		cvY = 100.* varY / medianY;
		metricY = (float) (100.* Math.sqrt(externalYVar)  / (count * medianY));
		
		double delta = ((targetX - medianX) * (targetX - medianX)) + ((targetY - medianY) * (targetY - medianY));
		double root = (float) Math.sqrt(delta);
		var = sumVar / (count * root);
	}

}

//-----------------------------------------------------------------

//String test1 = "/name/adam/height/tall/gender/male/tired/true";
//String test2 = "#color#red#shape#round";
//		private void testAttributeParsing()
//	{
//		HashMap<String, String> map = parseAttributes(test1);
//		map.putAll(parseAttributes(test2));
//		System.out.println(streamAttributes(map, '?'));
//	}
//public String streamAttributes( HashMap<String, String> map, char delim)
//{
//	StringBuilder buffer = new StringBuilder();
//	Set<String> keys = map.keySet();
//	for (String key : keys)
//		buffer.append(delim).append(key).append(delim).append(map.get(key));
//	return buffer.toString();

