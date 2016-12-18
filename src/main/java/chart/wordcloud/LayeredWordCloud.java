package chart.wordcloud;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import chart.wordcloud.bg.Background;
import chart.wordcloud.collide.CollisionMode;
import chart.wordcloud.font.CloudFont;
import chart.wordcloud.font.scale.FontScalar;
import chart.wordcloud.image.AngleGenerator;
import chart.wordcloud.nlp.WordFrequency;

/**
 * Created by kenny on 7/5/14.
 */
public class LayeredWordCloud {

    private static final Logger LOGGER = Logger.getLogger("LayeredWordCloud");

    private final double width;
    private final double height;
    private final List<WordCloud> wordClouds = new ArrayList<>();

    private Color backgroundColor = Color.BLACK;

    public LayeredWordCloud(int layers, int w, int h, CollisionMode collisionMode) {
        width = w;
        height = h;
        for(int i = 0; i < layers; i++) {
            final WordCloud wordCloud = new WordCloud(width, height, collisionMode);
            wordCloud.setBackgroundColor(null);
            wordClouds.add(wordCloud);
        }
    }
    private WordCloud get(int i) { return wordClouds.get(i);	}
    public void build(int layer, List<WordFrequency> f) {       get(layer).build(f);    }
    public void setPadding(int layer, int padding) {        get(layer).setPadding(padding);    }
    public void setColorPalette(int layer, ColorPalette cp) {        get(layer).setColorPalette(cp);  }
    public void setBackground(int layer, Background bg) {        get(layer).setBackground(bg);    }
    public void setFontScalar(int layer, FontScalar x) {        get(layer).setFontScalar(x);    }
    public void setFontOptions(int layer, CloudFont f) {        get(layer).setCloudFont(f);    }
    public void setAngleGenerator(int layer, AngleGenerator ag) {       get(layer).setAngleGenerator(ag);
    }

    public void setBackgroundColor(Color bg) {        backgroundColor = bg;    }

    public BufferedImage getBufferedImage() {
        final BufferedImage bufferedImage = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, (int)width, (int)height);
        for(WordCloud wordCloud : wordClouds) 
            graphics.drawImage(wordCloud.getBufferedImage(), 0, 0, null);
        return bufferedImage;
    }
//
//    public void writeToFile(final String outputFileName) {
//        String extension = "";
//        int i = outputFileName.lastIndexOf('.');
//        if (i > 0) {
//            extension = outputFileName.substring(i + 1);
//        }
//        try {
//            LOGGER.info("Saving Layered WordCloud to " + outputFileName);
//            ImageIO.write(getBufferedImage(), extension, new File(outputFileName));
//        } catch (IOException e) {
//            LOGGER.severe(e.getMessage() +  e);
//        }
//    }

}
