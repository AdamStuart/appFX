package chart.wordcloud.nlp.lexer.sentence;

import java.util.List;

/**
 * Created by kenny on 5/28/14.
 */
public interface SentenceLexer {

    List<Sentence> tokenize(String text);
}
