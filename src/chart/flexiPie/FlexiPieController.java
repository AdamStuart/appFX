package chart.flexiPie;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class FlexiPieController implements Initializable{
	
	@FXML Pane container;
	public Pane getContainer()	{ return container;	}
	@FXML TreeTableView<TreeItem> table;
	@FXML TreeTableColumn<TreeTableView, Wedge>  colorColumn;
	@FXML TreeTableColumn<TreeTableView, Wedge>  categoryColumn;
	@FXML TreeTableColumn<TreeTableView, Double>  portionColumn;
//	private Label label;
	private PieModel model;
	
	@Override public void initialize(URL location, ResourceBundle resources)
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
		ObservableMap<Object, Object> props = container.getProperties();
		ObservableList<String> styles = container.getStylesheets();
		List<CssMetaData<? extends Styleable, ?>> list = 	PieChart.getClassCssMetaData();
//		String p = props.get("default-color").toString();
//		label = new Label("0.0");
//		container.getChildren().add(label);
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
		portionColumn.setCellFactory( p ->	{	return new TwoDigitCell();	});
		
	}
	public void setLabel(String s)	{ /*label.setText(s);*/	}
	public void setLabel(double d)	{		setLabel(String.format("%.2f", d));	}

	class TwoDigitCell extends TreeTableCell<TreeTableView, Double>
	{
		@Override protected void updateItem(Double item, boolean empty)
		{
			super.updateItem(item, empty);
			setText(empty ? "" : String.format("%.2f", item));
		}	
	}
	
	// ---------------------------------------------------------------------------
	  private    ObservableList<PieChart.Data> data2 =     FXCollections.observableArrayList(
	    	            new PieChart.Data("T", 13),
	    	            new PieChart.Data("B", 25),
	    	            new PieChart.Data("NK", 10),
	    	            new PieChart.Data("Myeloid", 22),
	    	            new PieChart.Data("Other", 30));		
	  private    ObservableList<PieChart.Data> data3 =     FXCollections.observableArrayList(
	    	            new PieChart.Data("T", 16),
	    	            new PieChart.Data("B", 23),
	    	            new PieChart.Data("NK", 8),
	    	            new PieChart.Data("Myeloid", 45),
	    	            new PieChart.Data("Other", 23));		

    static String[] names = new String[]{ "Lymphocytes", "Monocytes", "Granulocytes", "Debris" };
    static double[] nums = new double[] { 123, 75, 45, 23, 20 };

    private DoughnutChart doughnutChart; 
    private    ObservableList<PieChart.Data> data;
    private AnchorPane flexiPieRoot;

    private  PieChart pieChart, innerPie;
 
 // ---------------------------------------------------------------------------
	public VBox createContent()
	{
		pieChart = new PieChart();
		data = FXCollections.observableArrayList();
		for (int i = 0; i < names.length; i++)
			data.add(new PieChart.Data(names[i], nums[i]));
		pieChart.getData().addAll(data);

		doughnutChart = new DoughnutChart(data2);
		if (doughnutChart == null) System.out.println("doughnutChart = null");

	
		
		innerPie = new PieChart(data3);
		innerPie.setLabelsVisible(false);
		innerPie.setLegendVisible(false);
		
		
		model.grabPalette(doughnutChart);
			
		return new VBox(new HBox(pieChart, doughnutChart), flexiPieRoot);
	}

}


