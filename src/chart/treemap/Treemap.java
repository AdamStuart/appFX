package chart.treemap;

import java.util.SortedSet;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import chart.treemap.paint.ColorBucket;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 * 
 * http://en.wikipedia.org/wiki/Treemapping
 * 
 */
public class Treemap extends Parent {


//    private final ColorBucket colorBucket = ColorBucket.createBucket();
    private final Item root;

    private DoubleProperty width = new SimpleDoubleProperty(640.0);
    private DoubleProperty height = new SimpleDoubleProperty(480.0);

    private TreemapElementFactory elementFactory = new TreemapElementFactory();
    private final TreemapLayout treemapLayouter;

    public Treemap(Item root) {
        this.root = root;
        final SortedSet<Item> items = root.getItems();
        treemapLayouter = elementFactory.createTreemapLayout(width.doubleValue(), height.doubleValue(), items);
        ChangeListener<Number> changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                treemapLayouter.update(width.doubleValue(), height.doubleValue(), items);
            }
        };
        width.addListener(changeListener);
        height.addListener(changeListener);
        this.getChildren().add(treemapLayouter);
    }

    public void update() {      treemapLayouter.update(width.doubleValue(), height.doubleValue(), root.getItems());    }

    public DoubleProperty getWidth() {       return width;    }
    public DoubleProperty getHeight() {        return height;    }
}
