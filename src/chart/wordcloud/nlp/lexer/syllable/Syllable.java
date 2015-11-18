package chart.wordcloud.nlp.lexer.syllable;

/**
 * Created by kenny on 3/11/14.
 */
public class Syllable {

    private final String text;

    public Syllable(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Syllable{" +
                "text='" + text + '\'' +
                '}';
    }

}

