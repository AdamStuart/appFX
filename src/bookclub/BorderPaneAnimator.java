package bookclub;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class BorderPaneAnimator
{
	Animation hider, shower;
	double expandedWidth = 200;

	
// TODO have a hover animation to collapse/expand if no button is sent in
	public BorderPaneAnimator(BorderPane pane, Button togl, Side onSide, boolean showText, double expWidth)
	{
		togl.setOnAction(actionEvent ->      {	toggleSidebar(pane, onSide);    });
		boolean isBottom = (onSide == Side.BOTTOM);
		boolean isLeft = (onSide == Side.LEFT);
	    Region sidebar = (Region)  ((isBottom) ? pane.getBottom() :   	(isLeft ? pane.getLeft() : pane.getRight()));
	    if (expWidth > 0) 
	    	expandedWidth = expWidth;

		hider = new Transition() {
        { setCycleDuration(Duration.millis(250)); }
        
        protected void interpolate(double frac) 
        {
          final double curWidth = expandedWidth * (1.0 - frac);
//          System.out.println("" + curWidth);
          if (isBottom)
          {
        	  sidebar.setTranslateY( expandedWidth - curWidth );
              sidebar.setPrefHeight(curWidth);
              sidebar.setMaxHeight(curWidth);
              sidebar.setMinHeight(curWidth);
         } else
        	  
         {    if (isLeft)
        	  sidebar.setTranslateX( curWidth - expandedWidth);
	         sidebar.setPrefWidth(curWidth);
	         sidebar.setMaxWidth(curWidth);
	         sidebar.setMinWidth(curWidth);
        }
//          center.setPrefWidth(centerWidth + expandedWidth - curWidth );
        }
      };
      hider.onFinishedProperty().set(actionEvent -> {
        	  sidebar.setVisible(false);
        	  if (showText) togl.setText("Show");
        	  togl.getStyleClass().remove(isBottom ? "hide-bottom" : (isLeft ? "hide-left" : "hide-right"));
        	  togl.getStyleClass().add(isBottom ? "hide-bottom" : (isLeft ? "show-right": "show-left"));
        });
      // create an animation to show a sidebar.
      shower = new Transition() {
        { setCycleDuration(Duration.millis(250)); }
        protected void interpolate(double frac) {
          final double curWidth = expandedWidth * frac;
          
          if (isBottom)
          {
        	  sidebar.setTranslateY( expandedWidth - curWidth );
              sidebar.setPrefHeight(curWidth);
              sidebar.setMaxHeight(curWidth);
              sidebar.setMinHeight(curWidth);
         } else
         {  
          sidebar.setPrefWidth(curWidth);  
          sidebar.setMaxWidth(curWidth); 
          sidebar.setMinWidth(curWidth);
          if (isLeft)      	  sidebar.setTranslateX( curWidth - expandedWidth );
         }
        }
      };
      shower.onFinishedProperty().set(actionEvent ->{
            if (isBottom)   sidebar.setTranslateY(0);
            else 			sidebar.setTranslateX(0);
        	if (showText) togl.setText("Collapse");
        	togl.getStyleClass().add(isBottom ? "hide-bottom" : (isLeft ? "hide-left" : "hide-right"));
        	togl.getStyleClass().remove(isBottom ? "show-bottom" : (isLeft ? "show-right": "show-left"));
      });			
	}

	public void toggleSidebar(BorderPane pane, Side onSide )
	{
		boolean isBottom = (onSide == Side.BOTTOM);
		Region sidebar;
		
		if (isBottom)
			sidebar = (Region) pane.getBottom();
		else
		{
			boolean isLeft = (onSide == Side.LEFT);
			sidebar = (Region) (isLeft ? pane.getLeft() : pane.getRight());
			if (sidebar == null)
				sidebar = (Region) pane.getRight();
		}
		if (shower.statusProperty().get() == Animation.Status.STOPPED
				&& hider.statusProperty().get() == Animation.Status.STOPPED)
		{
			if (sidebar.isVisible())
				hider.play();
			else
			{
				sidebar.setVisible(true);
				shower.play();
			}
		}
	}

}
