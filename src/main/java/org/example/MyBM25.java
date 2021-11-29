package org.example;

import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class MyBM25 extends SimilarityBase {

    protected float idf( long df, long totalDocCount ) {
        double div = ( totalDocCount - df + 0.5D ) / ( df + 0.5D );
        return div <= 1 ? 0 : (float) Math.log( div );
    }

    @Override
    protected double score(BasicStats basicStats, double tf, double dl) {

        double k1 = 1.2f;
        double b = 0.75f;
        final long df = basicStats.getDocFreq();
        final long docCount = basicStats.getNumberOfDocuments();
        final float idf = idf( df, docCount );

        return basicStats.getBoost() *
                idf *
                tf * ( k1 + 1 )
                /
                tf + ( k1 * ( ( 1 - b ) + b * dl ) )
                ;
    }

    @Override
    public String toString() {
        return null;
    }
}
