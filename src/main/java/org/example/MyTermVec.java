package org.example;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MyTermVec {

    private ArrayList<String> terms;
    private HashMap<String, Double> termFrequency;
    private HashMap<String, Double> inverseDocFrequency;
    private double vecLength;
    private double docLength;

    private String field;
    private IndexReader indexReader;

    private String externalId;

    private String[] relevant;

    public MyTermVec(IndexReader indexReader, String exId, Terms vector, String field) throws IOException {

        this.indexReader = indexReader;
        this.field = field;
        this.externalId = exId;
        this.terms = new ArrayList<>();
        this.relevant = new String[]{};
        this.termFrequency = new HashMap<>();
        this.inverseDocFrequency = new HashMap<>();


        var sumOfSquaredTF = 0;

        if(vector != null && vector.size() > 0) {

            TermsEnum terms = vector.iterator();

            BytesRef bytesRef;
            int N = indexReader.numDocs();

            while ((bytesRef = terms.next()) != null) {

                var t = bytesRef.utf8ToString();

                this.terms.add(t);

                long tf = terms.totalTermFreq();
                var tf_d = (tf > 0) ? Math.log10(tf)+1 : 0;
                termFrequency.put(t,tf_d);
                docLength += tf_d;

                var df_d = indexReader.docFreq(new Term(field, t));
                var idf_d = Math.log10((double) N / df_d);

                inverseDocFrequency.put(t,idf_d);

                sumOfSquaredTF += Math.pow(tf_d,2);
            }
        }

        vecLength = Math.sqrt(sumOfSquaredTF);
    }

    public MyTermVec(IndexReader indexReader, String exId, ArrayList<String> terms, String[] relevant, HashMap<String, Integer> frequency, String field) throws IOException {

        this.indexReader = indexReader;
        this.field = field;
        this.externalId = exId;
        this.terms = terms;
        this.relevant = relevant;
        this.termFrequency = new HashMap<>();
        this.inverseDocFrequency = new HashMap<>();

        var sumOfSquaredTF = 0;
        int N = indexReader.numDocs();

        for (String t : terms) {

            Integer tf = frequency.get(t);
            var tf_d = (tf > 0) ? Math.log10(tf) +1 : 0;
            docLength += tf_d;
            this.termFrequency.put(t,tf_d);

            var df_d = indexReader.docFreq(new Term(field, t));
            var idf_d = Math.log10((double) N / df_d);
            inverseDocFrequency.put(t,idf_d);

            sumOfSquaredTF += Math.pow(tf_d ,2);
        }

        vecLength = Math.sqrt(sumOfSquaredTF);
    }

    public double getDocLength() {
        return docLength;
    }

    public double getTermFrequency(String term){
        return termFrequency.get(term);
    }

    public double getInverseDocFrequency(String term){
        return inverseDocFrequency.get(term);
    }

    public double getTermFrequency(BytesRef bytesRef){

        Term term = new Term(field, bytesRef);

        return termFrequency.get(term.text());
    }

    public double getInverseDocFrequency(BytesRef bytesRef){

        Term term = new Term(field, bytesRef);

        return inverseDocFrequency.get(term.text());
    }

    public ArrayList<String> getTerms() {
        return terms;
    }

    public double getVecLength() {
        return vecLength;
    }

    public String getExternalId() {
        return externalId;
    }

    public String[] getRelevant() {
        return relevant;
    }
}
