package diagrams.draw;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javafx.scene.Node;
import model.AttributeMap;
import util.StringUtil;

public class EdgeFactory {

	private Pasteboard drawLayer;
	private UndoStack undoStack;
	private ShapeFactory shapeFactory;	

	public EdgeFactory(Pasteboard pasteboard) {
		drawLayer = pasteboard;
		undoStack = drawLayer.getController().getUndoStack();
		shapeFactory = new ShapeFactory(drawLayer, undoStack);
	}
	private Model getModel()				{ 	return drawLayer.getController().getDrawModel();	}
	private Controller getController()		{ 	return drawLayer.getController();	}
	private String gensym(String s)			{	return getModel().gensym(s);	}
	public ShapeFactory getShapeFactory()	{ 	return shapeFactory; }

	public Edge parseGPML(org.w3c.dom.Node edgeML) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = edgeML.getChildNodes();
		String startId="", endId="";
		double startx=0, starty=0, endx=0, endy=0;
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node n = elems.item(i);
			if ("Graphics".equals(n.getNodeName()))
			{
				NodeList pts = n.getChildNodes();
				boolean firstPt = true;
				for (int j=0; j<pts.getLength(); j++)
				{
					org.w3c.dom.Node pt = pts.item(j);
					if ("Point".equals(pt.getNodeName()))
					{
						NamedNodeMap map = pt.getAttributes();
						if (firstPt)
						{
							startId = getStr(map, "GraphRef");
							startx = getVal(map, "X");
							starty = getVal(map, "Y");
							firstPt = false;
						}
						else
						{
							endId = getStr(map, "GraphRef");
							endx = getVal(map, "X");
							endy = getVal(map, "Y");
						}
					}
				}
			}
		}
		attrMap.add(edgeML.getAttributes());
		Node startNode = getModel().find(startId);
		Node endNode = getModel().find(endId);
		if (startNode != null && endNode != null) 
			return new Edge(startNode, endNode);
		Edge edge = new Edge(startx, starty, endx, endy);
		return edge;
	}
	//--------------------------------------------
	private String getStr(NamedNodeMap map, String key) {
		org.w3c.dom.Node node = map.getNamedItem(key);
		return 	node == null ? null : node.getNodeValue();
	}
	private double getVal(NamedNodeMap map, String key) {
		org.w3c.dom.Node node = map.getNamedItem(key);
		return 	node == null ? Double.NaN : StringUtil.toDouble(node.getNodeValue());
	}
	//--------------------------------------------
}
