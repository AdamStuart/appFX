package chart.wordcloud.nlp.filter;

import java.util.function.Predicate;

/**
 * Created by kenny on 7/1/14.
 */
public abstract class Filter implements Predicate<String> {
    public abstract boolean test(String word);
    
}

