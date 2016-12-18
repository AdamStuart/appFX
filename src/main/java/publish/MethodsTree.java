package publish;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;
import util.FileUtil;
import util.StringUtil;
import xml.XMLTools;

// This class is specific to the ZKW implementation of EDL
public class MethodsTree
{

	public MethodsTree(TreeItem<File> root)
	{
		System.out.println("showMethodsTree");
		long startTime = System.currentTimeMillis();
		if (root == null)   {	System.out.println("No root specified");  return; }
		Map <String, List<String>> inputMap = new HashMap <String, List<String>>();
		Map <String, List<String>> outputMap = new HashMap <String, List<String>>();
		List <String> ids = new ArrayList <String>();
		try {
			traverseFiles(root, ids, inputMap, outputMap);
		} catch (Exception e) {		e.printStackTrace();	}

		int sz = ids.size();
		int maxPart1Len = 0;
		String[] part1 = new String[sz];
		String[] part2 = new String[sz];
		for (int i = 0; i< sz; i++)
		{
			String id = ids.get(i);
			List<String> input = inputMap.get(id);
			List<String> output = outputMap.get(id);
			part1[i] = input.toString();
			part2[i] = id + " ---> " + output.toString();
			maxPart1Len = Math.max(part1[i].length(), maxPart1Len);
		}
		for (int i = 0; i< sz; i++)
		{
			int padLen = maxPart1Len - part1[i].length();
			String padding = "                                                   ".substring(0, padLen);
			System.out.println(padding + part1[i] +  " ---> " + part2[i]);
		}
		System.out.println("Time (ms) = " + (System.currentTimeMillis() - startTime));
	}
	//-----------------------------------------------------------------------------
	void traverseFiles(TreeItem<File> tree, List<String> id, Map <String, List<String>> in, Map <String, List<String>> out) throws Exception
	{
		if (tree == null) return;
		File file = tree.getValue();
		if (file == null) return;
		
		if (FileUtil.isXML(file))
		{
			TreeItem<org.w3c.dom.Node> xml = FileUtil.getXMLtree(file, null);
			for (TreeItem<org.w3c.dom.Node> child : xml.getChildren())
			{
				String name = child.getValue().getNodeName();
				System.out.println(name);
				if (!"Method".equals(name))		continue;
				org.w3c.dom.Node value = child.getValue();
				String myId = StringUtil.chopExtension(file.getName()); // value.getNodeValue();
				org.w3c.dom.Node inputTree = getInputTree(value);
				org.w3c.dom.Node oututTree = getOutputTree(value);
				extractObjects(myId, inputTree, in);
				extractObjects(myId, oututTree, out);
				id.add(myId);
			}
		}
		for (TreeItem<File> child : tree.getChildren() )
			traverseFiles(child, id, in, out);
	}

	//-----------------------------------------------------------------------------
	private org.w3c.dom.Node getInputTree(org.w3c.dom.Node value)
	{
		return XMLTools.getChildByName(value, "Inputobjects");
	}
	private Node getOutputTree(org.w3c.dom.Node value)
	{
		return XMLTools.getChildByName(value, "Outputobjects");
	}
	// inNode is either Inputobjects or Outputobjects.  Iterate thru the Obj list
	// and get the Identity element and then the UID attribute
	//-----------------------------------------------------------------------------
	private void extractObjects(String id, Node inNode, Map<String, List<String>> map)
	{
//		System.out.println("extractObjects rewritten with XMLTools - to test"); 
		Node child = XMLTools.getChildByPath(inNode, new String[] { "Obj", "Identity" });
		if (child != null)
		{
			String ref =  XMLTools.getChildAttribute(child, "UID");
			List<String> hits = map.get(id);
			if (hits == null)
				hits = new ArrayList<String>();
			hits.add(ref);
			map.put(id, hits);
		}
	}	

}
