package diagrams.draw;

import java.util.ArrayList;
import java.util.List;

import diagrams.draw.gpml.GPMLAnchor;
import diagrams.draw.gpml.GPMLPoint;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import model.AttributeMap;
import util.LineUtil;
import util.StringUtil;

// started out from this reference, but there's much more admin cruft now
//http://stackoverflow.com/questions/19748744/javafx-how-to-connect-two-nodes-by-a-line
//
// Edge maintains BOTH a line and a polyline to connect nodes.  The line is only
// there to make binding easier.  The polyline should be used exclusively (IMHO).

public class Edge {

	private Circle head, tail;
	private List<GPMLPoint> points = new ArrayList<GPMLPoint>();
	private List<GPMLAnchor> anchors = new ArrayList<GPMLAnchor>();
	private AttributeMap attributes = new AttributeMap();
	private Node startNode=null, endNode=null;
	private Polyline polyline;
	private Line line;

	public Node getStartNode()	{ return startNode;	}
	public Node getEndNode()	{ return endNode;	}
	public Polyline getPolyline()	{ return polyline;	}
	public Line getLine()	{ return line;	}
	public Circle getHead()		{ return head;	}
	public Circle getTail()		{ return tail;	}
	public String getDatabase() { return attributes.get("Database");	}
	public String getDbId() 	{ return attributes.get("ID");	}
	public List<GPMLPoint> getPoints() 		{ 	return points;	}
	public List<GPMLAnchor> getAnchors() 	{ 	return anchors;	}

	//----------------------------------------------------------------------
	public Edge(Node start, Node end) 
    {
		this(start, end, null);
    }
	public Edge(Node start, Node end, AttributeMap attr) 
	    {
    	startNode = start;
    	endNode = end;
    	head = new Circle(0,0,8);
    	tail = new Circle(0,0,5);
    	polyline = new Polyline();
    	line = new Line();
    	init();
		setAttributes(attr);
    }
    	
	public Edge(Double startX, Double startY, Double endX, Double endY) 
    {
		polyline = new Polyline(startX, startY, endX, endY);
    	line = new Line();
		startNode =  endNode = null;
		head =  tail = null;
     }
    	
	public Edge(AttributeMap attrs, List<GPMLPoint> points) 
    {
		polyline = new Polyline();
    	line = new Line();
		for (GPMLPoint pt : points)
			polyline.getPoints().addAll(pt.getX() + pt.getRelX(), pt.getY() + pt.getRelY());
		
		GPMLPoint first = points.get(0);
		GPMLPoint last = points.get(points.size()-1);
		LineUtil.set(line, first.getX(), first.getY(), last.getX(), last.getY());
		startNode =  endNode = null;
		head =  tail = null;
		setAttributes(attrs);

     }

    public Edge(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY) 
    {
    	bind(startX, startY, endX, endY);
    }
    	
    public Edge(AttributeMap attr, Model model) 
    {
    	String startNodeId = attr.get("start");
    	startNode = model.getResource(startNodeId);
    	assert(startNode != null);
    	String endNodeId = attr.get("end");
    	endNode = model.getResource(endNodeId);
    	assert(endNode != null);
    	init();
    	setAttributes(attr);
    }
 
	//------------------------------------------------------------------------------------------
    private void init()
    {
       	NodeCenter s = new NodeCenter(startNode);
    	NodeCenter e = new NodeCenter(endNode);
		bind(s.centerXProperty(), s.centerYProperty(), e.centerXProperty(), e.centerYProperty());
//		head = new Circle(0,0,8);
//		tail = new Circle(0,0,3);
//		bind(head.centerXProperty(), head.centerYProperty(), e.centerXProperty(), e.centerYProperty());
//		bind(tail.centerXProperty(), tail.centerYProperty(), s.centerXProperty(), s.centerYProperty());
   }
    private void setAttributes(AttributeMap attr) {
 		if (attr == null || attr.isEmpty()) return;
 		attributes.addAll(attr); 
 		for (String key : attributes.keySet())
 		{
 			String val = attributes.get(key);
			if ("LineThickness".equals(key))
 			{
 				double d = StringUtil.toDouble(val);
 				if (line != null) line.setStrokeWidth(d);
 				if (polyline != null) polyline.setStrokeWidth(d);
 			}

 			else if ("ZOrder".equals(key))		{}
 			else if ("ConnectorType".equals(key))		{}
 			else if ("Color".equals(key))		
 			{
 				Paint p = Paint.valueOf(val);
 				if (line != null) line.setStroke(p);
 				if (polyline != null) polyline.setStroke(p);
 			}
 		}
 		
 	}

    //------------------------------------------------------------------------------------------
    public boolean isStart(Node n)	{  return n == startNode;	}
    public boolean isEnd(Node n)	{  return n == endNode;	}
    public boolean isEndpoint(Node n)	{  return isStart(n) || isEnd(n);	}
    private void  bind(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY)
    {
    	if (line != null)
    	{
    		line.startXProperty().bind(startX);
    		line.startYProperty().bind(startY);
    		line.endXProperty().bind(endX);
    		line.endYProperty().bind(endY);
    	}
    	if (polyline != null)
    	{
		    polyline.getPoints().addAll(startX.get(), startY.get());
		    polyline.getPoints().addAll(endX.get(), endY.get());
	
	    	polyline.setStrokeWidth(2);
		    polyline. setStroke(Color.GRAY.deriveColor(0, 1, 1, 0.5));
		    polyline.setStrokeLineCap(StrokeLineCap.BUTT);
		    polyline.getStrokeDashArray().setAll(10.0, 5.0);
		    polyline.setMouseTransparent(true);
    	}
    }
    
    @Override public String toString()
    {
    	return "from: " + startNode.getId() + " to " + endNode.getId();
    }
    public String getPointsStr ()
	{
		List<GPMLPoint> pts = getPoints();
		StringBuilder builder = new StringBuilder();
		for (GPMLPoint pt : pts)
			builder.append(pt.toString());
		return builder.toString();
	}
    
    public String getAnchorsStr ()
	{
		List<GPMLAnchor> anchors = getAnchors();
		StringBuilder builder = new StringBuilder();
		for (GPMLAnchor a : anchors)
			builder.append(a.toString());
		return builder.toString();
	}
}
// **-------------------------------------------------------------------------------
	 
