package bookclub;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.StringUtil;

public class Player
{

	long id;
	String name;		// unused
	String email;
	LocalDate updatedDate;
	LocalDate createdDate;
	
	public Player(JSONObject elem)
	{
		try {
		id = Integer.parseInt("" +elem.get("id"));
		email = (String) elem.get("email");
		String update = (String) elem.get("updated_at");
		updatedDate = LocalDate.parse(update, DateTimeFormatter.ISO_DATE_TIME);
		String created = (String) elem.get("created_at");
		createdDate = LocalDate.parse(created, DateTimeFormatter.ISO_DATE_TIME);
		}
		catch (Exception e)	{  System.err.println("Parsing Error in Player");  e.printStackTrace(); }
	}
	
	
	public static Player readPlayerInfo(long id)
	{
		boolean verbose = true;
		String jsonString = StringUtil.callURL("http://secret-world.herokuapp.com/users/" + id);
		if (verbose) System.out.println("\n\nPlayer: " + jsonString + id);

		try {  
			JSONParser jsonParser = new JSONParser();
			JSONObject json = (JSONObject) jsonParser.parse(jsonString);  
			Player player = new Player(json);
			if (verbose) System.out.println("Player: " + player.getId() + " has email: " + player.getEmail());
			return player;
			}
		catch (Exception e) {		e.printStackTrace();	}
		return null;
		
	}

	
	public long getId()				{ return id;	}
	public String getEmail()		{ return email;	}
	public String getCreatedDate()	{ return createdDate.toString();	}
	public String getUpdatedDate()	{ return updatedDate.toString();	}

}
