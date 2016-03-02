package container.gmapsfx;

import container.gmapsfx.javascript.object.GoogleMap;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class StreetViewBorderPane extends BorderPane implements MapComponentInitializedListener
{
	private GoogleMap map;
	private GoogleMapView mapview;
	
	public StreetViewBorderPane()
	{
		super();
		mapview = new GoogleMapView("html/streetview.html");
		mapview.addMapInializedListener(this);
		
		ToolBar tools = new ToolBar();
		HBox info = new HBox(8);
		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		setTop(tools);
		setCenter(mapview);
		setBottom(info);
	}
	  
	@Override   public void mapInitialized() {	}
}
