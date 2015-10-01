package chart.flexiPie;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.AnchorPane;

public class DoughnutChartController implements Initializable
{
	 @FXML private  AnchorPane doughnutChartHolder;
	 DoughnutChart doughnutChart;
	 
	  private ObservableList<PieChart.Data> data;

	  public  void buildData()
	  {
//	    Connection c ;
//	    try{
//	      c = DBConnect.connect();
	//
//	      String SQL = 
//	        " Select "
//	        +   "count(c.zip) as zipcount, c.zip as zip "
//	        + "From customer c "
//	        + "Group By zip";

		// ResultSet rs = c.createStatement().executeQuery(SQL);
		data =     FXCollections.observableArrayList(
	                new PieChart.Data("Grapefruit", 13),
	                new PieChart.Data("Oranges", 25),
	                new PieChart.Data("Plums", 10),
	                new PieChart.Data("Pears", 22),
	                new PieChart.Data("Apples", 30));		
//
//		    data = FXCollections.observableArrayList();
//		 String[] names = new String[] { "Lymphocytes", "Monocytes", "Granulocytes", "Dead Cells" };
//		double[] nums = new double[] { 88, 75, 45, 23, 20 };
//		for (int i = 0; i < names.length; i++)
//			data.add(new PieChart.Data(names[i], nums[i]));

//	      while(rs.next()){
//	        this.data.add(new PieChart.Data(rs.getString(2),rs.getDouble(1)));
//	      }
//	    } catch(Exception e) {
//	        System.out.println("Error on DB connection: "+e.toString());
//	    }
	//
	  }

	  @Override public  void initialize(URL url, ResourceBundle rb) 
	  {
	    System.out.println("DoughnutChartController.initialize()");
	    buildData();
		DoughnutChart doughnutChart= new DoughnutChart(data);
		doughnutChartHolder.getChildren().add(doughnutChart);
	    if (doughnutChart == null)
		    System.out.println("doughnutChart = null");
	  }
}
