package org.example;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.tartarus.snowball.ext.KpStemmer;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.util.*;

public class QueryUtil {

    private final String externalId;
    private final String query;
    private final String[] relevant;

    private MyTermVec queryVec;
    IndexReader indexReader;

    public QueryUtil(IndexReader indexReader, String id, String query, String[] relevant) throws IOException {
        this.externalId = id;
        this.query = query;
        this.indexReader = indexReader;
        this.relevant = relevant;

        queryVec = parseTerms(query);
    }

    private MyTermVec parseTerms(String text) throws IOException {
//        var kStemmer = new KpStemmer();
        var porterStemmer = new PorterStemmer();

        var tokens = text
                .toLowerCase()
                .replace("\r\n", " ")
                .replace(".", " ")
                .replace(",", " ")
                .split(" ");

        ArrayList<String> terms = new ArrayList<>();
        HashMap<String, Integer> frequency = new HashMap<>();

        for (var token : tokens) {
            if (!token.equals("") && !EnglishAnalyzer.ENGLISH_STOP_WORDS_SET.contains(token)) {

                porterStemmer.setCurrent(token);

                var current = porterStemmer.getCurrent();

                if(!terms.contains(current)) {
                    terms.add(current);
                    frequency.put(current, 1);
                }
                else {
                    frequency.replace(current, frequency.get(current) +1);
                }
            }
        }

        return new MyTermVec(indexReader, externalId, terms, relevant, frequency, "content");
    }

    public String getQuery() {
        return query;
    }

    public String getExternalId() {
        return externalId;
    }

    public String[] getRelevant() {
        return relevant;
    }

    @Override
    public String toString() {
        return "QueryWithDoc{" +
                "id='" + externalId + '\'' +
                ", query=" + query + '\'' +
                ", docs=" + Arrays.toString(relevant) +
                '}';
    }

    public MyTermVec getQueVec() {
        return queryVec;
    }
}
