package bookclub;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;

public class Gameboard extends Pane
{
	Color[] palette;
	
	public Gameboard(double w, double h, Color[] palette)
	{
	    double radius = (Math.min(w,  h) / 2) - 20;;
	    double centerX = w / 2;
	    double centerY = h / 2;
//	    double textRadius = radius-100;
	    Circle circl = new Circle(centerX, centerY, radius + 10);
	    circl.setFill(palette[5]);
	    getChildren().add(circl);
	    for (int i = 0; i < 30; i++)
	    {
	    	Arc arc = new Arc(centerX, centerY, radius, radius, 84 - (6 + i * 12), 12);
	    	arc.setType(ArcType.ROUND);
	    	arc.setId("" + i);
	    	arc.setFill( palette[i % 2 == 0 ? 0 : 1] );
	    	arc.setOnMouseEntered(ev ->   	{  arc.setFill(palette[2]);   	});	// TODO setStyleClass 
	    	arc.setOnMouseExited(ev -> 
	    	{   
    			int idx = Integer.parseInt(arc.getId());
	    		if (idx >= 0)
	    			arc.setFill(palette[idx % 2 == 0 ? 0 : 1] );    
	    	});
		    getChildren().add(arc);
	    }
	    for (int i = 0; i < 30; i++)
	    {
	    	double yOffset = 30;
	    	Label day = new Label("Day\n"+i);
	    	day.setStyle(cssBoldCenter18);	
	    	day.setLayoutX(centerX);
	    	day.setLayoutY(yOffset);
//	    	day.setTranslateZ(2);
	    	day.getTransforms().add(new Rotate( 3 + i * 12, 0, centerY-yOffset));
	    	day.setMouseTransparent(true);
		    getChildren().add(day);

	    }
	    radius -= 70;
	    circl = new Circle(centerX, centerY, radius + 10);
	    circl.setFill(palette[2]);
	    getChildren().add(circl);
	    for (int i = 0; i < 3; i++)
	    {
	    	Arc arc = new Arc(centerX, centerY, radius, radius, 90 + i * 120, 120);
	    	arc.setType(ArcType.ROUND);
	    	arc.setStroke(palette[3]);
	    	arc.setStrokeWidth(3);
	    	arc.setFill(palette[4]);
	    	arc.setOnMouseEntered(ev ->  {  arc.setFill(palette[2]);  	});					// TODO setStyleClass 
	    	arc.setOnMouseExited(ev -> 	 {  arc.setFill(palette[4]);	});
	    	getChildren().add(arc);
	   }
	    radius -= 170;
	    circl = new Circle(centerX, centerY, radius - 20);
	    circl.setFill(palette[1]);
	    getChildren().add(circl);
	    addActLabels(centerX, centerY);
	}

	static String cssBoldCenter18 = "-fx-text-alignment: center; -fx-font-weight: bold; -fx-font-size:  18";
	private void addActLabels(double x, double y)
	{
	    Label act1 = new Label("Act 1\nMorning");		act1.setStyle(cssBoldCenter18);	  act1.setTranslateX(x + 120);	act1.setTranslateY(y - 120);
	    Label act2 = new Label("Act 2\nDay");			act2.setStyle(cssBoldCenter18);	  act2.setTranslateX(x-15);		act2.setTranslateY(y + 120);
	    Label act3 = new Label("Act 3\nNight");			act3.setStyle(cssBoldCenter18);	  act3.setTranslateX(x - 180);	act3.setTranslateY(y - 120);
	    getChildren().addAll(act1, act2, act3);

		
	}
	
}
