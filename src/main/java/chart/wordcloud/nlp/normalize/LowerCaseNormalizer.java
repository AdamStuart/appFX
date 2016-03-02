package chart.wordcloud.nlp.normalize;

import util.StringUtil;

/**
 * Created by kenny on 7/1/14.
 */
public class LowerCaseNormalizer implements Normalizer {
    @Override
    public String normalize(final String text) {
        return text.toLowerCase();
    }
}
