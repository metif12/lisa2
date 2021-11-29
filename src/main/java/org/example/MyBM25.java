package org.example;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.similarities.SimilarityBase;

public class MyQueryLikelihood extends SimilarityBase {

    @Override
    protected double score(BasicStats basicStats, double freq, double docLength) {
        return basicStats.getBoost() * freq/docLength;
    }

    @Override
    public String toString() {
        return null;
    }
}
