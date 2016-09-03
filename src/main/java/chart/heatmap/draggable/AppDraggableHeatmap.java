package chart.heatmap.draggable;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import table.networkTable.NodeRecord;

public class AppDraggableHeatmap extends Application {

    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
		Group topGroup = new Group();
    	topGroup.getChildren().add(generateSquareMap());
		stage.setScene(new Scene(topGroup));
	    stage.setTitle("Drag mouse vertically to reorder rows, Press key to reset");
	    topGroup.addEventFilter(KeyEvent.KEY_PRESSED, event->{
	    	if (event.getCode() == KeyCode.TAB)
	    	{
	    		squareMap.selfSwap();
	    	}
	    	else
	    	{
	    		topGroup.getChildren().clear();
		    	topGroup.getChildren().add(generateSquareMap());
	    	}
       });	    
	    topGroup.requestFocus();
//	    coexGroup.setOnKeyPressed(ev -> {
//	    	generateSquareMap();
//	    });
	    stage.show();

    }

    SquareMap squareMap;
	int mode= 0;
	private Group generateSquareMap() {
		int nNodes = 100;
		List<NodeRecord> nodes = new ArrayList<NodeRecord>();
		for (int i=0; i<nNodes; i++)
			nodes.add(new NodeRecord("00"+i, "A"+(i+1), "N"+i, "Unknown Table Node"));
		squareMap = new SquareMap(nodes, mode++ % 2);
		squareMap.makeRows();
		squareMap.fillSquares();
		return squareMap.getParentGroup();
	}

}
