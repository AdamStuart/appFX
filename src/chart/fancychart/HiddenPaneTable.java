package chart.fancychart;

import java.util.ArrayList;
import java.util.List;

import model.DataItem;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class HiddenPaneTable extends Pane 
{
	public HiddenPaneTable(TabPane inTabPane)
	{
	    setPrefSize(200,  400);
	    initialize(inTabPane);
	}

	private TabPane tableTabPane;
	private List<HiddenPaneTabTableController> tableControllers  = new ArrayList<HiddenPaneTabTableController>();

	public void initialize(TabPane inTabPane)
	{
//		tableTabPane = new TabPane();
		for (int i = 0; i < 3; i++)						// HARDCODE 3
		{ 
			HiddenPaneTabTableController tableCon = new HiddenPaneTabTableController();
			tableCon.setDataSetIndex(i);
			tableControllers.add(tableCon);
		}

		tableTabPane.getSelectionModel().selectedIndexProperty().addListener(new InvalidationListener()
		{
			@Override public void invalidated(final Observable observable)
			{	clearAllTableSelections();		}
		});
//		getChildren().add(tableTabPane);

	}

	public void initTable(final int index, final ObservableList<DataItem> items)
	{
		tableControllers.get(index).initTable(items);
	}

	public void selectDataItem(final int dataSeriesIndex, final int dataItemIndex) {
		tableTabPane.getSelectionModel().select(dataSeriesIndex);
		clearAllTableSelections();
		tableControllers.get(dataSeriesIndex).selectDataItem(dataItemIndex);
	}

	private void clearAllTableSelections() {
		for (HiddenPaneTabTableController tableController : tableControllers) 
			tableController.clearTableSelection();
	}

}
