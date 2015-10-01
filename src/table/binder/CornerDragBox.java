package table.binder;

import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// this isn't a control, but a collection of nodes that need coordination

public class CornerDragBox extends StackPane
{
	BindingsController controller;
	boolean verbose = false;
	double viewToModelRatio = 1.;
	Label widthLabel, heightLabel, areaLabel, scaleLabel;
	
	public CornerDragBox(BindingsController bindingsController, Label w, Label h, Label ar, Label scle)
	{
		super();
		controller = bindingsController;
		widthLabel = w;
		heightLabel = h; 
		areaLabel = ar; 
		scaleLabel = scle;
        addRectHandlers();
        setStyle("-fx-background-color: blue;");

        getChildren().add(areaLabel);
        StackPane.setAlignment(ar, Pos.CENTER);
        
        Rectangle handle = new Rectangle(12,12);  
        handle.setStyle("-fx-fill:white;");
        getChildren().add(handle);
        
         StackPane.setAlignment(handle, Pos.BOTTOM_RIGHT);
	}
	double MAX_RECT_WIDTH = 140;
	double MAX_RECT_HEIGHT = 140;
	
	//------------------------------------------------------------------------------
	public void install(Rect r, double scale)
	{
		String colorStr; //  = "#" + r.getColor().toString().substring(2, 8);
		Color col = r.getColor();
		double red = col.getRed();
		double gr = col.getGreen();
		double bl = col.getBlue();
			
		colorStr = String.format("#%x%x%x", (int)(255 * red), (int)(255 * gr), (int)(255 * bl));
        setStyle("-fx-background-color: " + colorStr + ";");
		if (verbose)
			System.out.println("rect color = " + colorStr);
        backgroundProperty().bind(new SimpleObjectProperty(new Background(new BackgroundFill(col, CornerRadii.EMPTY, Insets.EMPTY))));  ///setBackground(new Background(col));
		String s = r.getWidthAndUnits();

		
		if (verbose)	System.out.println(s);
		widthLabel.setText(s);
		s = r.getHeightAndUnits();
		if (verbose)		System.out.println(s);
		heightLabel.setText(s);
		s = r.getAreaAndUnits();
		if (verbose)		System.out.println(s);
		areaLabel.setText(s);
		double w = r.getWidthInMeters();
		double h = r.getHeightInMeters();
		
		if (scale <= 0)
		{
			double scaleW = MAX_RECT_WIDTH / w;
			double scaleH = MAX_RECT_HEIGHT / h;
	
			scale  = Math.min(scaleW, scaleH);
		}
		String dat = DateTimeFormatter.ISO_DATE.format(r.getDueDate());
		scaleLabel.setText(String.format("%.3f Due: %s", scale, dat));
		NodeUtil.forceWidth(this, (int)(w * scale));   //setWidth(w * scale);
//		setHeight(h * scale);
		NodeUtil.forceHeight(this, (int)(h * scale));   //setWidth(w * scale);
//		setLayoutY(MAX_RECT_HEIGHT - h * scale);
	}
	
	//------------------------------------------------------------------------------
	// Display rect mouse handlers
	//------------------------------------------------------------------------------
	private void addRectHandlers()
	{
		setOnMousePressed(ev -> startDrag(ev));
		setOnMouseDragged(ev -> doDrag(ev));
		setOnMouseReleased(ev -> endDrag(ev));
	}
	
//	double rectLeft, rectRight, rectTop, rectBottom;
	private void startDrag(MouseEvent e)
	{
		if (verbose) System.out.println("dragCorner");
		Rect active = controller.getActiveRecord();
		double modelWidth = active.getWidthInMeters();
		viewToModelRatio = getWidth()  / modelWidth;		// getWidthInInches
	}
	
	static int MAXWidth = 140;
	double pin(double x, double floor, double top)
	{
		return Math.min(Math.max(floor, x), top);
	}
	
	private void doDrag(MouseEvent e)
	{
		if (verbose) System.out.println("doDrag: " + e.getX() + ", " + e.getY());

		double x = pin(e.getX(), 1, MAXWidth);
		double y = pin(e.getY(), 1, MAXWidth);;
		NodeUtil.forceWidth(this, (int)x);   //setWidth(w * scale);
		NodeUtil.forceHeight(this, (int) y);   //setWidth(w * scale);

		Rect active = controller.getActiveRecord();
		double wid = Math.max(0.0001, x) / viewToModelRatio;
		active.setWidthInMeters(wid );
		double ht = Math.max(0.0001,y) / viewToModelRatio;
		active.setHeightInMeters(ht);	// don't let height go negative
		active.recalcArea();
		controller.install(viewToModelRatio);
		
	}
	private void endDrag(MouseEvent e)
	{
		
		if (verbose)		System.out.println("rectScale.setValue");
//		rectScale = 1;			// TODO
	}

}
