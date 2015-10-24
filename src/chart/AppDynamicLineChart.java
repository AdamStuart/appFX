package chart;
import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.*;

// Demonstrates dynamically changing the data series assigned to a chart and applying css styles to the 
// chart based on user selection and data series attributes.
public class AppDynamicLineChart extends Application {
  private LineChart<Number, Number> lineChart;
  private ObservableList<Event> events;
  private Pane layout = new HBox();

  @Override public void init() throws Exception {
    //defining the axes
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Attempt");
    yAxis.setLabel("Distance (meters)");
    xAxis.setMinorTickVisible(false);
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(1);
    xAxis.setUpperBound(3);
    xAxis.setTickUnit(1);

    //creating the chart
    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setAnimated(false);
    lineChart.setTitle("Event Performance");
    events = FXCollections.observableArrayList(
       new Event("Javelin", "6 6", FXCollections.observableArrayList(
          createSeries("Javelin - Tokyo", FXCollections.observableArrayList(18, 20, 22)),
          createSeries("Javelin - Kyoto", FXCollections.observableArrayList(23, 14, 15))
       )),
       new Event("Hammer", "12 2 2 12", FXCollections.observableArrayList(
          createSeries("Hammer - Tokyo",  FXCollections.observableArrayList(12, 11, 5)),
          createSeries("Hammer - Kyoto",  FXCollections.observableArrayList(9, 8, 13))
       )),
       new Event("Shotput", "", FXCollections.observableArrayList(
          createSeries("Shotput - Tokyo", FXCollections.observableArrayList(3, 2, 4)),
          createSeries("Shotput - Kyoto", FXCollections.observableArrayList(4, 6, 5))
       ))
    );        
    populateData(events, lineChart);
    
    // create some controls which can toggle series display on and off.
    final VBox eventChecks = new VBox(20);
    eventChecks.setStyle("-fx-padding: 10;");
    final TitledPane controlPane = new TitledPane("Event Selection", eventChecks);
    controlPane.setCollapsible(false);
    for (final Event event: events) {
      final CheckBox box = new CheckBox(event.getName());
      box.setSelected(true);
      Line line = new Line(0, 10, 50, 10);
      StringBuilder styleString = new StringBuilder("-fx-stroke-width: 3; -fx-stroke: gray;");
      if (event.getStrokeDashArray() != null && !event.getStrokeDashArray().isEmpty()) {
        styleString.append("-fx-stroke-dash-array: ").append(event.getStrokeDashArray()).append(";");
      }
      line.setStyle(styleString.toString());
      
      box.setGraphic(line);
      eventChecks.getChildren().add(box);
      box.setOnAction(action -> {
          event.setActive(box.isSelected());
          populateData(events, lineChart);
          styleSeries(events, lineChart);
        }
      );
    }
    
    Label caption = new Label(
        "The chart displays performance an athelete in selected events at various sporting meets.  "
      + "Events in which the athelete performed above average at a given meet are shown blue and "
      + "events at which the athelete performed below average are shown red.  For a given meet, "
      + "the athelete may make three attempts per event.  The red and blue highlighting is calculated "
      + "based on the average distance achieved for an event at a meet, not the longest distance achieved for the event at the meet.  Select events to display from the controls on the left."
    );
    caption.setWrapText(true);
    
    // layout the scene
    HBox controlledChart =  new HBox(10,
          controlPane, lineChart
    );
    controlledChart.setAlignment(Pos.CENTER);
    VBox captionedChart = new VBox(10,
          controlledChart,
          caption
    );
    captionedChart.setAlignment(Pos.CENTER);
    HBox.setHgrow(lineChart, Priority.ALWAYS);
    VBox.setVgrow(captionedChart.getChildren().get(0), Priority.ALWAYS);
    layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
    layout.getChildren().addAll(captionedChart);
  }

  @Override public void start(Stage stage) {
    stage.setTitle("Sports Day Results");
    
    Scene scene = new Scene(layout, 800, 600);
    stage.setScene(scene);
    stage.show();

    styleSeries(events, lineChart);
  }

  private void populateData(final ObservableList<Event> events, final LineChart<Number, Number> lineChart) {
    lineChart.getData().clear();
    for (Event event: events) {
      if (event.isActive()) {
        lineChart.getData().addAll(event.getSeries());
      }  
    }
  }

  private void styleSeries(ObservableList<Event> events, final LineChart<Number, Number> lineChart) {
    // force a css layout pass to ensure that subsequent lookup calls work.
    lineChart.applyCss();

    // mark different series with different depending on whether they are above or below average.
    int nSeries = 0;
      for (Event event : events) {
          if (!event.isActive()) continue;
          for (int j = 0; j < event.getSeries().size(); j++) {
              XYChart.Series<Number, Number> series = event.getSeries().get(j);
              Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
              for (Node n : nodes) {
                  StringBuilder style = new StringBuilder();
                  if (event.isBelowAverage(series)) 
                      style.append("-fx-stroke: red; -fx-background-color: red, white; ");
                  else 
                      style.append("-fx-stroke: blue; -fx-background-color: blue, white; ");
                  
                  if (event.getStrokeDashArray() != null && !event.getStrokeDashArray().isEmpty()) 
                      style.append("-fx-stroke-dash-array: ").append(event.getStrokeDashArray()).append(";");
                  n.setStyle(style.toString());
              }
              nSeries++;
          }
      }
  }
  
  private XYChart.Series<Number, Number> createSeries(String name, List<Number> data) {
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName(name);
    ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
    for (int i = 0; i < data.size(); i++) 
      seriesData.add(new XYChart.Data<>(i+1, data.get(i)));
    series.setData(seriesData);
    return series;
  }
  //-----------------------------------------------------------------------------------
  private class Event {		// an event here is a track event (e.g., shot put), not a mouse event
    private String name;
    private ObservableList<XYChart.Series<Number, Number>> series;
    private String strokeDashArray;
    private boolean isActive = true;

    public String getName() { return name; }
    public String getStrokeDashArray() { return strokeDashArray; }
    private boolean isActive() {      return isActive;    }
    private void setActive(boolean isActive) {      this.isActive = isActive;    }
    public ObservableList<XYChart.Series<Number, Number>> getSeries() {      return series;    }
   
    public Event(String nm, String dashArray, ObservableList<XYChart.Series<Number, Number>> srs) {
      name = nm; strokeDashArray = dashArray; series = srs;
    }
    
    public boolean isBelowAverage(XYChart.Series<Number, Number> checkedSeries) {
      double checkedSeriesAvg = calcSeriesAverage(checkedSeries);
      double allSeriesAvgTot = 0;
      double seriesCount = series.size();
      for (XYChart.Series<Number, Number> curSeries: series) 
        allSeriesAvgTot += calcSeriesAverage(curSeries);
      double allSeriesAvg = seriesCount != 0 ? allSeriesAvgTot / seriesCount: 0; 
      return checkedSeriesAvg < allSeriesAvg;
    }
    
    private double calcSeriesAverage(XYChart.Series<Number, Number> series) {
      double sum = 0;
      int count = series.getData().size();
      for (XYChart.Data<Number, Number> data: series.getData()) 
        sum += data.YValueProperty().get().doubleValue();
      return count != 0 ? sum / count : 0; 
    }

  }
  
  public static void main(String[] args) {    launch(args);  }
}