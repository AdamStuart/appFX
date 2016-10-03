package diagrams.draw.gpml;

import util.StringUtil;

public class GPMLPoint {

	private double x = 0;
	private double y = 0;
	private double relX = 0;
	private double relY = 0;
	private ArrowType head = ArrowType.none;
	private String graphRef;
	
	public double getX()				{ return x;	}
	public void setX(double d)			{ x = d;	}

	public double getY()				{ return y;	}
	public void setY(double s)			{ y = s;	}

	public String getGraphRef()			{ return graphRef;	}
	public void setGraphRef(String s)	{ graphRef = s;	}

	public ArrowType getArrowType()		{ return head;	}
	public void setArrowType(ArrowType s)	{ head = s;	}

	public double getRelX()				{ return relX;	}
	public void setRelX(double s)		{ relX = s;	}
	public double getRelY()				{ return relY;	}
	public void setRelY(double s)		{ relY = s;	}
	//-----------------------------------------------------------------------
	
	public GPMLPoint(double X, double Y) {
		x = X;
		y = Y;
	}

	public GPMLPoint(double X, double Y, String ref, ArrowType h) {
		x = X;
		y = Y;
		head = h;
		graphRef = ref;
	}
	
	public GPMLPoint(org.w3c.dom.Node node) {
		for (int i=0; i<node.getAttributes().getLength(); i++)
		{
			org.w3c.dom.Node child = node.getAttributes().item(i);
			String name = child.getNodeName();
			if ("X".equals(name))  x = StringUtil.toDouble(child.getNodeValue());
			else if ("Y".equals(name))  y = StringUtil.toDouble(child.getNodeValue());
			else if ("RelX".equals(name))  relX = StringUtil.toDouble(child.getNodeValue());
			else if ("RelY".equals(name))  relY = StringUtil.toDouble(child.getNodeValue());
			else if ("GraphRef".equals(name))  graphRef = child.getNodeValue();
			else if ("ArrowHead".equals(name))  head = ArrowType.lookup(child.getNodeValue());
		}
	}
	

	public String toString()
	{
		String firstPart = String.format("<Point X=\"%.2f\" Y=\"%.2f\" ", x, y);
		String secondPart = String.format("GraphRef=\"%s\" RelX=\"%.2f\" RelY=\"%.2f\" ArrowHead=\"%s\" />\n", getGraphRef(), getRelX(), getRelY(), getArrowType());
		return firstPart + secondPart;
	}

	//-----------------------------------------------------------------------
	public enum ArrowType
	{
		none,
		mimActivating,
		mimBinding, 
		mimIinhibition,
		circle,
		tbar,
		x,
		big,
		small;

		public static ArrowType fromString(String type)
		{
			if (type == null) return none;
			String t = type.toLowerCase();
			for (ArrowType a : values())
				if (a.name().toLowerCase().equals(t))	return a;
			return none;
		}
		public static ArrowType lookup(String nodeValue) {

			String noDash = nodeValue.replace("-", "");
			ArrowType a = fromString(noDash);
			if (a != ArrowType.none)  return a;
			// keep mapping here
			return none;
		}
	};
}
