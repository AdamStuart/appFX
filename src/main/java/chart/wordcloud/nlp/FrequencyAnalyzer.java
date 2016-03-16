package chart.wordcloud.nlp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import chart.wordcloud.nlp.filter.CompositeFilter;
import chart.wordcloud.nlp.filter.Filter;
import chart.wordcloud.nlp.filter.StopWordFilter;
import chart.wordcloud.nlp.filter.WordSizeFilter;
import chart.wordcloud.nlp.normalize.CharacterStrippingNormalizer;
import chart.wordcloud.nlp.normalize.LowerCaseNormalizer;
import chart.wordcloud.nlp.normalize.Normalizer;
import chart.wordcloud.nlp.normalize.TrimToEmptyNormalizer;
import chart.wordcloud.nlp.tokenizer.WhiteSpaceWordTokenizer;
import chart.wordcloud.nlp.tokenizer.WordTokenizer;
import util.FileUtil;

/**
 * Created by kenny on 7/1/14.
 */
public class FrequencyAnalyzer {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int DEFAULT_WORD_MAX_LENGTH = 32;
    public static final int DEFAULT_WORD_MIN_LENGTH = 3;
    public static final int DEFAULT_WORD_FREQUENCIES_TO_RETURN = 350;
    public static final long DEFAULT_URL_LOAD_TIMEOUT = 3000; // 3 sec

    private final Set<String> stopWords = new HashSet<>();
    private WordTokenizer wordTokenizer = new WhiteSpaceWordTokenizer();
//    private final List<Filter> filters = new ArrayList<>();
    private final List<Normalizer> normalizers = new ArrayList<>();

    private int wordFrequencesToReturn = DEFAULT_WORD_FREQUENCIES_TO_RETURN;
    private int maxWordLength = DEFAULT_WORD_MAX_LENGTH;
    private int minWordLength = DEFAULT_WORD_MIN_LENGTH;
    private String characterEncoding = DEFAULT_ENCODING;
    private long urlLoadTimeout = DEFAULT_URL_LOAD_TIMEOUT;

    public FrequencyAnalyzer() {
        normalizers.add(new TrimToEmptyNormalizer());
        normalizers.add(new CharacterStrippingNormalizer());
        normalizers.add(new LowerCaseNormalizer());
    }

    public List<WordFrequency> load(File file) throws IOException {
    	return load(FileUtil.readFileIntoStringList(file.getAbsolutePath()));
    }

    public List<WordFrequency> load(String url) throws IOException 
    {
    	final Document doc = Jsoup.connect(url).get();
    	return load(Collections.singletonList(doc.body().text()));
    }

    static String[] prefixes = new String[]{"cd", "tnf", "il", "ig", "lsr", "ki"};		// TODO -- hacking biomarker patterns
    static String[] suffixes = new String[]{"+", "-"};
    public static boolean isBiomarker(String str)
    {
    	for (String s : prefixes)
    		if (str.startsWith(s))        	return true;
    	for (String s : suffixes)
    		if (str.endsWith(s))        	return true;

    	return false;
    	
    }
    public List<WordFrequency> load(final List<String> texts) {
        final List<WordFrequency> wordFrequencies = new ArrayList<>();

        final Map<String, Integer> cloud = buildWordFrequencies(texts, wordTokenizer);
        for(Map.Entry<String, Integer> wordCount : cloud.entrySet()) {
        	String key = wordCount.getKey();
        	int val = wordCount.getValue();
        	int weight =  isBiomarker(key) ? 10 : 1;
        	WordFrequency wf = new WordFrequency(key, val, weight);
            wordFrequencies.add(wf);
        }
        List<WordFrequency> top = takeTopFrequencies(wordFrequencies);
       StringBuilder builder = new StringBuilder();
       for (WordFrequency wf : top)
        	builder.append(", "  + wf);
       System.out.println(builder.toString());
        return top;
    }

    private Map<String, Integer> buildWordFrequencies(List<String> texts, WordTokenizer tokenizer) {
        final Map<String, Integer> wordFrequencies = new HashMap<>();
        for(final String text : texts) {
            final List<String> words = filter(tokenizer.tokenize(text));

            for(final String word : words) {
                final String normalized = normalize(word);
                Integer freq = wordFrequencies.get(normalized);
                if (freq == null) 
                    wordFrequencies.put(normalized, 1);
                else wordFrequencies.put(normalized, freq + 1);
            }
        }
        return wordFrequencies;
    }
    String[] stop = new String[]{"its", "it's", "very", "will", "in", "out", "ago", "one", "have", "one", "one", 
    				"for", "this", "all", "that", "your", "you", "his", "her", "my", "our",
    				"but", "has", "the", "and", "or", "to", "not", "is", "are",  "cell", "cells", "were", "been",
    				"it", "if", "a", "an", "as", "with", "we", "they", "from", "into", "this", "also"};
    List<String> stopWordList = Arrays.asList(stop);

    private List<String> filter(final List<String> words) {
        final List<Filter> allFilters = new ArrayList<>();
        addStopWords(stopWordList); 
        allFilters.add(new StopWordFilter(stopWords));
        allFilters.add(new WordSizeFilter(minWordLength, maxWordLength));
//        allFilters.addAll(filters);
        final CompositeFilter compositeFilter = new CompositeFilter(allFilters);
        
        List<String> out = new ArrayList<String>();
        for (String word : words)
        	if (compositeFilter.test(word))
        		out.add(word);
        return out;
    }

    private String normalize(final String word) {
        String normalized = word;
        for(Normalizer normalizer : normalizers) 
            normalized = normalizer.normalize(normalized);
        return normalized;
    }

    private List<WordFrequency> takeTopFrequencies(List<WordFrequency> wordCloudEntities) {
   
    	if(wordCloudEntities.isEmpty()) { return Collections.emptyList(); }
//        for (WordFrequency wf : wordCloudEntities)
//        	System.out.println(""  + wf);
      Collections.sort(wordCloudEntities);
//        Collections.reverse(wordCloudEntities);
        for (WordFrequency wf : wordCloudEntities)
        	System.out.println(""  + wf);
       return wordCloudEntities.subList(0, Math.min(wordCloudEntities.size(), wordFrequencesToReturn));
    }

    public void addStopWords(Collection<String> wds) 	{     stopWords.addAll(wds);    }
    public void setStopWords(Collection<String> wds) 	{     stopWords.clear();     stopWords.addAll(wds);    }
    public void setWordFrequencesToReturn(int toReturn) {     wordFrequencesToReturn = toReturn;    }
    public void setMinWordLength(int len) 				{	  minWordLength = len;    }
    public void setMaxWordLength(final int len)			{     maxWordLength = len;    }
    public void setWordTokenizer(WordTokenizer tokenzr) {     wordTokenizer = tokenzr;    }
    
    // I provided access to stop words instead of a user filter list, but this may be worth restoring
//    public void clearFilters() 							{        filters.clear();    }
//    public void addFilter(final Filter filter) 			{        filters.add(filter);    }
//    public void setFilter(final Filter filter) {    filters.clear();       filters.add(filter);    }

    public void clearNormalizers() 							{   normalizers.clear();    }
    public void addNormalizer(final Normalizer normalizer) 	{   normalizers.add(normalizer);    }
    public void setNormalizer(final Normalizer normalizer) 	{   normalizers.clear();       normalizers.add(normalizer);    }

    public void setCharacterEncoding(String encoding) {        characterEncoding = encoding;    }
    public void setUrlLoadTimeout(final long timeout) {        urlLoadTimeout = timeout;    }
}
