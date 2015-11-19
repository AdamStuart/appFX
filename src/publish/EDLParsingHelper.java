package publish;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.w3c.dom.Node;

public class EDLParsingHelper
{
	public EDLParsingHelper(TreeTableView<Node> xmlTree)
	{
		TreeTableColumn<Node, String> nameColumn = new TreeTableColumn<Node, String>("Name");
		nameColumn.setPrefWidth(500);
		TreeTableColumn<Node, String> idCol = new TreeTableColumn<Node, String>("Id");
		idCol.setPrefWidth(100);
		xmlTree.getColumns().setAll(new TreeTableColumn[] {nameColumn, idCol});

		nameColumn.setCellValueFactory(p ->
		{
			Node f = p.getValue().getValue();
			String text = "error";
			if (f != null)
				text = getNameCellText(f);
			return new ReadOnlyObjectWrapper<String>(text);
		});
		
		idCol.setCellValueFactory(p ->
		{
			Node f = p.getValue().getValue();
			String text = "error";
			if (f != null)
				text = getIDCellText(f);
			return new ReadOnlyObjectWrapper<String>(text);
		});
	}
	//-------------------------------------------------------------------
	// EDL specific hacks to make XML more human readable
	// Map terms, and selectively replace the node name with 
	// 		the node's type or name attributes
	
	String  getNameCellText(Node f)	
	{
		String text = "";
		text = lookup(f.getNodeName());
		
		if (text.equals("Object") || text.equals("Method")|| text.equals("Machine"))
		{
			Node n = f.getAttributes().getNamedItem("Type");
			if (n != null)
				text = n.getTextContent();
		} else
		{
			Node n = f.getAttributes().getNamedItem("Name");
			if (n != null)
				text = n.getTextContent();
		}
		text = lookup(text);
		if (text.length() > 100)
			text = text.substring(0,100) + "...";
		return text;
	}
	
	String  getIDCellText(Node f)	
	{
		String text = "";
		if (f.getNodeName().equals("Restriction"))
		{
			Node n = f.getAttributes().getNamedItem("MinOccur");
			if (n != null) 	text = n.getTextContent();
			n = f.getAttributes().getNamedItem("MaxOccur");
			if (n != null) 	text += ", " + n.getTextContent();
		}
		else
		{
			text = f.getTextContent();
			if (text.length() > 64)
				text = text.substring(0,64) + "...";
			
			Node n = f.getAttributes().getNamedItem("UID");
			if (n != null)	text = n.getTextContent();
			n = f.getAttributes().getNamedItem("Value");
			if (n != null)
			{
				text = n.getTextContent();
				n = f.getAttributes().getNamedItem("Unit");
				if (n != null) text += " " + n.getTextContent();
			}
		}
		return text;
	}
	
	//-------------------------------------------------------------------
	static String lookup(String orig)
	{
		if (orig == null) return "";
		String val = lookupMap.get(orig);
		return (val == null) ? orig : val;
	}
	//-------------------------------------------------------------------
	static Map<String,String> lookupMap = new HashMap<String,String>();
	
	void setupDictionary()
	{
		lookupMap.put("Inputobjects", "Input");
		lookupMap.put("Outputobjects", "Output");
		lookupMap.put("PrimaryContainer", "Container");
		lookupMap.put("ObjRef", "Reference");
		lookupMap.put("Obj", "Object");
		lookupMap.put("SpecificParameter", "Parameter");
		lookupMap.put("SpecificParameters", "Parameters");
		lookupMap.put("ObjectConnector", "Connection");
		lookupMap.put("EncapsulatedObjects", "Objects");
		lookupMap.put("EncapsulatedMethods", "Steps");
		lookupMap.put("EncapsulatedObjectsRef", "References");
		lookupMap.put("EncapsulatedMethodsRef", "Method References");
		lookupMap.put("Meth", "Method");
		lookupMap.put("MethRef", "Method Reference");
		lookupMap.put("MethodHistory", "History");
	}
		
		

}
