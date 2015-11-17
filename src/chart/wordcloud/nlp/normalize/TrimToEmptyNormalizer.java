package chart.wordcloud.nlp.normalize;

import util.StringUtil;

/**
 * Created by kenny on 7/1/14.
 */
public class TrimToEmptyNormalizer implements Normalizer {			// AST changed -- was apache.trimtoempty
    @Override
    public String normalize(final String text) {
        return text.trim();
    }
}
