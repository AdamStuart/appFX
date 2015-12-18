package chart.wordcloud.nlp;

/**
 * Created by kenny on 6/29/14.
 */
public class WordFrequency implements Comparable<WordFrequency> {
    private final String word;
    private final int frequency;
    private final int weight;

    public WordFrequency(String word, int frequency, int weight) {
        this.word = word;
        this.frequency = frequency;
        this.weight = weight;
    }

    public String getWord() 		{        return word;    }
    public int getFrequency()		{        return frequency * weight;    }
    @Override    public int compareTo(WordFrequency wordFrequency) {   return wordFrequency.frequency * weight - frequency * weight;    }
    @Override    public String toString() { return word + ": " + frequency;  }
}
