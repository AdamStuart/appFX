package chart.flexiPie;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DoughnutChart extends PieChart {
    private final Circle innerCircle;
//    private final PieChart innerPie;

    public DoughnutChart(ObservableList<Data> pieData) {
        super(pieData);

        innerCircle = new Circle();

        // just styled in code for demo purposes,
        // use a style class instead to style via css.
//        innerCircle.setFill(Color.WHITESMOKE);
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(3);
        innerCircle.setOpacity(0.3);
        
//        innerPie = new PieChart(innerData);
//        innerPie.setLabelsVisible(false);
//        innerPie.setLegendVisible(false);
//        innerPie.setMinSize(50, 50);
////        innerPie.prefHeightProperty().bind(innerCircle.heightProperty());
        
        
    }

    @Override  protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);

        addInnerCircleIfNotPresent();
        updateInnerCircleLayout();
    }

    private void addInnerCircleIfNotPresent() {
        if (getData().size() > 0) {
            Node pie = getData().get(0).getNode();
            if (pie.getParent() instanceof Pane) 
            {
                Pane parent = (Pane) pie.getParent();
                if (!parent.getChildren().contains(innerCircle)) 
                {
//                	parent.getChildren().add(innerPie);
                    parent.getChildren().add(innerCircle);
                }
            }
        }
    }

    private void updateInnerCircleLayout() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (PieChart.Data data: getData()) 
        {
            Bounds bounds = data.getNode().getBoundsInParent();
            if (bounds.getMinX() < minX) 	minX = bounds.getMinX(); 
            if (bounds.getMinY() < minY)    minY = bounds.getMinY();
            if (bounds.getMaxX() > maxX)    maxX = bounds.getMaxX();
            if (bounds.getMaxY() > maxY)    maxY = bounds.getMaxY();
        }

        innerCircle.setCenterX(average(minX , maxX));
        innerCircle.setCenterY(average(minY, maxY));
        innerCircle.setRadius((maxX - minX) / 4);
        
//        innerPie.setLayoutX(average(minX , maxX));
//        innerPie.setLayoutY(average(minY , maxY));
//        innerPie.setPrefWidth((maxX - minX) / 2);
//        innerPie.setPrefHeight((maxY - minY) / 2);
    }
    
   double average(double a, double b) {  return (a + (b-a)/2); }
}