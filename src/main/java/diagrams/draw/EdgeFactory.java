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

	public Edge parseGPML(org.w3c.dom.Node datanode) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = datanode.getChildNodes();
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
					if ("Point".equals(n.getNodeName()))
					{
						NamedNodeMap map = pt.getAttributes();
						if (firstPt)
						{
							String startId = attrMap.get("GraphRef");
							double startx = StringUtil.toDouble(map.getNamedItem("X").getNodeName());
							double starty = StringUtil.toDouble(map.getNamedItem("Y").getNodeName());
							firstPt = false;
						}
						else
						{
							String endId = attrMap.get("GraphRef");
							double endx = StringUtil.toDouble(map.getNamedItem("X").getNodeName());
							double endy = StringUtil.toDouble(map.getNamedItem("Y").getNodeName());

						}
							
					}
	
				}
			}
		}
		attrMap.add(datanode.getAttributes());
		String startId = attrMap.get("GraphRef");
		String endId = attrMap.get("GraphRef");
		Node startNode = getModel().find(startId);
		Node endNode = getModel().find(endId);
		if (startNode == null || endNode == null) return null;
		Edge edge = new Edge(startNode, endNode);
		return edge;
	}
}
