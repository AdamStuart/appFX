package container;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import model.KeyValue;
import util.StringUtil;

public class Onterm
{
	private SimpleStringProperty id = new SimpleStringProperty();
	private SimpleStringProperty name = new SimpleStringProperty();
	private SimpleStringProperty def = new SimpleStringProperty();
	private List<KeyValue> properties = new ArrayList<KeyValue>();
	private List<Onterm> children = new ArrayList<Onterm>();
	
	public Onterm(String idtag)
	{
		id.set(idtag);
	}
	
	public String getId() { return id.get();	}
	public String getName() { return name.get();	}
	public String getDef() { return def.get();	}
	public List<Onterm> getChildren() { return children;	} 
	public void addChild(Onterm child) {  children.add(child);	} 
	
	public void setName(String s)	{ name.set(s);	}
	public void setDef(String s)	{ def.set(StringUtil.clearQuotes(s));	}
	public SimpleStringProperty idProperty() { return id;	}
	public SimpleStringProperty nameProperty() { return name;	}
	public SimpleStringProperty defProperty() { return def;	}
	
	public void addProperty(String line)			{ properties.add(new KeyValue(line));	}
	public void addProperty(String key, String v)	{ properties.add(new KeyValue(key,v));	}

	public void add(String line)
	{
		if (line.startsWith("name:")) setName(line.substring(6));
		else if (line.startsWith("def:")) setDef(line.substring(5));
		else addProperty(line);
	}
	public String toString()	{		return getId() + ": " + getName();	}
	public void dump()	{		System.out.println(toString());			}

	public List<KeyValue> getProperties(String attr)
	{
		return properties.stream()
			.filter((keyVal) -> keyVal.getAttribute().equals(attr))
			.collect(Collectors.toList());
	}

	static String SEP = ": \t";
	static String EOL = "\n";
	public String getDescriptor()
	{
		return toString() + EOL + EOL + StringUtil.asciiWrap(getDef(), 45) + 
			properties.stream()
			.map(p -> getKVString(p))
			.collect(Collectors.joining());
		
	}
	static String getKVString(KeyValue kv)	{	return kv.getAttribute() + SEP + kv.getValue() + EOL;	}

}
