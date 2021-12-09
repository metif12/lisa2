package org.example;

public class ScoreUtil {

    private double cosineScore;
    private double likelihoodScore;
    private double bm25Score;

    private String exID;

    public ScoreUtil(MyTermVec docVec, MyTermVec queVec, double avgFieldLength, float k1, float b) {

        exID = docVec.getExternalId();

        double sumCosine = 0;
        double sumBM25 = 0;
        double prodLikelihoodProbability = 1;

        for (String t : queVec.getTerms()) {

            //if term is in both query and doc
            if (docVec.getTerms().contains(t)) {

                var idf_d = docVec.getInverseDocFrequency(t);
                var tf_d = docVec.getTermFrequency(t);

                var tf_q = queVec.getTermFrequency(t);

                sumCosine += (tf_d * tf_q * idf_d) / (docVec.getVecLength() * queVec.getVecLength());
                sumBM25 += (idf_d * tf_d * (k1+1)) / (tf_d + k1 * (1-b+b*avgFieldLength));
                prodLikelihoodProbability *= (tf_d / docVec.getDocLength());
            }
        }


        cosineScore = sumCosine;
        likelihoodScore = prodLikelihoodProbability;
        bm25Score = sumBM25;

//        System.out.println(exID + ":" + cosineScore + ", " + likelihoodScore + ", " + bm25Score);
    }

    public Score getCosineScore(){
        return new Score(exID, cosineScore);
    }

    public Score getLikelihoodScore(){
        return new Score(exID, likelihoodScore);
    }

    public Score getBM25Score(){
        return new Score(exID, bm25Score);
    }
}
