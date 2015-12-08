package publish;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import util.FileUtil;
import util.StringUtil;

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
	//--------------------------------------------------------------------------------
	static private File findFile(File[] dir, String idName)
	{
		for (File child : dir)
			if (StringUtil.chopExtension(child.getName()).equals(idName))
				return child;
		return null;
	}
	
	static  private TreeItem<org.w3c.dom.Node> getChildTreeItem(TreeItem<org.w3c.dom.Node> parent, String elemName)
	{
		for (TreeItem<org.w3c.dom.Node> child : parent.getChildren())
		{	
			org.w3c.dom.Node node = child.getValue();
			String name = node.getNodeName();
			if (name.equals(elemName))
			
				return child;
		}
		return null;
	}
	
	
	static public void setEDLDirectory(File f, TreeTableView<org.w3c.dom.Node> xmlTree, ListView<ScanJob> scans, ListView<Segment> segments)
	{
		File objectFile = null;
		File[] topLevelFiles = f.listFiles();
		
		objectFile = findFile(topLevelFiles, f.getName());
		if (objectFile == null) return;
		TreeItem<org.w3c.dom.Node> root = FileUtil.getXMLtree(objectFile, null);
		xmlTree.setRoot(root);
		TreeItem<org.w3c.dom.Node> obj = getChildTreeItem(root, "Obj");
		if (obj != null)
		{
			TreeItem<org.w3c.dom.Node> history = getChildTreeItem(obj, "MethodHistory");
			if (history != null)
			{
				List<TreeItem<org.w3c.dom.Node>> steps = history.getChildren();
				int siz = steps.size();
				for (int i=0; i<siz; i++)
				{
					TreeItem<org.w3c.dom.Node> step = steps.get(i);
					org.w3c.dom.Node attr = null;
					if (step != null) 
						attr = step.getValue().getAttributes().getNamedItem("UID");
					if (attr != null) 
					{
						String id = attr.getTextContent();
						File methodFile = findFile(topLevelFiles, id);
						if (methodFile != null)
						{
							TreeItem<org.w3c.dom.Node> methodroot = FileUtil.getXMLtree(methodFile);
							methodroot = methodroot.getChildren().get(0);
							step.getChildren().addAll(methodroot.getChildren());
						}
					}
				}
			}
		}
			
		for (File child : f.listFiles())
		{
			if (child.isDirectory())		// add sub-directories to the results tab
			{
				String chName = child.getName().toLowerCase();
				if ("scanjobs".equals(chName))
				{
					scans.getItems().clear();
					addScanJobsDirectory(child, scans);
				}
				if ("segments".equals(chName))
				{
					segments.getItems().clear();
					addSegmentsDirectory(child, segments);
				}
			}
		}
	}
	

	//--------------------------------------------------------------------------------
	
	static private void addScanJobsDirectory(File f,ListView<ScanJob> scans)
	{
		for (File kid : f.listFiles())
		{
			if (FileUtil.isImageFile(kid))
			{
				String id = kid.getParentFile().getParentFile().getParentFile().getName();
				scans.getItems().add(new ScanJob(id, kid));
			}
			else if (kid.isDirectory())
				addScanJobsDirectory(kid, scans);
		}
	}

		//--------------------------------------------------------------------------------
	static private void addSegmentsDirectory(File f,ListView<Segment> segments)
	{
		try
		{
			File[] kids = f.listFiles();
			for (File kid : kids)
			{
				String id = kid.getName();
				if (kid.isDirectory())
				{
					File[] grandkids = kid.listFiles();
					for (File gkid : grandkids)
						if (FileUtil.isCSV(gkid))
							segments.getItems().add(new Segment(id, gkid));		// read the file, build the table
				}
			}
		}
		catch (Exception e) {			e.printStackTrace();		}
	}

	public static void addCSVFilesToSegments(File dir, ListView<Segment> segments)
	{
		File[] kids = dir.listFiles();
		for (File kid : kids)
		{
			if (FileUtil.isCSV(kid))
				segments.getItems().add(new Segment(kid.getName(), kid));		// read the file, build the table
		}
	}
}
