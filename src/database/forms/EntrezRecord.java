package database.forms;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import game.bookclub.StringUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

//http://www.ncbi.nlm.nih.gov/books/NBK25501/
public class EntrezRecord
{
	private SimpleStringProperty pmid = new SimpleStringProperty();
	private SimpleStringProperty pubdate = new SimpleStringProperty();
	private SimpleStringProperty author = new SimpleStringProperty();
	private SimpleStringProperty title = new SimpleStringProperty();
	private SimpleStringProperty status = new SimpleStringProperty();
	private SimpleStringProperty source = new SimpleStringProperty();
	private SimpleStringProperty volume = new SimpleStringProperty();
	private SimpleStringProperty issue = new SimpleStringProperty();
	private SimpleStringProperty pages = new SimpleStringProperty();
	private SimpleStringProperty abst = new SimpleStringProperty();
	
	public StringProperty pmidProperty()	{ return pmid;	}
	public StringProperty authorProperty()	{ return author;	}
	public StringProperty titleProperty()	{ return title;	}
	public StringProperty pubdateProperty()	{ return pubdate;	}
	
	public EntrezRecord()
	{
		status.set("new");
	}

	public EntrezRecord(Element xml)
	{
		this();
		NodeList items = xml.getChildNodes();
		int sz = items.getLength();
		for (int i=0; i< sz; i++)
		{
			Node n = items.item(i);
			if (n instanceof Element)
				readItem((Element) n);
		
		}
	}
	
	public void readItem(Element item)
	{
		String name = item.getAttribute("Name");
		String content = item.getTextContent();
		if (name == null || content == null) return;
		if (name.equals("Source"))						source.set(content);
		else if (item.getNodeName().equals("Id"))		pmid.set(content);
		else if (name.equals("LastAuthor"))				author.set(content);
		else if (name.equals("Title"))					title.set(content);
		else if (name.equals("PubStatus"))				status.set(content);
		else if (name.equals("Volume"))					volume.set(content);
		else if (name.equals("Issue"))					issue.set(content);
		else if (name.equals("Pages"))					pages.set(content);
		else if (name.equals("PubDate")) 				pubdate.set(content);
	}
	String base = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	
	public void fetch()
	{
		String cmd =  "efetch.fcgi?db=pubmed&id=" + getPMID() + "&retmode=text&rettype=abstract";
		System.out.println(cmd);
		String result = StringUtil.callURL(base + cmd);
		System.out.println(result);
		abst.set(result);
	}
	
	
	public String getPMID()		{ return pmid.get();		}
	public String getAuthor()	{ return author.get();		}
	public String getTitle()	{ return title.get();		}
	public String getSource()	{ return source.get();		}
	public String getLocation()	{ return volume.get() + ": " + issue.get() + ": " + pages.get();		}
	public String getStatus()	{ return status.get();		}
	public String getPubDate()	{ return pubdate.get();		}
	public String getAbstract()	{ return abst.get();		}		// TODO
	
	public String toString()	{ return getAuthor() + ", " + getTitle() + ": " + getSource() + ": " + getPMID() + ": " + getPubDate() + ".";}
}
