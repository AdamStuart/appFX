package chart.flexiPie;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;

public class Wedge {
	
	private	StringProperty name;
	private	DoubleProperty length;
	private	SimpleBooleanProperty locked;
	private	Color color;
	private	Arc arc;
//	private	PieModel model;
	private int index;
	
	public Wedge(String s, Color c, double d, PieModel parent, int idx)
	{
		name = new SimpleStringProperty(s);
		color = c;
		length = new SimpleDoubleProperty(d);			// corresponds to the length of the arc
//		model = parent;
		index = idx;
		locked = new SimpleBooleanProperty(false);
	}
	public StringProperty nameProperty()	{ 	return name;	}
	public DoubleProperty lengthProperty()	{ 	return length;	}  
	public String getName() 				{	return name.getValue();	}
	public double getLength() 				{	return length.getValue();	}
	public void setStartAngle(double a) 	{	 arc.setStartAngle(a); 	}
	public void setLength(double a) 		{	 length.setValue(a);	 		getArc().setLength(a);}
	public void deltaLength(double da) 		{	 setLength(getLength() + da);  	}
	public Color getColor() 				{	return color;	}
	public void setArc(Arc a) 				{	arc = a;  }
	public Arc getArc() 					{	return arc;  }

	public void lock() 						{	locked.set(true);  }			
	public void unlock() 					{	locked.set(false);  }	
	public boolean isLocked() 				{	return locked.get();  }			// PieModel.redistribute checks if a wedge's length is locked

	public int getIndex() 					{	return index;  }		// redundant, but useful to iterate around the circle
	public void setIndex(int i) 			{	index = i;  }				// be able to change index for deletion & insertion

	
	public Point2D getStartPoint(Point2D center) 
	{	
		System.out.println(String.format("angle %.2f", arc.getStartAngle()));
		return pointAtAngle(center, arc.getStartAngle());
	}
	
	public Point2D pointAtAngle( Point2D center, double angle)
	{
		double scalar = 1.1* arc.getRadiusX();		// assumes circle and 10% extension outside the circumference
		angle *= Math.PI / 180;
		return new Point2D(center.getX() + (Math.cos(angle)* scalar),center.getY() - (Math.sin(angle)* scalar));	
	}
	
	public Point2D getEndPoint(Point2D center) 
	{	
		return pointAtAngle(center, arc.getStartAngle()+ getLength());
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
