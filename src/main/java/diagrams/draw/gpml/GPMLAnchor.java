package diagrams.draw.gpml;

import util.StringUtil;

public class GPMLAnchor {

	private double position;
	private String shape = "None";
	
	public double getPosition()	{ return position;	}
	public void setPosition(double p)	{  position = p;	}

	public String getShape()		{ return shape;	}
	public void setShape(String p)	{  shape = p;	}
	
	public GPMLAnchor(org.w3c.dom.Node node)
	{
		for (int i=0; i<node.getAttributes().getLength(); i++)
		{
			org.w3c.dom.Node child = node.getAttributes().item(i);
			String name = child.getNodeName();
			if ("Position".equals(name))  position = StringUtil.toDouble(child.getNodeValue());
			if ("ArrowHead".equals(name))  shape = child.getNodeValue();
		}
	}
	public String toString()
	{
		return String.format("<Anchor Position=\"%.2f\" Shape=\"%s\" />", position, shape);
	}

}
