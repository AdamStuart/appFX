package bookclub;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.collections.FXCollections;
import util.StringUtil;


public class Story
{

	int month;
	long id;
	String name;
	LocalDate updatedDate;
	LocalDate createdDate;
	

	public static List<Story> readStoryList()
	{
		List<Story> stories  = FXCollections.observableArrayList();
		boolean verbose = true;
		String jsonString = StringUtil.callURL("http://secret-world.herokuapp.com/story_worlds");
		if (verbose) System.out.println("\n\nraw String: " + jsonString);
	
		try {  
			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonString);  
			
			for(int i=0; i<jsonArray.size(); i++)
			{
				JSONObject elem = (JSONObject) jsonArray.get(i);
				Story story = new Story(elem);
				stories.add(story);
			}
		} 
		catch (Exception e) {		e.printStackTrace();	}
		return stories;
	}
	//---------------------------------------------------------------------
	
	public Story(JSONObject elem)
	{
		try {
		month = Integer.parseInt("" + elem.get("month"));
		id = Integer.parseInt("" +elem.get("id"));
		name = (String) elem.get("name");
		String update = (String) elem.get("updated_at");
		updatedDate = LocalDate.parse(update, DateTimeFormatter.ISO_DATE_TIME);
		String created = (String) elem.get("created_at");
		createdDate = LocalDate.parse(created, DateTimeFormatter.ISO_DATE_TIME);
		}
		catch (Exception e)	{  System.err.println("Parsing Error in Story");  e.printStackTrace(); }
	}
	
	public long getId()		{ return id;	}
	public int getMonth()	{ return month;	}
	public String getCreatedDate()	{ return createdDate.toString();	}
	public String getUpdatedDate()	{ return updatedDate.toString();	}
}
