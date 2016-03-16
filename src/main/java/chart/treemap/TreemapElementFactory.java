package chart.treemap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chart.treemap.paint.ColorBucket;
import chart.treemap.paint.ColorGroup;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapElementFactory {
    private final ColorBucket colorBucket = ColorBucket.createBucket();

    private Map<TreemapDtoElement, ColorGroup> colorGroupCache = new HashMap<>();
    private Map<TreemapDtoElement, Color> colorCache = new HashMap<>();

    public Parent createElement(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        ColorGroup realColorGroup = getColorGroup(dtoElement, colorGroup);
        Color color = getColor(dtoElement, realColorGroup);
        if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) {
            final List<Item> items = dtoElement.getItem().getItems();
            final double width = dtoElement.getWidth();
            final double height = dtoElement.getHeight();
            System.out.println(String.format("size = %.2f x %.2f", width, height)) ;
            return new TreemapLayout(width, height, items, realColorGroup, this);
        }
        return new TreemapRectangle(dtoElement, color);
    }

    private Color getColor(TreemapDtoElement dtoElement, ColorGroup realColorGroup) {
        if (colorCache.containsKey(dtoElement)) 
            return colorCache.get(dtoElement);
        Color color = realColorGroup.fetchColor();
        colorCache.put(dtoElement, color);
        return color;
    }

    private ColorGroup getColorGroup(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        if (colorGroupCache.containsKey(dtoElement)) 
            return colorGroupCache.get(dtoElement);

        ColorGroup realColorGroup = colorGroup;
        if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) 
            realColorGroup = colorBucket.fetchColorGroup(dtoElement.getItem().getItems().size());
        colorGroupCache.put(dtoElement, realColorGroup);
        return realColorGroup;
    }

    public TreemapLayout createTreemapLayout(double w, double h, List<Item> items) {
        return new TreemapLayout(w, h, items, colorBucket.fetchColorGroup(items.size()), this);
    }
}
