package plate;
import model.AttributeValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.Light;
import javafx.scene.effect.LightingBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Well extends Rectangle
{
	private int row;
	private int column;

	private ObservableList<AttributeValue> attributes;

	public Well(int r, int c)
	{
		row = r;
		column = c;
		attributes = FXCollections.observableArrayList();
		attributes.add(new AttributeValue("circleColor", "BISQUE"));
		attributes.add(new AttributeValue("circleWeight", "1.0"));
		attributes.add(new AttributeValue("circleSize", "0.8"));
		setStyle("-fx-background-color: burlywood");
	    Light.Distant light = new Light.Distant();
	    light.setAzimuth(-135);
	    light.setElevation(30);
	    setFill(Color.MAROON);
	    setWidth(100); setHeight(100);
//		    setOnMouseEntered(System.out.println(getDescriptor()));
	    
//		    setPrefSize(200, 200);	
	    }
	//---------------------------------------------------------------------------------------------------
	
	public String getDescriptor()
	{
		char c = (char) ('A' + row);
		String colStr = ("0" + (1 + column));
		int len = colStr.length();
		if (len>2) 
			colStr = colStr.substring(1,3);
		return c + colStr;	
	}

	public void computePrefWidth()
	{

	}

	public void draw()
	{
		System.out.println("draw: " + getDescriptor());
	}
//---------------------------------------------------------------------------------------------------
	final static public String wellNotation96(int i)
	{
		return padTo3("" + (char) ('A' + (i / 12)) + (int) (1 + (i % 12)));
	}

	final static public String wellNotation96(int i,int j)
	{
		return padTo3("" + (char) ('A' + (i / 12)) + (int) (1 + (j % 12)));
	}

	final static public int wellToInt96(String s)
	{
		return (int) (s.charAt(0) - 'A') * 12 + Integer.parseInt(s.substring(1));
	}

	final static private String padTo3(String s)
	{
		return (s.length() == 3 ? s : "" + s.charAt(0) + '0' + s.charAt(1));
	}

}
	
