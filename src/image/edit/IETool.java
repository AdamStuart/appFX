package image.edit;

import javafx.event.EventType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public abstract class IETool {
	//Renaming constants for an easier reference.
	public final EventType<MouseEvent> MOUSE_DRAGGED = MouseEvent.MOUSE_DRAGGED;
	public final EventType<MouseEvent> MOUSE_CLICKED = MouseEvent.MOUSE_CLICKED;
	
	public abstract void handleMouseAction(MouseEvent me, GraphicsContext gc);
}
