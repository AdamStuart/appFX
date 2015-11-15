package container.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.scene.control.TreeItem;
import util.FileUtil;
import util.StringUtil;

public class MethodsTree
{

	public MethodsTree(TreeItem<File> root)
	{
		System.out.println("showMethodsTree");
		long startTime = System.currentTimeMillis();
		if (root == null)
		{	System.out.println("No root specified");  return; }

		
		Map <String, List<String>> inputMap = new HashMap <String, List<String>>();
		Map <String, List<String>> outputMap = new HashMap <String, List<String>>();
		List <String> ids = new ArrayList <String>();
		traverseFiles(root, ids, inputMap, outputMap);
		
//		System.out.println("Method IDs: " + ids.toString());
//		System.out.println("Inputobjects: " + inputMap.toString());
//		System.out.println("Outputobjects: " + outputMap.toString());
		
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
//			System.out.println(input + " ---> " + id + " ---> " + output);
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
	
	void traverseFiles(TreeItem<File> tree,List<String> id, Map <String, List<String>> in, Map <String, List<String>> out)
	{
		if (tree == null) return;
		File file = tree.getValue();
		if (file == null) return;
//		System.out.println("node: " + file);
		
		if (FileUtil.isXML(file))
		{
//			System.out.println("isXML");
			TreeItem<org.w3c.dom.Node> xml = FileUtil.getXMLtree(file, null);
			for (TreeItem<org.w3c.dom.Node> child : xml.getChildren())
			{
				String name = child.getValue().getNodeName();
				if (!"Method".equals(name))
				{
//					System.out.println("not a method");
					continue;
				}
			org.w3c.dom.Node value = child.getValue();
			String myId = StringUtil.chopExtension(file.getName()); // value.getNodeValue();
			org.w3c.dom.Node inputTree = getInputTree(value);
			org.w3c.dom.Node oututTree = getOutputTree(value);
			extractObjects(myId, inputTree, in);
			extractObjects(myId, oututTree, out);

			id.add(myId);
			}
		/*
			 * read the file
			 * getDoc, getTopElement
			 * if(top -- isOurElement
			 * 	traverseXML(doc, id, in, out);
			 * 
			 */
		}
		for (TreeItem<File> child : tree.getChildren() )
			traverseFiles(child, id, in, out);
	}
	private org.w3c.dom.Node getInputTree(org.w3c.dom.Node value)
	{
		if (value == null) return null;
		NodeList children = value.getChildNodes();
		if (children == null) return null;
		int sz = children.getLength();
		for (int i=0; i<sz; i++)
		{
			Node child = children.item(i);
			String nodename = child.getNodeName();
			if ("Inputobjects".equals( nodename))
				return child;
		}
		return null;
	}
	private Node getOutputTree(org.w3c.dom.Node value)
	{
		if (value == null) return null;
		NodeList children = value.getChildNodes();
		if (children == null) return null;
		int sz = children.getLength();
		for (int i=0; i<sz; i++)
		{
			Node child = children.item(i);
			if ("Outputobjects".equals(child.getNodeName()))
				return child;
		}
		return null;
	}

	private void extractObjects(String id, Node inNode, Map<String, List<String>> map)
	{
		NodeList children = inNode.getChildNodes();
		if (children == null) return;
		if (map == null) return;
		int sz = children.getLength();
		for (int i=0; i<sz; i++)
		{
			Node child = children.item(i);
			if ("Obj".equals(child.getNodeName()))
			{
				NodeList children2 = child.getChildNodes();
				if (children2 == null) return;
				int sz2 = children2.getLength();
				for (int j=0; j<sz2; j++)
				{
					Node child2 = children2.item(j);
					if (child2 != null && "Identity".equals(child2.getNodeName()))
					{
						Node attr = child2.getAttributes().getNamedItem("UID");
						String ref = attr.getTextContent();
						List<String> hits = map.get(id);
						if (hits == null)
							hits = new ArrayList<String>();
						hits.add(ref);
						map.put(id, hits);
					}

				}
			}
		}
	}
	
	
	
	void traverseXML(TreeItem<File> tree,List<String> id, Map <String, String> in, Map <String, String> out)
	{
		System.out.println("traverseXML");
	}
	

}
