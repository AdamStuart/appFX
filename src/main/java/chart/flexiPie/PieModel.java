package chart.flexiPie;

import java.lang.reflect.Field;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;


public class PieModel 
{
	ObservableList<Wedge> wedgeList;
	double centerX, centerY, radiusX, radiusY;
	FlexiPieController controller;
	final Point2D center;

	private Circle handle;
	private Line connection;
	double xMouse,yMouse;
	boolean verbose = false;
	DoubleProperty pieSize = new SimpleDoubleProperty(800);
	//---------------------------------------------------------------------------------
	public PieModel(double cX, double cY, double rX, double rY, FlexiPieController parent)
	{
		centerX = cX;
		centerY = cY;
		radiusX = rX;
		radiusY = rY;
		center = new Point2D(centerX, centerY);
		controller = parent;
		double total = 0;
		wedgeList = FXCollections.observableArrayList();
		int ct = names.length;

		for (int i =0; i< ct; i++)		total += sizes[i];			//sizes.stream().collect();
		pieSize.set(total);

		for (int i =0; i< ct; i++)
		{
			Color color = colors[i];
			double portion = 360. * sizes[i] / total;
			wedgeList.add(new Wedge(names[i], color, portion, this, i, pieSize));

		}
	}
	
	String[] names = new String[]{"Eva", "AlexP", "Kristina", "AlexW", "Sean", "Stacia", "Reuben", "Justin", "Anders", "Adam"};
	Color[] colors = new Color[]{Color.GREEN, Color.BLUE, Color.BROWN, Color.PURPLE,  Color.GOLDENROD,  Color.MEDIUMORCHID,  Color.BEIGE, Color.CYAN,  Color.FIREBRICK, Color.AQUAMARINE};
	double[] sizes = new double[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
	
	//---------------------------------------------------------------------------------
	public Wedge find(String s)
	{
		for (Wedge w : wedgeList)
			if (w.getName().equals(s))
				return w;
		return null;
	}
	
	public Wedge findByPoint(double x, double y) 
	{
		double dist = distanceFromCenter(x, y);
		if (dist < radiusX)
		{
			double dx =  x - centerX  ;
			double dy = y - centerY;
			int quadrant = (dy > 0 ? 2 : 0) +  (dx < 0 ? 1 : 0);
			double angle = (90 * quadrant) + Math.atan(Math.abs(dy / dx)) * (180 / Math.PI);
			System.out.println(dist + ", " + angle);
			double start = 0;
			for (Wedge w : wedgeList)
			{
				if (start <= angle && (start + w.getLength() > angle))
					return w;
				start += w.getLength();
			}
		}
		return null;
	}
	//---------------------------------------------------------------------------------
	public Group createHandle() 
	{

	    handle = new Circle();
	    handle.setRadius(5);    
	    handle.setStroke(Color.BLACK);
	    handle.setStrokeWidth(2.5);
	    handle.setFill(Color.BROWN);

	    connection = new Line();
	    connection.setStrokeWidth(4);
	    connection.setStartX(centerX);
	    connection.setStartY(centerY);
	    connection.endXProperty().bind(handle.centerXProperty());
	    connection.endYProperty().bind(handle.centerYProperty());

	    handle.setOnMouseDragged(event -> {
	            xMouse = event.getX() - centerX;
	            yMouse = event.getY() - centerY;
	            double angleInRadians = Math.atan2(-yMouse, xMouse);
	            setHandlePosition(angleInRadians);
	    });
	    return new Group(connection, handle);
	}
	
	//---------------------------------------------------------------------------------
	void setHandlePosition(double a)
	{
		if (activeWedge != null)
		{
			double oldAngle = activeWedge.getArc().getStartAngle();
			double oldEnd = oldAngle + activeWedge.getArc().getLength();

			a *= 180 / Math.PI;		// a comes in radians, but we work in degrees
	        
	        double tmpDelta = a - oldAngle;
	        double delta = boundDeltaAngle(tmpDelta);
	        boolean flipped = delta != tmpDelta;
	        if (verbose) System.err.println(String.format("Delta: %.2f a: %.2f oldAngle: %.2f oldEnd: %.2f ", delta, a, oldAngle, oldEnd));
	        
			if (!flipped && delta > 0 && a > oldEnd)
	        	a = oldEnd;
	        if (!flipped && delta < 0 && (a + 359) < oldEnd)			//1 degree buffer to prevent loss of wedge proportions
	        	a = oldEnd - 359;
	        
	        delta = boundDeltaAngle(oldAngle - a);				//recalculate delta with bounded a
	        
			controller.setLabel(a);
			Point2D pt = activeWedge.pointAtAngle(center, a);
	        handle.setCenterX(pt.getX());
	        handle.setCenterY(pt.getY());	
	        activeWedge.setAngles(a, oldEnd);
	        redistribute(delta);

	        double start = a;
	        for (int i = activeWedge.getIndex()-1; i>= 0; i--)
	        {
	        	Wedge w = wedgeList.get(i);
	        	start -= w.getLength();
	        	while (start <  0) start += 360;
	        	w.setStartAngle(start);
	        }

	        start = oldEnd;
	        for (int i = activeWedge.getIndex()+1; i<wedgeList.size(); i++)
	        {
	        	Wedge w = wedgeList.get(i);
	        	w.setStartAngle(start);
	        	start += w.getLength();
	        	while (start > 360) start -= 360;
	        }
	    }
	}
	
	//---------------------------------------------------------------------------------
	protected double boundDeltaAngle(double delta)
	{
		if (Math.abs(delta) > 10)	   
		{
			while (delta > 180) delta -= 360;
			while (delta < -180) delta += 360;
//			System.err.println(String.format("Delta: %.2f a: %.2f oldAngle: %.2f oldEnd: %.2f ", delta, a, oldAngle, oldEnd));
		}
		return delta;
	}

	//---------------------------------------------------------------------------------
	private int nUnlocked()
	{
		int ct = 0;
		for (Wedge w : wedgeList)
			if (!w.isLocked()) ct++;
		return ct;
	}
	//---------------------------------------------------------------------------------
	enum Distribution	{		EQUAL, 		PROPORTIONAL	}
	
	Distribution distrib = Distribution.PROPORTIONAL;
	
	private void redistribute(double delta) 
	{
       if (distrib ==  Distribution.EQUAL)
       {
    	   double nOthers = nUnlocked() - 1;	
           for (Wedge w: wedgeList)
           	if (w != activeWedge && !w.isLocked())
           		w.deltaLength(-delta / nOthers);
   	  	}
       
       if (distrib ==  Distribution.PROPORTIONAL)
       {
    	   double total = 0;
    	   for (Wedge w: wedgeList)
              	if (w != activeWedge && !w.isLocked())
              		total += w.getLength();
    	   
           for (Wedge w: wedgeList)
           {
           		double weight = w.getLength() / total;
           		if (w != activeWedge && !w.isLocked())
           			w.deltaLength(-delta * weight);
           }
   	  }
	}

	//---------------------------------------------------------------------------------
	Point2D getStartHandlePoint(Wedge inWedge) 				{	return inWedge.getStartPoint(center);	}
	private double distanceFromCenter(double x, double y) 	{	return center.distance(x, y);	}
	
	public Group buildPie()
	{
		Group g = new Group();
		double startAngle = 0;
		
		for (Wedge w : wedgeList)
		{
			double len = w.getLength();
			Arc arc = new Arc(centerX, centerY,  radiusX, radiusY,  startAngle, len);
			arc.setFill(w.getColor());			// TODO use CSS styles
			
			arc.setStroke(Color.BLACK);
			arc.setStrokeWidth(1);
			arc.setType(ArcType.ROUND);           
			arc.getStyleClass().setAll("chart-pie", "data" + w.getIndex(),
                            "default-color" + w.getIndex() % 8);

			startAngle += len;
			arc.setOnMouseClicked(new EventHandler<MouseEvent>() {		@Override public void handle(MouseEvent event) {	select(w);	}	});
			Tooltip tip = new Tooltip("");
			Tooltip.install(arc, tip);
	        tip.setText(w.getName());
	        hackTooltipStartTiming(tip);
			g.getChildren().add(arc);
			w.setArc(arc);
		}
		return g;

	}
	// http://stackoverflow.com/questions/26854301/control-javafx-tooltip-delay/27739605#27739605
	public static void hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);

	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(10)));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}	
	//---------------------------------------------------------------------------------
	private Wedge activeWedge;

	public void select(Wedge wedge) 
	{
		for (Wedge w : wedgeList)
			w.getArc().setStrokeWidth(1);
		activeWedge = wedge;
		activeWedge.getArc().setStrokeWidth(5);
		Point2D pt = getStartHandlePoint( wedge);
		handle.setCenterX( pt.getX() );
		handle.setCenterY( pt.getY() );
	}
	//---------------------------------------------------------------------------------
	public void select(int i) 
	{
		if (i >= 0 && i < wedgeList.size())
			select(wedgeList.get(i));
		
	}
	//---------------------------------------------------------------------------------
//	public void grabPalette(PieChart other) 
//	{
//		if (other == null) return;
//		int i=0;
//		for (Wedge w : wedgeList)
//		{
//			Node nod = other.getData().get(i++).getNode();
//			
//			w.getArc().setFill(Color.BURLYWOOD);;
//		}
//	}
	//---------------------------------------------------------------------------------
	public TreeItem<Wedge> createTreeItems() 
	{
		TreeItem<Wedge> root = new TreeItem<Wedge>(new Wedge("Bioinformatics Core", Color.WHITE, 360, null, -1, pieSize));
		for (Wedge w : wedgeList)
			root.getChildren().add(new TreeItem<Wedge>(w));
		return root;
	}
//=====================================================================


}
