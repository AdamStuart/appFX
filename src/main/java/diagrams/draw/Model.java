package diagrams.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import diagrams.draw.gpml.GPML;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import util.StringUtil;

public class Model
{
/*
 *  draw.Model - this program uses the Scene Graph (the View!) as the model	
 *  
 *  So, most of this class is a map to keep resources in, and utilities to look into nodes.
 *  Normally the Model is the access to the key data structures.
 *  It is very much application-specific.
 */
	private Controller controller;
	private Map<String, Node> resourceMap;
	private int nodeCounter;
	private ListView<Node> resourceListView;
	public ListView<Node> getResourceListView()		{		return resourceListView;	}
	private void refresh() {	resourceListView.setItems(resourceListView.getItems()); }		// HACK to update list
	

	private List<Edge> edgeTable;
	private ListView<Edge> edgeListView;
	
	public Controller getController() { return controller; } 
	private Scene getScene()		{ 	return getController().getPasteboard().getPane().getScene();  }
	public Node find(String id)		{	return getScene().lookup(id);	}

	
	public Model(Controller ct)
	{
		controller = ct;
		edgeTable = FXCollections.observableArrayList();
		resourceMap = FXCollections.observableHashMap();
		nodeCounter = 0;
		resourceListView = controller.getResourceListView();
	}
	
//	public void addResource(Node rgn)				{  resourceMap.put(rgn.getId(), rgn);		}
	// **-------------------------------------------------------------------------------
	public void addResource(String key, Node n)		
	{  
		if (resourceMap.get(key) == null)
		{
			resourceMap.put(key, n);	
			resourceListView.getItems().add(n);
			refresh();
		}
	}
	public Edge addEdge(Node start, Node end)		
	{  
		Edge edge = new Edge(start, end, null);
		return addEdge(edge);
	}
	
	public Edge addEdge(Edge e)		
	{  
		edgeTable.add(e);
		return e;
	}
	// **-------------------------------------------------------------------------------
	public List<Edge> connectSelectedNodes()		
	{  
		List<Edge> edges = new ArrayList<Edge>();
		List<Node> selection = controller.getSelection();
		for (int i=0; i<selection.size()-1; i++)
		{
			Node start = selection.get(i);
			if (start instanceof Line) continue;
			for (int j=i+1; j < selection.size(); j++)
			{
				Node end = selection.get(j);
				if (end instanceof Line) continue;
				edges.add(addEdge( start, end));
			}
		}
		return edges;
	}
	public void removeEdge(Edge edge)		
	{  
		edgeTable.remove(edge);
	}
	public void removeEdges(Node node)		
	{  
		for (int z = edgeTable.size()-1; z >= 0; z--)
		{
			Edge e = edgeTable.get(z);
			if (e.isStart(node) || e.isEnd(node))
			{
				e.getPolyline().setVisible(false);
				edgeTable.remove(e);
			}
		}
//		List<Edge> okEdges = edgeTable.stream().filter(new TouchingNodeFilter(node)).collect(Collectors.toList());
//		edgeTable.clear();
//		edgeTable.addAll(okEdges);
	}
	// **-------------------------------------------------------------------------------

	public static class TouchingNodeFilter implements Predicate<Edge>
	{
		Node node;
		TouchingNodeFilter(Node n)				{	node = n;		}
		@Override public boolean test(Edge e)	{	return (!e.isStart(node) && !e.isEnd(node));	}
	}
	
	public void removeNode(Node node)		
	{  
		if (node != null && ! "Marquee".equals(node.getId()))
			removeEdges(node);
	}
	public void addResource(int idx, String key, Node n)		
	{  
		if (resourceMap.get(key) == null)
		{
			resourceMap.put(key, n);	
			resourceListView.getItems().add(idx, n);
			refresh();
		}
	}
	public Node getResource(String key)				
	{
		 if (key == null) return null;
		 if (key.startsWith("\""))  
		 {
			 int len = key.length();
			 key = key.substring(1,len-1);
		 }
		 Node n = resourceMap.get(key);	
		 return n;
	}
	
	public String cloneResourceId(String oldId)
	{
//		Node old = getResource(oldId);
//		if (old != null)
//		{
//			String text = describe(old);
//			String newId = gensym(oldId.substring(0,1));
//			String newText = inject(text, "id=", ",", newId);
//			
//			Node clone = controller.getNodeFactory().cloneNode(oldId, newId);
//			if (clone != null)
//				controller.add(clone);
//		}
		return gensym(oldId.substring(0,1));
	}
	
	
// **-------------------------------------------------------------------------------
	
	public StringBuilder traverseSceneGraph(Pane root)
	{
		StringBuilder buff = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buff.append("<Pathway>\n");
		Pasteboard board  = controller.getPasteboard();
		int width = (int) board.getWidth();
		int height = (int) board.getHeight();
		buff.append(String.format("<Graphics BoardWidth=\"%d\" BoardHeight=\"%d\" />\n", 
				width, height));
		traverse(buff, root, 0);
		buff.append("</Pathway>\n");
		return buff;
	}
	
	static private void traverse(StringBuilder buff,Node node, int indent)
	{
		if (ShapeFactory.isMarquee(node)) return;
//		if (node instanceof Edge)			buff.append(describe(node));	
		if (node instanceof Shape)			buff.append(describe(node));	
		if (node instanceof StackPane)		buff.append(describe(node));
		if (node instanceof Parent)
			for (Node n : ((Parent)node).getChildrenUnmodifiable())
			{
				String id = n.getId();
				if (id == null)					continue;			// only propagate thru nodes with ids
				if ("Marquee".equals(id) )		continue;
				
				if (n instanceof Text)
				{
					String txt = ((Text) n).getText();
					if (txt.length() < 1) 	continue;				//System.out.println("Don't stream empty text");
				}
				traverse(buff,n, indent+1);
			}
	}
	// **-------------------------------------------------------------------------------
	static public String describe(Node node)	{	return GPML.dataNodeToGPML(node);	}
	static String getBoundsString(double x, double y, double w, double h)	{
	 return String.format("x=%.1f, y=%.1f, width=%.1f, height=%.1f", x, y, w, h);
	}
	public String gensym(String prefix)	{		return (prefix == null ? "" : prefix ) + ++nodeCounter;	}
	
}



