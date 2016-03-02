package chart.wordcloud.nlp.test.java.com.fleschkincaid.lexer;

import org.junit.Test;

import chart.wordcloud.nlp.lexer.syllable.SyllableLexer;
import chart.wordcloud.nlp.lexer.word.Word;

/**
 * Created by kenny on 3/12/14.
 */
public class TestSyllableLexer {

    @Test
    public void countTest() {
        count("kawazaki");
        count("am");
        count("going");
        count("away");
        count("syllable");
        count("arrangement");
        count("strange");
    }

    private void count(String word) {
        System.out.println("counting syllables: " + word);
        SyllableLexer syllableLexer = new SyllableLexer();
        System.out.println(syllableLexer.count(new Word(word)));
    }

}
