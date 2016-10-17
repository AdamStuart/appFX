package chart.flexiPie;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;

public class Wedge
 {
	private	StringProperty name;
	private	DoubleProperty length;
	private	DoubleProperty scaled;
	private	SimpleBooleanProperty locked;
	private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<Color>();
	private	Arc arc;
	private int index;
	
	public Wedge(String s, Color c, double d, PieModel parent, int idx, DoubleProperty pieSize)
	{
		name = new SimpleStringProperty(s);
		color.set(c);
		length = new SimpleDoubleProperty(d);			// arc in degrees
		scaled = new SimpleDoubleProperty(d);			// length * size
		scaled.bind(pieSize.multiply(length));
		index = idx;
		locked = new SimpleBooleanProperty(false);
	}
	public StringProperty nameProperty()	{ 	return name;	}
	public DoubleProperty lengthProperty()	{ 	return length;	}  
	public DoubleProperty scaledProperty()	{ 	return scaled;	}  
	public String getName() 				{	return name.getValue();	}
	public SimpleObjectProperty colorProperty()	{	return color;	}
	public Color getColor() 				{	return color.get();	}
	public void setColor(Color c) 			{	 color.set(c);	}
	public Arc getArc() 					{	return arc;  }
	public double getLength() 				{	return length.getValue();	}
	public double getScaled() 				{	return scaled.getValue();	}

	public void setLength(double a) 		{	length.setValue(a);	 		getArc().setLength(a);}
//	public void setScaled(double a) 		{	scaled.setValue(a);	}
	public void deltaLength(double da) 		{	setLength(getLength() + da);  	}
	public void setStartAngle(double a) 	{	arc.setStartAngle(a); 	}
	public void setArc(Arc a) 				{	arc = a;  }

	public void lock() 						{	locked.set(true);  }			
	public void unlock() 					{	locked.set(false);  }	
	public boolean isLocked() 				{	return locked.get();  }			// PieModel.redistribute checks if a wedge's length is locked

	public int getIndex() 					{	return index;  }			// redundant, but useful to iterate around the circle
	public void setIndex(int i) 			{	index = i;  }				// be able to change index for deletion & insertion

	
	public Point2D getStartPoint(Point2D center) {	return pointAtAngle(center, arc.getStartAngle());	}
	public Point2D getEndPoint(Point2D center) 	{	return pointAtAngle(center, arc.getStartAngle()+ getLength());	}

	public Point2D pointAtAngle( Point2D center, double angle)
	{
		double scalar = 1.1 * arc.getRadiusX();		// assumes circle and 10% extension outside the circumference
		angle *= Math.PI / 180;
		return new Point2D(center.getX() + (Math.cos(angle)* scalar),center.getY() - (Math.sin(angle)* scalar));	
	}

	public void setAngles(double start, double end) 
	{
		setStartAngle(start);
		double len = end-start;
		while (len > 360) len -= 360;
		while (len < 0) len += 360;
		setLength(len);
	}
}
