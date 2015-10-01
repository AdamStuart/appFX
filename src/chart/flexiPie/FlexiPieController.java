package chart.flexiPie;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class FlexiPieController {
	
	@FXML Pane container;
	@FXML TreeTableView<TreeItem> table;
	@FXML TreeTableColumn<TreeTableView, Wedge>  colorColumn;
	@FXML TreeTableColumn<TreeTableView, Wedge>  categoryColumn;
	@FXML TreeTableColumn<TreeTableView, Wedge>  portionColumn;
	Label label;
	PieModel model;
	
	public void initialize()
	{
		assert(container != null);
		double rX = container.getPrefWidth() / 2;
		double rY = container.getPrefHeight() / 2;
		double centerX =  rX;
		double centerY = rY;
		model = new PieModel(centerX, centerY, rX, rY, this);
		Group g = model.buildPie();
		container.getChildren().add(g);
		container.getChildren().add(model.createHandle());
		
		label = new Label("0.0");
		container.getChildren().add(label);
		model.select(0);
		setupTable();
		 
	}
	
	private void setupTable()
	{
		TreeItem root = model.createTreeItems();
		table.setRoot(root);
		root.setExpanded(true);
		categoryColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));  
		portionColumn.setCellValueFactory(new TreeItemPropertyValueFactory("length"));  

	}
	public void setLabel(String s)	{		label.setText(s);	}
	public void setLabel(double d)	{		setLabel(String.format("%.2f", d));	}
}
