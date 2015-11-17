package chart.wordcloud.nlp.normalize;

import util.StringUtil;

/**
 * Created by kenny on 7/1/14.
 */
public class UpperCaseNormalizer implements Normalizer {
    @Override
    public String normalize(final String text) {
        return StringUtil.capitalize(text);
    }
}
