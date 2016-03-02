package uploader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import services.MD5;
import util.FileUtil;
import util.StringUtil;

public class ManifestBuilder
{
	public boolean makeAcsManifest(File topFile)
	{
		try
		{
			StringBuilder manifest = new StringBuilder();
			manifest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			manifest.append("<toc:TOC \n");
			manifest.append(" xmlns:toc = \"http://www.isac-net.org/std/ACS/1.0/toc/\"\n");
			manifest.append(" mlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n");
			manifest.append(" xmlns:sig = \"http://www.w3.org/2000/09/xmldsig#\"\n");
			manifest.append(" xsi:schemaLocation = \"http://www.isac-net.org/std/ACS/1.0/toc/");
			manifest.append(" http://flowcyt.sf.net/acs/toc/TOC.v1.0.xsd\"\n>\n");
		
			List<String> descriptors = new ArrayList<String>();
			traverseFiles(topFile, descriptors);
			for (String s : descriptors)		manifest.append(s);
			manifest.append("</toc:TOC>");
			FileUtil.writeTextFile(topFile, "TOC1.xml", manifest.toString());
		}
		catch(	Exception ex) { return false;	}
		return true;
	}
	
	//----------------------------------------------------------------
	public boolean makeEDLManifest(File topFile)
	{
		List<String> objects = new ArrayList<String>();
		List<String> methods = new ArrayList<String>();
		List<String> images = new ArrayList<String>();
		List<String> other = new ArrayList<String>();
		mine(topFile, objects, methods, images, other);	
	
		StringBuilder manifest = new StringBuilder();
		manifest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		manifest.append("<UploadInformation>\n");
		manifest.append(" xmlns:edl = \"http://www.zkw.com/toc/\"\n");				// TODO 
		manifest.append(" mlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n");// TODO 
		manifest.append(" xmlns:sig = \"http://www.w3.org/2000/09/xmldsig#\"\n");	// TODO 
		manifest.append(" xsi:schemaLocation = \"http://www.zkw/toc.com/\n");		// TODO 
		manifest.append("\t<MetaInfo ID=" + topFile.getName() + "/>\n");
		
		manifest.append("\t<EDLEntities >\n");
		addSection(manifest, "EDLObjects", objects);
		addSection(manifest, "EDLMethods", methods);
		manifest.append("\t</EDLEntities >\n\n");
		
		addSection(manifest, "ImageFiles", images);
		addSection(manifest, "TableFiles", other);
									
		manifest.append("</UploadInformation >\n");
		FileUtil.writeTextFile(topFile, "manifest", manifest.toString());
		return true;
	}

	private void addSection(StringBuilder manifest, String string, List<String> objects)
	{
		manifest.append("\t<" + string + " >\n");
		for (String s : objects)	manifest.append("\t").append(s);
		manifest.append("\t</" + string + " >\n\n");
	}

	//----------------------------------------------------------------
	// recurse thru the file system sorting files into four sets
	private void mine(File directory, List<String> objects, List<String> methods, List<String> images, List<String> other)
	{
		if (!directory.isDirectory())	return;		// error
		for (File f : directory.listFiles())
		{
			if (f.getName().startsWith(".")) continue;
			else if (isEDLObjectFile(f)) 	objects.add(getEDLObjectDescriptor(f));
			else if (isEDLMethodFile(f)) 	methods.add(getEDLMethodDescriptor(f));
			else if (isEDLImageFile(f)) 	images.add(getEDLImageDescriptor(f));
			else if (f.isDirectory())  		mine(f, objects, methods, images, other);
			else other.add(getEDLTableDescriptor(f));
		}
	}
	//----------------------------------------------------------------
	// recurse thru the file system sorting files into four sets
	private void traverseFiles(File directory,   List<String> fileUrls)
	{
		if (!directory.isDirectory())	return;		// error
		for (File f : directory.listFiles())
		{
			if (f.getName().startsWith(".")) continue;
			else if (f.isDirectory())  		traverseFiles(f,fileUrls);
			else fileUrls.add(getFileUrl(f));
		}
	}
	private String getFileUrl(File f)
	{
		String relativePath = f.getAbsolutePath();
		return "<toc:file toc:URI = \"file://" + relativePath + "\"</toc:file>	\n";
	}


	//----------------------------------------------------------------
	// TODO   RELYING ON CONVENTIONS HERE -- MORE ROBUST PATTERN MATCHING REQD
	

	private boolean isEDLMethodFile(File f)
	{
		String name = f.getName();
		return (name.startsWith("E") && isEDLname(name));
	}
	private boolean isEDLObjectFile(File f)
	{
		String name = f.getName();
		return (!name.startsWith("E") && isEDLname(name));
	}
	private boolean isEDLImageFile(File f)
	{
		return FileUtil.isImageFile(f);
	}
	private boolean isEDLname(String s)
	{
		char c = s.charAt(0);
		boolean startsGood = (c=='E' || c=='D' || c == 'M');
		String tail = s.substring(1);
		boolean rightExt = (tail.endsWith(".XML"));
		String id = StringUtil.chopExtension(tail);
		boolean numberTail = StringUtil.isNumber(id);
		return (startsGood && numberTail && rightExt);
	}
	//----------------------------------------------------------------
	
	private String getEDLObjectDescriptor(File f)
	{
		return " <EDLObject ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </EDLObject>\n";
	}
	private String getEDLMethodDescriptor(File f)
	{
		return " <EDLMethod ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </EDLMethod>\n";
	}
	private String getEDLImageDescriptor(File f)
	{
		File parent = f.getParentFile();		if (parent == null) return "";
		File gp = parent.getParentFile();		if (gp == null) return "";
		File ggp = gp.getParentFile();			if (ggp == null) return "";
		String id = ggp.getName() + "/" + gp.getName();
		return " <ImageFile ID=\"" + id + "\">" + fileSpecWithPath(f) + " </ImageFile>\n";
	}
	
	private String getEDLTableDescriptor(File f)
	{
		return " <TableFile ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </TableFile>\n";
	}
	//----------------------------------------------------------------
	private String fileSpec(File f)
	{
		return "<File filename=\"" + f.getName() + "\" length=\""+ f.length() + "\" md5=\"" + MD5.forFile(f) + "\" /> ";
	}
	//----------------------------------------------------------------
	private String fileSpecWithPath(File f)
	{
		String path = getNAncestors(f, 4); 
		return "<File filename=\"" + f.getName() + "\" path=\"" + path + "\" length=\""+ f.length() + "\" md5=\"" + MD5.forFile(f) + "\" /> ";
	}
	//----------------------------------------------------------------
	private String getNAncestors(File f, int n)
	{
		String path = "";
		File file = f.getParentFile();
		while (n-- > 0 &&  file != null)
		{
			path =  file.getName() + "/" + path;
			file = file.getParentFile();
		}
		return path;
	}

}
