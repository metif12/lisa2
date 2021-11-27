package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String s) {
        TokenStreamComponents standard_ts = new TokenStreamComponents(new StandardTokenizer());
        TokenStreamComponents lowercase_ts = new TokenStreamComponents(standard_ts.getSource(), new LowerCaseFilter(standard_ts.getTokenStream()));
        StopFilter stopFilter = new StopFilter(lowercase_ts.getTokenStream(), EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        TokenStreamComponents stopword_ts = new TokenStreamComponents(lowercase_ts.getSource(), stopFilter);
        TokenStreamComponents stemmer_ts = new TokenStreamComponents(stopword_ts.getSource(), new KStemFilter(stopword_ts.getTokenStream()));
        return new TokenStreamComponents(stemmer_ts.getSource(), new PorterStemFilter(stemmer_ts.getTokenStream()));
    }
}