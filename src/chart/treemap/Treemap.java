package chart.treemap;

import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;

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

    void dump(Item item)
    {
    	System.out.println("Item: " + item.toString());
    	for (Item i : item.getItems())
    		dump(i);
    }
    
    public Treemap(Item item) {
        root = item;
        
        dump(root);
        final List<Item> items = root.getItems();
        treemapLayouter = elementFactory.createTreemapLayout(width.doubleValue(), height.doubleValue(), items);
        ChangeListener<Number> changeListener = (obs, old, val) -> {
                treemapLayouter.update(width.doubleValue(), height.doubleValue(), items);
        };
        width.addListener(changeListener);
        height.addListener(changeListener);
        
        getChildren().add(treemapLayouter);
    }

    public void update() {      treemapLayouter.update(width.doubleValue(), height.doubleValue(), root.getItems());    }

    public DoubleProperty getWidth() {       return width;    }
    public DoubleProperty getHeight() {        return height;    }
}
