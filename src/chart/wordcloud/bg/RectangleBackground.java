package chart.wordcloud.bg;

import chart.wordcloud.collide.Collidable;
import chart.wordcloud.collide.Vector2d;

/**
 * Created by kenny on 6/30/14.
 */
public class RectangleBackground implements Background {

    private final double width;
    private final double height;

    public RectangleBackground(double w, double h) {
        width = w;
        height = h;
    }

    @Override
    public boolean isInBounds(Collidable collidable) {
        final Vector2d position = collidable.getPosition();
        return position.getX() >= 0 &&
                position.getX() + collidable.getWidth() < width &&
                position.getY() >= 0 &&
                position.getY() + collidable.getHeight() < height;
    }

}
