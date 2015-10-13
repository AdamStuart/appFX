package table.binder;

import java.time.format.DateTimeFormatter;

import diagrams.grapheditor.GConnectionSkin;
import diagrams.grapheditor.model.GConnection;
import gui.Backgrounds;
import gui.Borders;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.NodeUtil;

// this isn't a control, but a collection of nodes that need coordination
/*
 *   *  {@link CornerDragBox}.
     *
     * @param a BindingsController and 4 labels
 	* CornerDragBox is a StackPane that aligns a rectangle with 4 Labels
 */
public class CornerDragBox extends StackPane
{
	BindingsController controller;
	boolean verbose = false;
	double viewToModelRatio = 1.;
	Label widthLabel, heightLabel, areaLabel, scaleLabel;
	
	public CornerDragBox(BindingsController c, Label w, Label h, Label ar, Label scle)
	{
		super();
		controller = c;
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
        setBorder(Borders.dashedBorder);
         StackPane.setAlignment(handle, Pos.BOTTOM_RIGHT);
	}
	double MAX_RECT_WIDTH = 140;
	double MAX_RECT_HEIGHT = 140;
	
	//------------------------------------------------------------------------------
	//  OLD SCHOOL:  can this be replaced with bindings?
	
	public void install(Rect r)
	{
//		String colorStr; //  = "#" + r.getColor().toString().substring(2, 8);
		Color col = r.getColor();
//		double red = col.getRed();
//		double gr = col.getGreen();
//		double bl = col.getBlue();
			
//		colorStr = String.format("#%x%x%x", (int)(255 * red), (int)(255 * gr), (int)(255 * bl));
//        setStyle("-fx-background-color: " + colorStr + ";");
//		if (verbose)
//			System.out.println("rect color = " + colorStr);
		Background bgnd = Backgrounds.coloredBackground(col);
        setBackground(bgnd);

        widthLabel.setText(r.getWidthAndUnits());
		heightLabel.setText(r.getHeightAndUnits());
		areaLabel.setText(r.getAreaAndUnits());
		if (verbose)		System.out.println(r.toString());

		double w = r.getWidthInMeters();
		double h = r.getHeightInMeters();
		
		double scaleW = MAX_RECT_WIDTH / w;
		double scaleH = MAX_RECT_HEIGHT / h;
		double scale  = Math.min(scaleW, scaleH);
		
		NodeUtil.forceWidth(this, (int)(w * scale));  
		NodeUtil.forceHeight(this, (int)(h * scale));  
		
		String dat = DateTimeFormatter.ISO_DATE.format(r.getDueDate());
		scaleLabel.setText(String.format("Scale: %.1f,   Due Date: %s", scale, dat));
	}
	
	//------------------------------------------------------------------------------
	// Display rect mouse handlers
	//------------------------------------------------------------------------------
	private void addRectHandlers()
	{
		setOnMousePressed(ev -> {
			Rect active = controller.getActiveRecord();
			double modelWidth = active.getWidthInMeters();
			viewToModelRatio = getWidth()  / modelWidth;		// getWidthInInches
		});
		setOnMouseDragged(ev -> doDrag(ev));
		setOnMouseReleased(ev -> {});
	}
	
	static int MAXWidth = 140;
	double pin(double x, double floor, double top)	{		return Math.min(Math.max(floor, x), top);	}
	
	private void doDrag(MouseEvent e)
	{
		if (verbose) System.out.println("doDrag: " + e.getX() + ", " + e.getY());

		double x = pin(e.getX(), 1, MAXWidth);
		double y = pin(e.getY(), 1, MAXWidth);;
		NodeUtil.forceWidth(this, (int)x);  
		NodeUtil.forceHeight(this, (int) y);  

		Rect active = controller.getActiveRecord();
		double wid = Math.max(0.0001, x) / viewToModelRatio;
		active.setWidthInMeters(wid );
		double ht = Math.max(0.0001,y) / viewToModelRatio;		// don't let width or height go negative
		active.setHeightInMeters(ht);	
		active.recalcArea();
		controller.setActiveRecord(active);
		
	}

}
