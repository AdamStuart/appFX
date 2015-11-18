package chart.wordcloud.nlp.lexer.sentence;

/**
 * Created by kenny on 3/11/14.
 */
public class Sentence {

    public final String text;

    public Sentence(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Sentence{" +
                "text='" + text + '\'' +
                '}';
    }

}
