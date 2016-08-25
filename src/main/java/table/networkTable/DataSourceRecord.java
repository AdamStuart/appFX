package table.networkTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import util.StringUtil;

public class DataSourceRecord {

	StringProperty name = new SimpleStringProperty();
	StringProperty system = new SimpleStringProperty();
	StringProperty site = new SimpleStringProperty();
	StringProperty usage = new SimpleStringProperty();
	StringProperty example = new SimpleStringProperty();
	StringProperty entity = new SimpleStringProperty();
	StringProperty exclusiveSpecies = new SimpleStringProperty();
//	StringProperty gravityStr = new SimpleStringProperty();
	StringProperty uri = new SimpleStringProperty();
	StringProperty idtype = new SimpleStringProperty();
	StringProperty pattern = new SimpleStringProperty();
	StringProperty fullname = new SimpleStringProperty();
//	StringProperty supportedSystemsStr = new SimpleStringProperty();
//	double gravity;

	public String toString() {  return getName() + " " + getExample(); }
	
	public String getName() 				{		return name.get();	}
	public void setName(String s) 			{		name.set(s);	}
	public StringProperty nameProperty()	{		return name;	}

	public String getSystem() 				{		return system.get();	}
	public void System(String s) 			{		system.set(s);	}
	public StringProperty systemProperty()	{		return system;	}

	public String getSite() 				{		return site.get();	}
	public void setSite(String s) 			{		site.set(s);	}
	public StringProperty siteProperty()	{		return site;	}

	public String getUsage() 				{		return usage.get();	}
	public void setUsage(String s) 			{		usage.set(s);	}
	public StringProperty usageProperty()	{		return usage;	}

	public String getExample() 				{		return example.get();	}
	public void setExample(String s) 		{		example.set(s);	}
	public StringProperty exampleProperty()	{		return example;	}

	public String getEntity() 				{		return entity.get();	}
	public void setEntity(String s) 		{		entity.set(s);	}
	public StringProperty entityProperty()	{		return entity;	}

	public String getExclusiveSpecies() 				{		return exclusiveSpecies.get();	}
	public void setExclusiveSpecies(String s) 			{		exclusiveSpecies.set(s);	}
	public StringProperty exclusiveSpeciesProperty()	{		return exclusiveSpecies;	}

//	public String getGravityStr() 				{		return gravityStr.get();	}
//	public void setGravityStr(String s) 		{		gravityStr.set(s);	}
//	public StringProperty gravityStrProperty()	{		return gravityStr;	}

	public String getUri() 					{		return uri.get();	}
	public void setUri(String s) 			{		uri.set(s);	}
	public StringProperty uriProperty()		{		return uri;	}

	public String getIdtype() 				{		return idtype.get();	}
	public void setIdtype(String s) 		{		idtype.set(s);	}
	public StringProperty idtypeProperty()	{		return idtype;	}

	public String getPattern() 				{		return pattern.get();	}
	public void setPattern(String s) 		{		pattern.set(s);	}
	public StringProperty patternProperty()	{		return pattern;	}

	public String getFullname() 			{		return fullname.get();	}
	public void setFullname(String s) 		{		fullname.set(s);	}
	public StringProperty fullnameProperty(){		return fullname;	}

//	public String getSupportedSystemsStr() 			{		return supportedSystemsStr.get();	}
//	public void setSupportedSystemsStr(String s) 		{		supportedSystemsStr.set(s);	}
//	public StringProperty supportedSystemsStrProperty(){		return supportedSystemsStr;	}
//
//	
	
	public DataSourceRecord(String inputLine)
	{
		StringProperty[] model = new StringProperty[] { name, system, site, usage, example, entity, exclusiveSpecies, uri, pattern, fullname};
		String[] flds = inputLine.split("\t");
		
		for (int i = 0; i < flds.length; i++)
			model[i].set(flds[i].trim());

//		try 
//		{
//			gravity = Double.parseDouble(gravityStr.get());
//		}
//		catch (NumberFormatException ex)
//		{
//			gravity = 0.;
//		}
	}

//	public double gravity() {		return gravity;	}

	public boolean patternMatch(String s)
	{
		if (getPattern().trim().length() == 0) return true;
		Pattern pat = Pattern.compile(getPattern());
		Matcher match = pat.matcher(s);
		boolean itsaMatch = match.matches();
		if (itsaMatch)
			System.out.println(getName()+ " [ " + getPattern() + " ] \t matches " + s);
		return itsaMatch;
	}

	public boolean speciesIncluded(String fullSpecies) {
		if (exclusiveSpecies.get().length() > 0)
				return fullSpecies.contains(exclusiveSpecies.get());
		return true;
	}
//	
//	// this is the map of all the target systems this source could write to
//	public void checkSupportMap(String speciesShort, BridgeDbController ctrol) {
//		String str = getSupportedSystemsStr();
//		if (StringUtil.isEmpty(str))
//		{
//			String supported = ctrol.targetsForRec(this, speciesShort);
//			setSupportedSystemsStr(supported);
//		}
//	}
//
//	public boolean anySupportedSystems() {
//		String val = getSupportedSystemsStr();
//		if (val == null) return false;
//		return	val.trim().length() > 0;  
//	}

}