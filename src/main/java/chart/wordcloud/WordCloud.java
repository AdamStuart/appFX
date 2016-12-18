package chart.wordcloud;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import chart.wordcloud.bg.Background;
import chart.wordcloud.bg.RectangleBackground;
import chart.wordcloud.collide.CollisionMode;
import chart.wordcloud.collide.RectanglePixelCollidable;
import chart.wordcloud.collide.Vector2d;
import chart.wordcloud.collide.checkers.CollisionChecker;
import chart.wordcloud.collide.checkers.RectangleCollisionChecker;
import chart.wordcloud.collide.checkers.RectanglePixelCollisionChecker;
import chart.wordcloud.font.CloudFont;
import chart.wordcloud.font.FontWeight;
import chart.wordcloud.font.scale.FontScalar;
import chart.wordcloud.font.scale.LinearFontScalar;
import chart.wordcloud.image.AngleGenerator;
import chart.wordcloud.image.CollisionRaster;
import chart.wordcloud.image.ImageRotation;
import chart.wordcloud.nlp.Word;
import chart.wordcloud.nlp.WordFrequency;
import chart.wordcloud.padding.Padder;
import chart.wordcloud.padding.RectanglePadder;
import chart.wordcloud.padding.WordPixelPadder;
import javafx.scene.paint.Color;

/**
 * Created by kenny on 6/29/14.
 * http://kennycason.com/posts/2014-07-03-kumo-wordcloud.html
 */
public class WordCloud {

    private static final Logger LOGGER = Logger.getLogger("WordCloud");

    protected static final Random RANDOM = new Random();

    protected final double width;
    protected final double height;

    protected final CollisionMode collisionMode;
    protected final CollisionChecker collisionChecker;

    protected final Padder padder;
    protected int padding = 0;

    protected Background background;
    protected final RectanglePixelCollidable backgroundCollidable;
    protected Color backgroundColor = Color.BLACK;

    protected FontScalar fontScalar = new LinearFontScalar(10, 40);
    protected CloudFont cloudFont = new CloudFont("Comic Sans MS", FontWeight.BOLD);

    protected AngleGenerator angleGenerator = new AngleGenerator();
    protected final CollisionRaster collisionRaster;
    protected final BufferedImage bufferedImage;

    protected final Set<Word> placedWords = new HashSet<>();
    protected final Set<Word> skipped = new HashSet<>();

    protected ColorPalette colorPalette = new ColorPalette(Color.ORANGE, Color.WHITE, Color.YELLOW, Color.GRAY, Color.GREEN);

    public WordCloud(double width, double height, CollisionMode collisionMode) {
        this.width = width;
        this.height = height;
        this.collisionMode = collisionMode;
        switch(collisionMode) {
            case PIXEL_PERFECT:
                this.padder = new WordPixelPadder();
                this.collisionChecker = new RectanglePixelCollisionChecker();
                break;

            case RECTANGLE:
            default:
                this.padder = new RectanglePadder();
                this.collisionChecker = new RectangleCollisionChecker();
                break;
        }
        collisionRaster = new CollisionRaster((int)width, (int)height);
        bufferedImage = new BufferedImage((int)width,(int) height, BufferedImage.TYPE_INT_ARGB);
        backgroundCollidable = new RectanglePixelCollidable(collisionRaster, 0, 0);
        background = new RectangleBackground(width, height);
    }

    public void build(List<WordFrequency> wordFrequencies) {
        Collections.sort(wordFrequencies);

        for(final Word word : buildwords(wordFrequencies, this.colorPalette)) {
            final int startX = RANDOM.nextInt((int)Math.max(width - word.getWidth(), width));
            final int startY = RANDOM.nextInt((int)Math.max(height - word.getHeight(), height));
            place(word, startX, startY);

        }
        drawForgroundToBackground();
    }
//
//    public void writeToFile(final String outputFileName) {
//        String extension = "";
//        int i = outputFileName.lastIndexOf('.');
//        if (i > 0) {
//            extension = outputFileName.substring(i + 1);
//        }
//        try {
//            LOGGER.info("Saving WordCloud to " + outputFileName);
//            ImageIO.write(bufferedImage, extension, new File(outputFileName));
//        } catch (IOException e) {
//            LOGGER.severe(e.getMessage() + e.getMessage());
//        }
//    }

    /**
     * Write to output stream as PNG
     *
     * @param outputStream the output stream to write the image data to
//     */
//    public void writeToStreamAsPNG(final OutputStream outputStream) {
//        writeToStream("png", outputStream);
//    }

    /**
     * Write wordcloud image data to stream in the given format
     *
     * @param format       the image format
     * @param outputStream the output stream to write image data to
     */
//    public void writeToStream(final String format, final OutputStream outputStream) {
//        try {
//            LOGGER.info("Writing WordCloud image data to output stream");
//            ImageIO.write(bufferedImage, format, outputStream);
//            LOGGER.info("Done writing WordCloud image data to output stream");
//        } catch (IOException e) {
//            LOGGER.info(e.getMessage() + e.getMessage());
//            throw new RuntimeException("Could not write wordcloud to outputstream due to an IOException", e);
//        }
//    }

    /**
     * create background, then draw current word cloud on top of it.
     * Doing it this way preserves the transparency of the this.bufferedImage's pixels
     * for a more flexible pixel perfect collision
     */
    protected void drawForgroundToBackground() {
        if(backgroundColor == null) { return; }

        final BufferedImage backgroundBufferedImage = new BufferedImage((int)width,(int) height, this.bufferedImage.getType());
        final Graphics graphics = backgroundBufferedImage.getGraphics();

        // draw current color
        // AST 
        java.awt.Color awtColor = new java.awt.Color((int)(backgroundColor.getRed() * 256), (int)(backgroundColor.getGreen() * 256), (int)(backgroundColor.getBlue() * 256));
        graphics.setColor(awtColor);
        graphics.fillRect(0, 0, (int)width,(int) height);
        graphics.drawImage(bufferedImage, 0, 0, null);

        // draw back to original
        final Graphics graphics2 = bufferedImage.getGraphics();
        graphics2.drawImage(backgroundBufferedImage, 0, 0, null);
    }

    /**
     * try to place in center, build out in a spiral trying to place words for N steps
     * @param word
     */
    protected void place(final Word word, Vector2d start ) {
    	place(word,  start.getX(),start.getY());
   }
    
    protected void place(final Word word, final int startX, final int startY ) {
        final Graphics graphics = this.bufferedImage.getGraphics();

        final int maxRadius = (int)width;
        for(int r = 0; r < maxRadius; r += 2) {
            for(int x = -r; x <= r; x++) {
                if(startX + x < 0) { continue; }
                if(startX + x >= width) { continue; }

                boolean placed = false;
                word.setX(startX + x);

                // try positive root
                int y1 = (int) Math.sqrt(r * r - x * x);
                if(startY + y1 >= 0 && startY + y1 < height) {
                    word.setY(startY + y1);
                    placed = tryToPlace(word);
                }
                // try negative root
                int y2 = -y1;
                if(!placed && startY + y2 >= 0 && startY + y2 < height) {
                    word.setY(startY + y2);
                    placed = tryToPlace(word);
                }
                if(placed) {
                    collisionRaster.mask(word.getCollisionRaster(), word.getX(), word.getY());
                    graphics.drawImage(word.getBufferedImage(), word.getX(), word.getY(), null);
                    return;
                }

            }
        }
        LOGGER.info("skipped: " + word.getWord());
        skipped.add(word);
    }

    private boolean tryToPlace(final Word word) {
        if(!background.isInBounds(word))  return false;

        switch(collisionMode) {
            case RECTANGLE:
                for(Word placeWord : this.placedWords) 
                    if(placeWord.collide(word))    return false;
                LOGGER.info("place: " + word.getWord());
                placedWords.add(word);
                return true;

            case PIXEL_PERFECT:
                if(backgroundCollidable.collide(word))  return false; 
                LOGGER.info("place: " + word.getWord());
                placedWords.add(word);
                return true;

        }
        return false;
    }

    protected List<Word> buildwords(final List<WordFrequency> wordFrequencies, final ColorPalette colorPalette) {
        final int maxFrequency = maxFrequency(wordFrequencies);

        final List<Word> words = new ArrayList<>();
        for(final WordFrequency wordFrequency : wordFrequencies) {
            if (wordFrequency.getWord().length() > 3) 
            	words.add(buildWord(wordFrequency, maxFrequency, colorPalette));
        }
        return words;
    }

    private Word buildWord(final WordFrequency wordFrequency, int maxFrequency, final ColorPalette colorPalette) {
        final Graphics graphics = this.bufferedImage.getGraphics();

        final int frequency = wordFrequency.getFrequency();
        final float fontHeight = this.fontScalar.scale(frequency, 0, maxFrequency);
        final Font font = cloudFont.getFont().deriveFont(fontHeight);

        final FontMetrics fontMetrics = graphics.getFontMetrics(font);
        final Word word = new Word(wordFrequency.getWord(), colorPalette.next(), fontMetrics, this.collisionChecker);

        final double theta = angleGenerator.randomNext();
        if(theta != 0) 
            word.setBufferedImage(ImageRotation.rotate(word.getBufferedImage(), theta));
        
        if(padding > 0)    padder.pad(word, padding); 
        return word;
    }

    private int maxFrequency(final List<WordFrequency> wordFrequencies) {
        if(wordFrequencies.isEmpty()) { return 1; }
        return wordFrequencies.get(0).getFrequency();  //Lambda.max(wordFrequencies, on(WordFrequency.class).getFrequency());
    }

    public void setBackgroundColor(Color c) 		{        backgroundColor = c;    }
    public void setPadding(int p) 					{        padding = p;    }
    public void setColorPalette(ColorPalette cp) 	{        colorPalette = cp;   }
    public void setBackground(Background bg) 		{        background = bg;    }
    public void setFontScalar(FontScalar s) 		{        fontScalar = s;    }
    public void setCloudFont(CloudFont f) 			{        cloudFont = f;    }
    public void setAngleGenerator(AngleGenerator ag){        angleGenerator = ag;    }
    public BufferedImage getBufferedImage()			{        return bufferedImage;    }
    public Set<Word> getSkipped() 					{        return skipped;    }
}
