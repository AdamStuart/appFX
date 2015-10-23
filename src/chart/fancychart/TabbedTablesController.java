/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package chart.fancychart;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import model.DataItem;

/**
 * 
 */
public class TabbedTablesController  {


	private List<TabTableController> tableControllers;

	public TabbedTablesController(List<TableView<DataItem>> tables)
	{
		tableControllers = new ArrayList<TabTableController>();
		for (int i = 0; i < tables.size(); i++) 
		{
			TabTableController ttc = new TabTableController(tables.get(i));
			ttc.setDataSetIndex(i);
			tableControllers.add(ttc);
			tables.get(i).getSelectionModel().selectedIndexProperty().addListener(observable -> {
					clearAllTableSelections();
			});
		}
	}
	public void initialize() {


	
	}

	public void initTable(final int index, final ObservableList<DataItem> items) {
		tableControllers.get(index).initTable(items);
	}

	public void selectDataItem(final int dataSeriesIndex, final int dataItemIndex) {
		TabTableController ttc = tableControllers.get(dataSeriesIndex);
		ttc.getTable().getSelectionModel().select(dataSeriesIndex);
		clearAllTableSelections();
		ttc.selectDataItem(dataItemIndex);

	}

	private void clearAllTableSelections() {
		for (TabTableController tableController : tableControllers) {
			tableController.clearTableSelection();
		}
	}

}
