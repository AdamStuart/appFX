package diagrams.draw;

import org.w3c.dom.NodeList;

import diagrams.draw.App.Tool;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.AttributeMap;
import util.StringUtil;

public class GPML {

	private Controller controller;

	public GPML(Controller c) {
		controller = c;
	}

	public void read(org.w3c.dom.Document doc)
	{
		NodeList nodes = doc.getElementsByTagName("DataNode");
		ShapeFactory f = controller.getNodeFactory().getShapeFactory();
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node child = nodes.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			Node node = controller.getNodeFactory().parseGPML(child);
			if (node != null)
			{
				ObservableMap<Object, Object> map = node.getProperties();
				String label = map.get("TextLabel").toString();
				if (StringUtil.hasText(label)) {
					final Label text = f.createLabel(label);
			    	NodeCenter ctr = new NodeCenter(node);
			    	text.layoutXProperty().bind(ctr.centerXProperty().subtract(text.widthProperty().divide(2.)));	// width / 2
			    	text.layoutYProperty().bind(ctr.centerYProperty().subtract(text.heightProperty().divide(2.)));
			    	controller.getDependents().put(node, text);
					add(node);
					add(text);
				}
				else	controller.add(node);
			}
		}
		NodeList shapes = doc.getElementsByTagName("Shape");
		for (int i=0; i<shapes.getLength(); i++)
		{
			org.w3c.dom.Node child = shapes.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			Node node = controller.getNodeFactory().parseGPML(child);
			if (node != null)
				add(node);
		}
		NodeList edges = doc.getElementsByTagName("Interaction");
		for (int i=0; i<edges.getLength(); i++)
		{
			org.w3c.dom.Node child = edges.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			Edge edge = controller.getEdgeFactory().parseGPML(child);
			if (edge != null)
			{
				add(edge);
				controller.getModel().addEdge(edge);
			}
		}
		
		handleBiopax(doc.getElementsByTagName("Biopax"));
		handleGroups(doc.getElementsByTagName("Groups"));
		handleLines(doc.getElementsByTagName("Line"));
		handleLinks(doc.getElementsByTagName("Link"));
		handleLabels(doc.getElementsByTagName("Label"));

	}
	
	
	private void handleBiopax(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
		}
		
	}
	private void handleLabels(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
			Label label = getNodeFactory().parseGPMLLabel(child);
			if (label != null)
				add(label);
	
		}
	}
	private void add(Node n)	{ controller.add(n);	}
	private NodeFactory getNodeFactory() {		return controller.getNodeFactory();	}

	private void handleLines(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
		}
	}
	private void handleLinks(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
		}
	}
	private void handleGroups(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
		}
	}
	//----------------------------------------------------------------------------
	static String LINEDIM = "\n";

	public static String nodeToGPML(Node node)
	{
		StringBuilder buffer = new StringBuilder("<DataNode>\n");
		String basic = node.toString();
		basic = StringUtil.chopLast(basic);		// chop off "]"
		String shape = basic.substring(0, basic.indexOf("["));
		basic = basic.replaceAll(",", "");		// strip commas
		double w = node.getLayoutBounds().getWidth();
		double h = node.getLayoutBounds().getHeight();
		double cx = node.getLayoutX() + w / 2;
		double cy = node.getLayoutY() + h / 2;
		String graphics1 = String.format("  <Graphics CenterX=\"%.2f\" CenterY=\"%.2f\" Width=\"%.2f\" Height=\"%.2f\" ZOrder=\"32768\" ", cx, cy, w, h);
		String graphics2 = String.format("FontWeight=\"%s\" FontSize=\"%d\" Valign=\"%s\" ShapeType=\"%s\"", "Bold", 12, "Middle", shape);
		buffer.append(graphics1).append(graphics2).append(" />\n") ;
		buffer.append("  <Xref Database=\"\" ").append("ID=\"\"").append("/>\n") ;
		buffer.append("</DataNode>"+ LINEDIM);
		return buffer.toString();
	}
	
	public static String edgeToGPML(Edge edge)
	{
		StringBuilder buffer = new StringBuilder("<Interaction>\n");
		buffer.append("<Graphics ZOrder=\"12288\" LineThickness=\"1.0\">\n");
		buffer.append(getPoints(edge));
		buffer.append("</Graphics>\n");
		buffer.append("<XRef Database=\"\" ID=\"\">\n");
		buffer.append("</Interaction>\n");
		return buffer.toString();
	}
	
	private static String getPoints (Edge edge)
	{
		String firstPart = String.format("<Point X=\"%.2f\" Y=\"%.2f\" ", edge.getStartX(), edge.getStartY());
		String secondPart = String.format("GraphRef=\"%s\" RelX=\"%.2f\" RelY=\"%.2f\" />\n", edge.getStartNode().getId(),0,0);
		String thirdPart = String.format("<Point X=\"%.2f\" Y=\"%.2f\" ", edge.getEndX(), edge.getEndY());
		String fourthPart = String.format("GraphRef=\"%s\" RelX=\"%.2f\" RelY=\"%.2f\" />\n", edge.getEndNode().getId(),0,0);
		return firstPart + secondPart + thirdPart + fourthPart;
	}
	//----------------------------------------------------------------------------
	public Node[] makeTestItems() {
		Node[] items = new Node[12];
		ShapeFactory f = controller.getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		
		attrMap.putCircle(new Circle(150, 150, 60, Color.AQUA));
		attrMap.put("TextLabel", "Primary");
		Label label = f.createLabel("Primary");
		items[11] = label;
		Circle c = (Circle) f.makeNewShape(Tool.Circle, attrMap);
		items[4] = c;
		label.layoutXProperty().bind(c.centerXProperty().subtract(label.widthProperty().divide(2.)));	// width / 2
		label.layoutYProperty().bind(c.centerYProperty().subtract(label.heightProperty().divide(2.)));
		
		
		attrMap = new AttributeMap();
		attrMap.putCircle(new Circle(180, 450, 60, Color.AQUA));
		attrMap.put("TextLabel", "Secondary");
		items[5] = f.makeNewShape(Tool.Circle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putCircle(new Circle(150, 300, 20, Color.BEIGE));
		attrMap.put("TextLabel", "Tertiary");
		items[6] = f.makeNewShape(Tool.Circle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putRect(new Rectangle(250, 50, 30, 30));
		items[7] = f.makeNewShape(Tool.Rectangle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putRect(new Rectangle(250, 450, 30, 50));
		items[8] = f.makeNewShape(Tool.Rectangle, attrMap);
		
		items[0] = new Edge(items[4], items[5]);
		items[1] = new Edge(items[6], items[4]);
		items[2] = new Edge(items[8], items[4]);
		items[3] = new Edge(items[8], items[5]);
		items[9] = new Edge(items[7], items[8]);
		items[10] = new Edge(items[7], items[6]);
		return items;
	}

}
