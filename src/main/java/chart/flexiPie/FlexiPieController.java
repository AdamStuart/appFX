package chart.flexiPie;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import table.binder.Rect;

public class FlexiPieController implements Initializable{
	
	@FXML Pane container;
	public Pane getContainer()	{ return container;	}
	@FXML TreeTableView<TreeItem> table;
	@FXML TreeTableColumn<TreeTableView, Wedge>  firstColumn;
	@FXML TreeTableColumn<TreeTableView, Color>  colorColumn;
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
		container.getChildren().addAll(model.buildPie(), model.createHandle());
//		ObservableMap<Object, Object> props = container.getProperties();
//		ObservableList<String> styles = container.getStylesheets();
//		List<CssMetaData<? extends Styleable, ?>> list = 	PieChart.getClassCssMetaData();
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
		colorColumn.setCellValueFactory(new TreeItemPropertyValueFactory("color"));  
		categoryColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));  
		portionColumn.setCellValueFactory(new TreeItemPropertyValueFactory("scaled"));  
		portionColumn.setCellFactory( p ->	{	return new TwoDigitCell();	});

		// color popup
//		TableColumn<Wedge, Color> colorCol = cols[1];
		colorColumn.setEditable(true);
//		colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
		colorColumn.setCellFactory((t) -> {    return new TreeTableCell<TreeTableView, Color>(){
            private ColorPicker colorPicker;
            private ColorPicker createPicker()
            {
                colorPicker = new ColorPicker();
                colorPicker.getStyleClass().add("button");
                colorPicker.setPromptText("");
                colorPicker.setOnAction(evt -> {
                		int idx = getTreeTableRow().getIndex();
                        ColorPicker cp = (ColorPicker)evt.getSource();
                        Color cw = (Color)cp.getValue();
                        cw = cp.getValue();
//                        select(getTreeTableRow().getIndex());
//        				int idx = getSelectedIndex();
//        				if (idx >= 0)
//        					table.getItems().get(idx).setColor(cw);
                });
                return colorPicker;
            }

            @Override  protected void updateItem(Color value, boolean empty) 
            {                      
                super.updateItem(value, empty);
                if(empty){   setGraphic(null);  return;}		//http://stackoverflow.com/questions/25532568/javafx-tableview-delete-issue
                if(colorPicker == null){
                    colorPicker = createPicker();
                    colorPicker.setUserData(value);
                }
                colorPicker.setValue(value);
                setGraphic(colorPicker);
            }
        	};
	    });
	
	}
	public void setLabel(String s)	{ /*label.setText(s);*/	}
	public void setLabel(double d)	{		setLabel(String.format("%.2f", d));	}

	class TwoDigitCell extends TreeTableCell<TreeTableView, Double>
	{
		@Override protected void updateItem(Double item, boolean empty)
		{
			super.updateItem(item, empty);
			setTextAlignment(TextAlignment.RIGHT);
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
	
//		model.grabPalette(doughnutChart);
		return new VBox(new HBox(pieChart, doughnutChart, innerPie));	//, flexiPieRoot
	}

}


