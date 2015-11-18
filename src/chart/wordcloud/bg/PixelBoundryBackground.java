package chart.wordcloud.bg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import chart.wordcloud.collide.Collidable;
import chart.wordcloud.collide.Vector2d;
import chart.wordcloud.image.CollisionRaster;

/**
 * Created by kenny on 6/30/14.
 */
public class PixelBoundryBackground implements Background {

    private final CollisionRaster collisionRaster;
    private final RectangleBackground rectangleBackground;

    public PixelBoundryBackground(final InputStream imageInputStream) throws IOException {
        final BufferedImage bufferedImage = ImageIO.read(imageInputStream);
        collisionRaster = new CollisionRaster(bufferedImage);
        rectangleBackground = new RectangleBackground(bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    @Override
    public boolean isInBounds(Collidable collidable) {
        // check if bounding boxes intersect
        if(!this.rectangleBackground.isInBounds(collidable))      return false;
        
        final Vector2d position = collidable.getPosition();
        // get the overlapping box
        int startX = Math.max(position.getX(), 0);
        int endX = Math.min(position.getX() + collidable.getWidth(), collisionRaster.getWidth());

        int startY = Math.max(position.getY(), 0);
        int endY = Math.min(position.getY() + collidable.getHeight(), collisionRaster.getHeight());

        for(int y = startY ; y < endY ; y++) {
            for(int x = startX ; x < endX ; x++) {
                // compute offsets for surface
                if(collisionRaster.isTransparent(x - 0, y - 0) &&
                        !collidable.getCollisionRaster().isTransparent(x - position.getX(), y - position.getY())) {
                    return false;
                }
            }
        }
        return true;
    }

}
