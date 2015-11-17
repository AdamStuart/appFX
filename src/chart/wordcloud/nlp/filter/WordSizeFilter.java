package chart.wordcloud.nlp.filter;

/**
 * Created by kenny
 */
public class WordSizeFilter extends Filter {

    private final int minLength;

    private final int maxLength;

    public WordSizeFilter(final int minLength, final int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean test(String word) {
        return word != null
                && word.length() >= minLength
                && word.length() < maxLength;
    }

}
