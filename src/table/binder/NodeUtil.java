package table.binder;

import javafx.beans.binding.Binding;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class NodeUtil
{
	static public void centerAt(ImageView view, double x, double y)
	{
		view.setX(x - (view.getFitWidth() / 2));
		view.setY(y + (view.getFitHeight() / 2));
	}
	static public void forceWidth(Region n, int w)
	{
		n.setPrefWidth(w);
		n.setMinWidth(w);
		n.setMaxWidth(w);
	}
	static public void forceHeight(Region n, int h)
	{
		n.setPrefHeight(h);
		n.setMinHeight(h);
		n.setMaxHeight(h);
	}
	static public void forceSize(Region n, int w, int h)
	{
		forceWidth(n, w);
		forceHeight(n, h);
	}
	
    static public void invalOnActionOrFocusLost(Node n, Binding b)
    {
    	n.addEventHandler(ActionEvent.ACTION, evt -> b.invalidate());
        n.focusedProperty().addListener((obs, wasFocused, isFocused)-> {
            if (! isFocused) {	b.invalidate(); }
        });
 	
    }
    

}
