package image.edit;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class IEToolManager implements EventHandler<MouseEvent> {
	
	private 	   IETool 		 curTool;
	private 	   Color  		 curColor;
	
	public IEToolManager()
	{
		curColor = Color.BLACK;
		curTool  = new IEPencil();
	}

	public void setCurTool(IETool newTool)
	{
		curTool = newTool;
	}
	
	public void setCurColor(Color c)
	{
		curColor = c;
	}
	
	@Override
	public void handle(MouseEvent me) {
		GraphicsContext gc = ((Canvas)me.getSource()).getGraphicsContext2D();
		gc.setFill(curColor);
		curTool.handleMouseAction(me, gc);
	}
}
