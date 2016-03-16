package chart.treemap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

import chart.treemap.paint.ColorGroup;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

/**
 * Squarified Treemaps 
 * http://www.win.tue.nl/~vanwijk/stm.pdf
 *
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapLayout extends Parent {

    private final ColorGroup colorGroup;
    private final TreemapElementFactory elementFactory;

    enum LayoutOrient {        VERTICAL, HORIZONTAL    }

    private static final Logger LOG = Logger.getLogger(TreemapLayout.class.getName());
    private AnchorPane anchorPane = new AnchorPane();
    private double height;
    private double width;
    private double heightLeft;
    private double widthLeft;
    private double left = 0.0;
    private double top = 0.0;
    private LayoutOrient layoutOrient = LayoutOrient.VERTICAL;
    private List<TreemapDtoElement> children = new ArrayList<>();

    public TreemapLayout(double w, double h, List<Item> children, ColorGroup colors, TreemapElementFactory f) {
        colorGroup = colors;
        elementFactory = f;
        getChildren().add(anchorPane);
        update(w, h, children);
//        anchorPane.setBorder(Borders.thinGold);
    }


    public void update(double w, double h, List<Item> kids) {
        width = w;
        height = h;
        left = top = 0.0;

        children.clear();
        for (Item item : kids) 
            children.add(new TreemapDtoElement(item));
        
        layoutOrient = (width > height) ? LayoutOrient.VERTICAL : LayoutOrient.HORIZONTAL;
        
        scaleArea(children);
        doLayout();
    }


    private void doLayout() 
    {
    	System.out.println("doLayout");
        heightLeft = height;
        widthLeft = width;
        AnchorPane.clearConstraints(anchorPane);
        anchorPane.getChildren().clear();
        squarify(new ArrayDeque<>(children), new ArrayDeque<TreemapDtoElement>(), minimumSide());
        for (TreemapDtoElement child : children) 
        {
            Parent treeElementItem = elementFactory.createElement(child, colorGroup);
            anchorPane.getChildren().add(treeElementItem);
            if (child.getTop() > this.height) {      throw new IllegalStateException("Top is bigger than height");          }
            if (child.getLeft() > this.width) {      throw new IllegalStateException("Left is bigger than width");          }
            AnchorPane.setTopAnchor(treeElementItem, child.getTop());
            AnchorPane.setLeftAnchor(treeElementItem, child.getLeft());
        }
    }

    private void squarify(Deque<TreemapDtoElement> children, Deque<TreemapDtoElement> row, double w) {
    	System.out.println("squarify");
        ArrayDeque<TreemapDtoElement> remainPoped = new ArrayDeque<>(children);
        TreemapDtoElement c = remainPoped.pop();
        Deque<TreemapDtoElement> concatRow = new ArrayDeque<>(row);
        concatRow.add(c);

        Deque<TreemapDtoElement> remaining = new ArrayDeque<>(remainPoped);


        final double worstConcat = worst(concatRow, w);
        final double worstRow = worst(row, w);

        if (row.isEmpty() || (worstRow > worstConcat || isDoubleEqual(worstRow, worstConcat))) 
        {
            if (remaining.isEmpty())        layoutrow(concatRow, w);
             else 			                squarify(remaining, concatRow, w);
            
        } else 
        {
            layoutrow(row, w);
            squarify(children, new ArrayDeque<TreemapDtoElement>(), minimumSide());
        }
    }

    private double worst(Deque<TreemapDtoElement> ch, double w) {
        if (ch.isEmpty())       return Double.MAX_VALUE;
        
        double areaSum = 0.0, maxArea = 0.0, minArea = Double.MAX_VALUE;
        for (TreemapDtoElement item : ch) 
        {
            double area = item.getArea();
            areaSum += area;
            minArea = minArea < area ? minArea : area;
            maxArea = maxArea > area ? maxArea : area;
        }
        double sqw = w * w;
        double sqAreaSum = areaSum * areaSum;
        return Math.max(sqw * maxArea / sqAreaSum, sqAreaSum / (sqw * minArea));
    }

    private void layoutrow(Deque<TreemapDtoElement> row, double w) {

        double totalArea = 0.0;
        for (TreemapDtoElement item : row) {
            double area = item.getArea();
            totalArea += area;
        }

        if (layoutOrient.equals(LayoutOrient.VERTICAL)) {


            double rowWidth = totalArea / w;
            double topItem = 0;

            for (TreemapDtoElement item : row) {
                double area = item.getArea();
                double h = (area / rowWidth);
                item.setTop(top + topItem);
                item.setLeft(left);
                item.setWidth(rowWidth);
                item.setHeight(h);

                topItem += h;
            }
            widthLeft -= rowWidth;
            //this.heightLeft -= w;
            left += rowWidth;
            double minimumSide = minimumSide();
            if (!isDoubleEqual(minimumSide, heightLeft)) {
                changeLayout();
            }
        } else {

            double rowHeight = totalArea / w;
            double rowLeft = 0;

            for (TreemapDtoElement item : row) {
                double area = item.getArea();
                double wi = (area / rowHeight);
                item.setTop(top);
                item.setLeft(left + rowLeft);
                item.setHeight(rowHeight);
                item.setWidth(wi);

                rowLeft += wi;
            }
            //this.widthLeft -= rowHeight;
            heightLeft -= rowHeight;
            top += rowHeight;

            double minimumSide = minimumSide();
            if (!isDoubleEqual(minimumSide, widthLeft)) 
                changeLayout();
        }
    }

    private void changeLayout() {
        layoutOrient = (layoutOrient.equals(LayoutOrient.HORIZONTAL)) ? 
            	LayoutOrient.VERTICAL : LayoutOrient.HORIZONTAL;
    }

    private boolean isDoubleEqual(double one, double two) {
        double eps = 0.00001;
        return Math.abs(one - two) < eps;
    }

    private double minimumSide() {        return Math.min(heightLeft, widthLeft);    }

    private void scaleArea(List<TreemapDtoElement> children) {
        double areaGiven = width * height;
        double areaTotalTaken = 0.0;
        for (TreemapDtoElement child : children) 
            areaTotalTaken += child.getArea();
        
        double ratio = areaTotalTaken / areaGiven;
        for (TreemapDtoElement child : children) 
            child.setArea(child.getArea() / ratio);
        
    }
}
