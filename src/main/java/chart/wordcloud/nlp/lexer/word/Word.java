package chart.wordcloud.nlp.lexer.word;

/**
 * Created by kenny on 3/11/14.
 */
public class Word {

    private final String text;

    public Word(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Word{" +
                "text='" + text + '\'' +
                '}';
    }

}

