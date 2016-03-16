package chart.wordcloud.nlp;

import java.util.Iterator;
import java.util.List;

import chart.wordcloud.nlp.lexer.sentence.Sentence;
import chart.wordcloud.nlp.lexer.sentence.SentenceLexer;
import chart.wordcloud.nlp.lexer.sentence.SimpleSentenceLexer;
import chart.wordcloud.nlp.lexer.syllable.SyllableLexer;
import chart.wordcloud.nlp.lexer.word.Word;
import chart.wordcloud.nlp.lexer.word.WordLexer;

/**
 * Created by kenny on 3/11/14.
 */
public class FleschKincaid {

    private static final int MIN_ALLOWED_SENTENCE_LENGTH = 10;

    private SentenceLexer sentenceLexer;
    final SyllableLexer syllableLexer;
    private final WordLexer wordLexer;

    public FleschKincaid() {
        sentenceLexer = new SimpleSentenceLexer();
        syllableLexer = new SyllableLexer();
        wordLexer = new WordLexer();
    }

    public double calculate(String text) {
        List<Sentence> sentences = filterInvalidSentences(this.sentenceLexer.tokenize(text));
        int syllableCount = countSyllables(sentences);
        int wordCount = countWords(sentences);

        //calculate flesch index
        final double f1 = 206.835;
        final double f2 = 84.6;
        final double f3 = 1.015;
        double r1 = (double) syllableCount / (double) wordCount;
        double r2 = (double) wordCount / (double) sentences.size();

        return f1 - (f2 * r1) - (f3 * r2);
    }

    private List<Sentence> filterInvalidSentences(List<Sentence> sentences) {
        Iterator<Sentence> iterator = sentences.iterator();
        while(iterator.hasNext()) {
            Sentence sentence = iterator.next();
            if(sentence.getText().length() < MIN_ALLOWED_SENTENCE_LENGTH) 
                iterator.remove();
        }
        return sentences;
    }

    private int countWords(List<Sentence> sentences) {
        int count = 0;
        for(Sentence sentence : sentences) 
            count += wordLexer.tokenize(sentence).size();
        return count;
    }

    private int countSyllables(List<Sentence> sentences) {
        int count = 0;
        for(Sentence sentence : sentences) 
            for(Word word : wordLexer.tokenize(sentence)) 
                count += syllableLexer.count(word);
        return count;
    }

    public void setSentenceLexer(SentenceLexer lex) {    sentenceLexer = lex;    }

}
