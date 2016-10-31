package database.forms;

import java.time.LocalDateTime;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.NamedNodeMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import util.StringUtil;

public class EntrezQuery
{
	private SimpleStringProperty name = new SimpleStringProperty();
	private String rawQuery;
	private String exploded;
	private LocalDateTime lastUsed;
	private int useCount;

	public static String getPubMedId(String id)
	{
		String raw = EntrezForm.EUTILS + "efetch.fcgi?db=pubmed&id=" + id + "&retmode=text&rettype=abstract";
		String result = StringUtil.callURL(raw, true);
		return result;
	}
	
	public EntrezQuery(String s)
	{
		name.set(s);
		useCount = 1;
		lastUsed = LocalDateTime.now();
	}

	public EntrezQuery(String s, String raw)
	{
		this(s);
		rawQuery = raw;
	}
	
	public EntrezQuery(NamedNodeMap attributes)
	{
		org.w3c.dom.Node n  = attributes.getNamedItem("name");
		if (n != null)
			name.set(n.getNodeValue());
		
		n = attributes.getNamedItem("raw");
		if (n != null)		rawQuery = n.getNodeValue();
		n = attributes.getNamedItem("exploded");
		if (n != null)		exploded = n.getNodeValue();
		n = attributes.getNamedItem("lastUsed");
		if (n != null)		lastUsed = LocalDateTime.parse(n.getNodeValue());
		n = attributes.getNamedItem("useCount");
		if (n != null)		useCount = StringUtil.toInteger(n.getNodeValue());
	}

	public void addXML( XMLEventFactory  werk, List<XMLEvent> steps)
	{
		steps.add(werk.createStartElement( "", "", "Query"));
		steps.add(werk.createAttribute("name", 		getName()));
		steps.add(werk.createAttribute("raw", 		getRawQuery()));
		steps.add(werk.createAttribute("exploded", 	getExplodedQuery()));
		steps.add(werk.createAttribute("lastUsed", 	getLastUsed().toString()));
		steps.add(werk.createAttribute("useCount", 	"" + getUseCount()));
		steps.add(werk.createEndElement( "", "", "Query"));
	}
	
	//@formatter:off	
	public String getName()					{		return name.get(); 	}
	public StringProperty nameProperty()	{ 		return name;	}
	public void setRawQuery(String raw)		{		rawQuery = raw;	}
	public String getRawQuery()				{		return rawQuery;}
	public void setExplodedQuery(String q)	{		exploded = q;	}
	public String getExplodedQuery()		{		return exploded;}
	public LocalDateTime getLastUsed()		{		return lastUsed;}
	public int getUseCount()				{		return useCount;}
	public void incrementUseCount()			{		useCount++;}
	public void refreshLastUsed()			{		lastUsed = LocalDateTime.now();}
	public String toString()				{ 		return getName();	}
}
