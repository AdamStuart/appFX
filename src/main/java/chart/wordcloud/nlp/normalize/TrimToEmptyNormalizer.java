package chart.wordcloud.nlp.normalize;

/**
 * Created by kenny on 7/1/14.
 */
public class TrimToEmptyNormalizer implements Normalizer {			// AST changed -- was apache.trimtoempty
    @Override
    public String normalize(final String text) {
        return text.trim();
    }
}
