package plate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.effect.Light;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.AttributeValue;

public class Well extends Group
{
	private int row;
	private int column;
	Rectangle rect;
	Circle cir;
	
	private ObservableList<AttributeValue> attributes;

	public Well(int r, int c)
	{
		row = r;
		column = c;
		rect = new Rectangle(12, 12, 80,80);
	    rect.setFill(Color.MAROON);
	    cir = new Circle(24, 24, 10);
	    cir.setFill(Color.AQUAMARINE);
	    getChildren().addAll(rect, cir);
	    
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
	
