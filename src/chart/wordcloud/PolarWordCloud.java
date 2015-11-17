package chart.wordcloud;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import chart.wordcloud.collide.Vector2d;
import javafx.scene.paint.Color;

/**
 * Created by kenny on 6/29/14.
 */
public class PolarWordCloud extends WordCloud {

    private static final ColorPalette DEFAULT_POSITIVE_COLORS = new ColorPalette(Color.web("1BE000FF"), 
    				Color.web("1AC902FF"),
    				Color.web("15B000FF"),
    				Color.web("129400FF"),
    				Color.web("0F7A00FF"),
    				Color.web("0B5E00FF"));

    private static final ColorPalette DEFAULT_NEGATIVE_COLORS = new ColorPalette(Color.web("F50000FF"), 
    				Color.web("DE0000FF"),
    				Color.web("C90202FF"),
    				Color.web("B50202FF"),
    				Color.web("990202FF"),
    				Color.web("800101FF"));


    private final PolarBlendMode polarBlendMode;

    private ColorPalette colorPalette2;

    public PolarWordCloud(int width, int height, CollisionMode collisionMode) {
        this(width, height, collisionMode, PolarBlendMode.EVEN);
        this.colorPalette = DEFAULT_POSITIVE_COLORS;
        this.colorPalette2 = DEFAULT_NEGATIVE_COLORS;
    }

    public PolarWordCloud(int width, int height, CollisionMode collisionMode, PolarBlendMode polarBlendMode) {
        super(width, height, collisionMode);
        this.polarBlendMode = polarBlendMode;
        this.colorPalette = DEFAULT_POSITIVE_COLORS;
        this.colorPalette2 = DEFAULT_NEGATIVE_COLORS;
    }

    public void build(List<WordFrequency> wordFrequencies, List<WordFrequency> wordFrequencies2) {
        Collections.sort(wordFrequencies);
        Collections.sort(wordFrequencies2);

        final List<Word> words = buildwords(wordFrequencies, colorPalette);
        final List<Word> words2 = buildwords(wordFrequencies2, colorPalette2);

        final Iterator<Word> wordIterator = words.iterator();
        final Iterator<Word> wordIterator2 = words2.iterator();

        final Vector2d[] poles = getRandomPoles();
        final Vector2d pole1 = poles[0];
        final Vector2d pole2 = poles[1];

        while(wordIterator.hasNext() || wordIterator2.hasNext()) {

            if(wordIterator.hasNext()) {
                final Word word = wordIterator.next();
                final Vector2d startPosition = getStartPosition(pole1);

                place(word, startPosition.getX(), startPosition.getY());
            }
            if(wordIterator2.hasNext()) {
                final Word word = wordIterator2.next();
                final Vector2d startPosition = getStartPosition(pole2);

                place(word, startPosition.getX(), startPosition.getY());
            }
        }

        drawForgroundToBackground();
    }

    private Vector2d getStartPosition(Vector2d pole) {
        switch(polarBlendMode) {
            case BLUR:
                final int blurX = width / 2;
                final int blurY = height / 2;
                return new Vector2d(
                    pole.getX() + -blurX + RANDOM.nextInt(blurX * 2),
                    pole.getY() + -blurY + RANDOM.nextInt(blurY * 2)
                );
            case EVEN:
            default:
                return pole;
        }
    }

    private Vector2d[] getRandomPoles() {
        final Vector2d[] max = new Vector2d[2];
        double maxDistance = 0.0;
        for(int i = 0; i < 100; i++) {
            final int x = RANDOM.nextInt(width);
            final int y = RANDOM.nextInt(height);
            final int x2 = RANDOM.nextInt(width);
            final int y2 = RANDOM.nextInt(height);
            final double distance = Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2));
            if(distance > maxDistance) {
                maxDistance = distance;
                max[0] = new Vector2d(x, y);
                max[1] = new Vector2d(x2, y2);
            }
        }
        return max;
    }

    public void setColorPalette2(ColorPalette colorPalette2) {
        this.colorPalette2 = colorPalette2;
    }

}
