package chart;

import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

//------------------------------------------------------------------------
class OverlaidScatterChart<X, Y> extends ScatterChart<X, Y> {

   // data defining horizontal markers, xValues are ignored
//   private ObservableList<Re> horizontalMarkers;

   public OverlaidScatterChart(Axis<X> xAxis, Axis<Y> yAxis) {
       super(xAxis, yAxis);
   }

   /**
    * Add gate. 
    * 
    * @param gate must not be null.
    */
   public void addRectangleOverlay(Rectangle gate) {
       Objects.requireNonNull(gate, "the gate must not be null");
       getPlotChildren().add(gate);
//       horizontalMarkers.add(marker);
   }

   /**
    * Remove gate.
    * 
    * @param gate must not be null
    */
   public void removeRectangleOverlay(Rectangle gate) {
       Objects.requireNonNull(gate, "the gate must not be null");
       if (gate != null) {
           getPlotChildren().remove(gate);
       }
//       horizontalMarkers.remove(marker);
   }
   public void removeGates() {
   	ObservableList<Node> kids = getPlotChildren();
       for (int i = kids.size()-1; i>0; i--)
       {
       	if (kids.get(i).getStyleClass().contains("gate"))
       		kids.remove(i);
       }
   }

   /**
    * Add horizontal value marker. The marker's Y value is used to plot a
    * horizontal line across the plot area, its X value is ignored.
    * 
    * @param marker must not be null.
    */
   public void addHorizontalValueMarker(Data<X, Y> marker) {
       Objects.requireNonNull(marker, "the marker must not be null");
//       if (horizontalMarkers.contains(marker)) return;
       Line line = new Line();
       marker.setNode(line );
       getPlotChildren().add(line);
//       horizontalMarkers.add(marker);
   }

   /**
    * Remove horizontal value marker.
    * 
    * @param horizontalMarker must not be null
    */
   public void removeHorizontalValueMarker(Data<X, Y> marker) {
       Objects.requireNonNull(marker, "the marker must not be null");
       if (marker.getNode() != null) {
           getPlotChildren().remove(marker.getNode());
           marker.setNode(null);
       }
//       horizontalMarkers.remove(marker);
   }

   /**
    * Overridden to layout the value markers.
    */
   @Override
   protected void layoutPlotChildren() {
       super.layoutPlotChildren();
//       	ObservableList<Node>children = getPlotChildren();
//       	removeGates();
//       	Rectangle frame = getPlotFrame();
//			rescaleRect(selectionRectangleDef, frame, selectionRectangle);
//       	for (Rectangle gate : gateDefs)			
//       		makeGate(getScaleRect(gate, frame));
   	
//	        System.out.println("layoutPlotChildren");
  	

//       for (Data<X, Y> horizontalMarker : horizontalMarkers) {
//           double lower = ((ValueAxis) getXAxis()).getLowerBound();
//           X lowerX = getXAxis().toRealValue(lower);
//           double upper = ((ValueAxis) getXAxis()).getUpperBound();
//           X upperX = getXAxis().toRealValue(upper);
//           Line line = (Line) horizontalMarker.getNode();
//           line.setStartX(getXAxis().getDisplayPosition(lowerX));
//           line.setEndX(getXAxis().getDisplayPosition(upperX));
//           line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()));
//           line.setEndY(line.getStartY());
//
//       }
   }
}
