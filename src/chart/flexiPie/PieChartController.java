package chart.flexiPie;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;

public 
class PieChartController implements Initializable {

  @FXML private  PieChart pieChart;
  
  private    ObservableList<PieChart.Data> data;

  public 
  void buildData(){
    Connection c ;
    this.data = FXCollections.observableArrayList();

    String[] names = new String[]{ "Lymphocytes", "Monocytes", "Granulocytes", "Dead Cells" };
      double[] nums = new double[] { 88,75, 45, 23, 20 };
     for (int i=0; i<names.length; i++)
         this.data.add(new PieChart.Data(names[i],nums[i]));
  }

  @Override public 
  void initialize(URL url, ResourceBundle rb) {
    System.out.println("PieChartController.initialize()");
    buildData();
    this.pieChart.getData().addAll(data);
  }

}
