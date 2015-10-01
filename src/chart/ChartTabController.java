package chart;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import chart.boxWhiskers.BoxWhiskersController;
import chart.histograms.HistogramChartController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class ChartTabController implements Initializable
{
	@FXML private StackPane pieContainer;
	@FXML private StackPane hoverContainer;
	@FXML private StackPane whiskersContainer;
	@FXML private StackPane drillDownContainer;
	@FXML private StackPane histogramContainer;
	@FXML private StackPane fancyContainer;
	@FXML private StackPane timeSeriesContainer;
	@FXML private StackPane usMapContainer;
	
    static private String FXML = "";
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("initialize");		
		Objects.requireNonNull(pieContainer);
		Objects.requireNonNull(hoverContainer);
		Objects.requireNonNull(whiskersContainer);
		Objects.requireNonNull(drillDownContainer);
		Objects.requireNonNull(histogramContainer);
		Objects.requireNonNull(fancyContainer);
		Objects.requireNonNull(timeSeriesContainer);
		Objects.requireNonNull(usMapContainer);
		
		
	    FXMLLoader fxmlLoader = new FXMLLoader();
	    URL url = getClass().getResource(FXML + "histograms/HistogramChart.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
			histogramContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
//		new HistogramChartController(histogramContainer);
	    
	    BoxWhiskersController contl = new BoxWhiskersController();
	    contl.createContent(whiskersContainer);
	    
	    
	    fxmlLoader = new FXMLLoader();
	    url = getClass().getResource("fancychart/FancyChart.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
	    	fancyContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	    fxmlLoader = new FXMLLoader();
	    url = getClass().getResource("usMap/us-map.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
	    	usMapContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
