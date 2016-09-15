package diagrams.draw;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

//
//http://stackoverflow.com/questions/19748744/javafx-how-to-connect-two-nodes-by-a-line

public class Edge extends Line {

	private Node startNode, endNode;
    public Edge(Node start, Node end) 
    {
    	startNode = start;
    	endNode = end;
    	NodeCenter s = new NodeCenter(start);
    	NodeCenter e = new NodeCenter(end);
		bind(s.centerXProperty(), s.centerYProperty(), e.centerXProperty(), e.centerYProperty());
    }
    	
    public Edge(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY) 
    {
    	bind(startX, startY, endX, endY);
    }
    	
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
	 
