package chart.wordcloud.nlp.lexer.word;

import java.util.LinkedList;
import java.util.List;

import chart.wordcloud.nlp.lexer.sentence.Sentence;

/**
 * Created by kenny on 3/11/14.
 */
public class WordLexer {

    public List<Word> tokenize(Sentence sentence) {
        List<Word> words = new LinkedList<>();

        String[] parsedWords = sentence.getText().split("\\s+");
        for (String word : parsedWords) {
            words.add(new Word(word));
        }

        return words;
    }

}
