package chart.wordcloud.bg;

import chart.wordcloud.collide.Collidable;

/**
 * Created by kenny on 6/30/14.
 */
public interface Background {
    boolean isInBounds(Collidable collidable);
}
