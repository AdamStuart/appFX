package diagrams.draw;

import java.util.Map;

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import util.StringUtil;

public class Model
{
/*
 *  draw.model - this program uses the Scene Graph (the View!) as the model	
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
	
	public Controller getController() { return controller; } 
	
	public Model(Controller ct)
	{
		controller = ct;
		resourceMap = FXCollections.observableHashMap();
		nodeCounter = 0;
		resourceListView = controller.getResourceListView();
	}
	
//	public void addResource(Node rgn)				{  resourceMap.put(rgn.getId(), rgn);		}
	public void addResource(String key, Node n)		
	{  
		if (resourceMap.get(key) == null)
		{
			resourceMap.put(key, n);	
			resourceListView.getItems().add(n);
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
	
	static public StringBuffer traverseSceneGraph(Pane root)
	{
		StringBuffer buff = new StringBuffer();
		traverse(buff, root, 0);
		return buff;
	}
			
	static private void traverse(StringBuffer buff,Node node, int indent)
	{
		if (ShapeFactory.isMarquee(node)) return;
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
	
	static String LINEDIM = "\n";
	
	static public String describe(Node node)
	{		
		Map<Object, Object> props = node.getProperties();
		StringBuffer buffer = new StringBuffer();
		for (Object key : props.keySet())
		{
			if (key instanceof String)
			{
				Object o = props.get(key); 
				if (o instanceof Property)
				{		
					Object val = ((Property) o).getValue();
					buffer.append(key).append("=").append(val.toString()).append(", ");
				}
			}
		}
		String style = node.getStyle();
		String basic = node.toString();
		
		String propStr = buffer.toString();
		if (propStr.length() > 0)
		{
			propStr = StringUtil.chopLast2(propStr);	// chop off ", "
			basic = StringUtil.chopLast(basic);		// chop off "]"

			if (node instanceof StackPane && style != null)
			{
				StackPane p = (StackPane) node;
				String bounds = getBoundsString(p.getLayoutX(), p.getLayoutY(), p.getWidth(), p.getHeight());
				bounds += String.format(", rotate=%.1f",p.getRotate());
				if (style.length() > 0)
				{
					String styl = style.substring(1);
					styl = styl.replace(": ", "=");
					styl = styl.replace("; ", ", ");
					styl = styl.substring(0, styl.length()-1);
					styl = styl.substring(0, styl.length() - 2);	// chop off ", "
					bounds +=  ", " + styl;
				}
				basic += ", " + bounds;
			}
			basic += ", " + propStr + "]";		
		}
		
		
		
//		String id = ", id="+node.getId();
//		String selected = ", selected=" + ((node.getEffect() == null) ? "FALSE" : "TRUE") + "]";			// HACK uses effect!=null for selected
//		String out = basic.substring(0, basic.length()-1);
		return basic + LINEDIM;
		
	}
	static String getBoundsString(double x, double y, double w, double h)
	{
	 return String.format("x=%.1f, y=%.1f, width=%.1f, height=%.1f", x, y, w, h);
	}
	
	public String gensym(String prefix)	{		return (prefix == null ? "" : prefix ) + ++nodeCounter;	}
}



