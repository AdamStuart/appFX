package chart.wordcloud.collide;

import chart.wordcloud.image.CollisionRaster;

/**
 * Created by kenny on 6/29/14.
 */
public interface Collidable {
    boolean collide(Collidable collidable);
    Vector2d getPosition();
    int getWidth();
    int getHeight();
    CollisionRaster getCollisionRaster();
}
