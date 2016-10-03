package diagrams.draw;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.draw.gpml.GPMLPoint;
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
	private Controller getController()		{ 	return drawLayer.getController();	}
	private Model getModel()				{ 	return getController().getDrawModel();	}
	public ShapeFactory getShapeFactory()	{ 	return shapeFactory; }

	public Edge parseGPML(org.w3c.dom.Node edgeML) {
		AttributeMap attrMap = new AttributeMap();
		attrMap.add(edgeML.getAttributes());
		List<GPMLPoint> points = new ArrayList<GPMLPoint>();
		NodeList elems = edgeML.getChildNodes();
		String startId="", endId="";
		double startx=0, starty=0, endx=0, endy=0;
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node n = elems.item(i);
			String name = n.getNodeName();
			if ("Graphics".equals(name))
			{
				attrMap.add(n.getAttributes());
				NodeList pts = n.getChildNodes();
				for (int j=0; j<pts.getLength(); j++)
				{
					org.w3c.dom.Node pt = pts.item(j);
					if ("Point".equals(pt.getNodeName()))
						points.add(new GPMLPoint(pt));
				}
			}
			if ("Xref".equals(name))	
				attrMap.add(n.getAttributes());
		}
		int z = points.size();
		if (z > 1)
		{
			startId = points.get(0).getGraphRef();
			Node startNode = getModel().getResource(startId);
			if (startNode != null)
			{
				endId = points.get(z-1).getGraphRef();
				Node endNode = getModel().getResource(endId);
				if (endNode != null) 
					return new Edge(startNode, endNode, attrMap);
			}
			
		}
//		Edge edge = new Edge(startx, starty, endx, endy);
		Edge edge = new Edge(attrMap, points);

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
