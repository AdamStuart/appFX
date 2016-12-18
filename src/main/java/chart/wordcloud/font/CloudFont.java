package chart.wordcloud.font;

import java.awt.Font;

/**
 * Created by kenny on 7/3/14.
 */
public class CloudFont {

    private static final int DEFAULT_SIZE = 10;
    private final Font font;

    public CloudFont(String type, FontWeight weight) {
        font = new Font(type, weight.getWeight(), DEFAULT_SIZE);
    }

    public CloudFont(Font f) {        font = f;    }
    public Font getFont() {        return font;    }

}
