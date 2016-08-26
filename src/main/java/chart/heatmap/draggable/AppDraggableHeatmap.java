package chart.heatmap.draggable;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import table.networkTable.NodeRecord;

public class AppDraggableHeatmap extends Application {

    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
		Group coexGroup = generateSquareMap();
		stage.setScene(new Scene(coexGroup));
	    stage.setTitle("Drag mouse vertically to reorder rows");
	    stage.show();

    }

	Group squareMap;
	
	private Group generateSquareMap() {
		int nNodes = 100;
		List<NodeRecord> nodes = new ArrayList<NodeRecord>();
		for (int i=0; i<nNodes; i++)
			nodes.add(new NodeRecord("00"+i, "A"+(i+1), "N"+i, "Unknown Table Node"));
		SquareMap sqMap = new SquareMap(nodes);
		sqMap.makeRows();
		sqMap.fillSquares();
		return sqMap.getParentGroup();
	}

}
