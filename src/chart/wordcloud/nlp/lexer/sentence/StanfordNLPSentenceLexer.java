package chart.wordcloud.nlp.lexer.sentence;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

//http://nlp.stanford.edu/software/corenlp.shtml
/**
 * Created by kenny on 3/11/14.
 *
 * A naive version
 */
public class StanfordNLPSentenceLexer implements SentenceLexer {

    @Override
    public List<Sentence> tokenize(String text) {
        List<Sentence> sentences = new ArrayList<>();
        final DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : dp) 
            sentences.add(new Sentence(sentence.toString()));
        return sentences;
    }

}
