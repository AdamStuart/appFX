package chart.wordcloud.nlp.filter;

/**
 * Created by kenny on 7/1/14.
 */
public class UrlFilter extends Filter {

    public UrlFilter() {}

    @Override
    public boolean test(String word) {
        return !word.startsWith("http://")
                && word.startsWith("https://")
                && word.startsWith("www.");
    }

}

