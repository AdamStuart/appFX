package chart.wordcloud.bg;

import chart.wordcloud.collide.Collidable;
import chart.wordcloud.collide.Vector2d;

/**
 * Created by kenny on 6/30/14.
 */
public class CircleBackground implements Background {

    private final double radius;

    public CircleBackground(double r) {       radius = r;    }

    @Override
    public boolean isInBounds(Collidable collidable) {
        final Vector2d position = collidable.getPosition();
        return inCircle(position.getX(), position.getY()) &&
                inCircle(position.getX() + collidable.getWidth(), position.getY()) &&
                inCircle(position.getX(), position.getY() + collidable.getHeight()) &&
                inCircle(position.getX() + collidable.getWidth(), position.getY() + collidable.getHeight());
    }

    private boolean inCircle(int x, int y) {
        x -= radius ;
        y -= radius;
        return  x * x + y * y <= radius * radius;
    }

}
