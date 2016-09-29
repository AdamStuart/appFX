package diagrams.draw;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import model.AttributeMap;

//
//http://stackoverflow.com/questions/19748744/javafx-how-to-connect-two-nodes-by-a-line

public class Edge extends Line {

	private Node startNode, endNode;
	public Node getStartNode()	{ return startNode;	}
	public Node getEndNode()	{ return endNode;	}
	
	public Edge(Node start, Node end) 
    {
    	startNode = start;
    	endNode = end;
    	init();
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
    }

    private void setAttributes(AttributeMap attr) {
 		//TODO
 	}

    //------------------------------------------------------------------------------------------
    public boolean isStart(Node n)	{  return n == startNode;	}
    public boolean isEnd(Node n)	{  return n == endNode;	}
    public boolean isEndpoint(Node n)	{  return isStart(n) || isEnd(n);	}
   
    private void  bind(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY)
    {
    	startXProperty().bind(startX);
	    startYProperty().bind(startY);
	    endXProperty().bind(endX);
	    endYProperty().bind(endY);
	    setStrokeWidth(2);
	    setStroke(Color.GRAY.deriveColor(0, 1, 1, 0.5));
	    setStrokeLineCap(StrokeLineCap.BUTT);
	    getStrokeDashArray().setAll(10.0, 5.0);
	    setMouseTransparent(true);
    }
    
    @Override public String toString()
    {
    	return "from: " + startNode.getId() + " to " + endNode.getId();
    }
}
// **-------------------------------------------------------------------------------
	 
