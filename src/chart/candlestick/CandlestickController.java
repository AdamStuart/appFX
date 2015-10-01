package chart.candlestick;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;

public class CandlestickController implements Initializable {
    
  @FXML private  ScatterChart<Number, Number> candlestickChart;
  @FXML  private NumberAxis xAxis;
  @FXML  private NumberAxis yAxis;

	@Override
	public void initialize(URL url, ResourceBundle rb)
	{
	    System.out.println("CandlestickController doesn't do anything!");
//		assert (candlestickChart != null);
//		buildData();
		// this.barChart.getData().addAll(data);
	}

}
//	

