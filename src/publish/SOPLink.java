package publish;

import javafx.beans.property.SimpleStringProperty;

public class SOPLink
{
	SimpleStringProperty	url = new SimpleStringProperty(); 
	SimpleStringProperty	description = new SimpleStringProperty(); 
	SimpleStringProperty	abstractText = new SimpleStringProperty(); 
	SimpleStringProperty	contentText = new SimpleStringProperty(); 
	
	SimpleStringProperty urlProperty()			{ return url;	}
	SimpleStringProperty descriptionProperty()	{ return description;	}
	SimpleStringProperty abstractTextProperty()	{ return abstractText;	}
	SimpleStringProperty contentTextProperty()	{ return contentText;	}
	
	public void setUrl(String s)			{	url.set(s);}
	public String getUrl()					{	return url.get();}
	public void setDescription(String s)	{	description.set(s);}
	public String getDescription()			{	return description.get();}
	public void setAbstractText(String s)	{	abstractText.set(s);}
	public String getAbstractText()			{	return abstractText.get();}
	public void setContentText(String s)	{	contentText.set(s);}
	public String getcontentText()			{	return contentText.get();}

	
	public SOPLink(String u, String d)
	{
		setUrl(u);
		setDescription(d);
	}
	public SOPLink(String u, String d, String ab, String text)
	{
		this(u,d);
		setAbstractText(ab);
		setContentText(text);
	}
	
	public String toString()	{ 	return getDescription();	}
}
