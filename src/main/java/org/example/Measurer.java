package org.example;

import org.apache.commons.io.IOUtil;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Measurer {

    ArrayList<MyTermVec> queries;
    ArrayList<MyTermVec> docs;
    Engine engine;
    IndexReader indexReader;

    public Measurer(Engine engine) {

        this.engine = engine;
        docs = new ArrayList<>();
        queries = new ArrayList<>();
        indexReader = engine.getDirectoryReader();
    }

    public void run() throws IOException {
        loadQueries();

        int N = indexReader.numDocs();

        System.out.println("N=" + N);

        var sumFieldLength = 0;

        //for calc avgFieldLength
        for (int docId = 0; docId < indexReader.maxDoc(); docId++) {

            var docU = new DocumentUtil(indexReader, docId);

            var docVec = docU.getDocVec();

            sumFieldLength += docVec.getDocLength();

            docs.add(docVec);
        }

        var avgFieldLength = sumFieldLength / N;

        System.out.println("avgDL=" + avgFieldLength);

        for (var query : queries) {

            var scoresInCosine = new ArrayList<Score>();
            var scoresInLikelihood = new ArrayList<Score>();
            var scoresInBM25 = new ArrayList<Score>();

            for (var doc : docs) {

                var scoreUtil = new ScoreUtil(doc, query, avgFieldLength, 1.2f, 0.75f);

                scoresInCosine.add(scoreUtil.getCosineScore());
                scoresInLikelihood.add(scoreUtil.getLikelihoodScore());
                scoresInBM25.add(scoreUtil.getBM25Score());
            }

            var calcCosine = new Calculator(scoresInCosine, query.getRelevant());
            var calcLikelihood = new Calculator(scoresInLikelihood, query.getRelevant());
            var calcBM25 = new Calculator(scoresInBM25, query.getRelevant());

            StringBuilder rel = new StringBuilder();
            for (String r : query.getRelevant()) rel.append(r).append(",\t");

            System.out.println("---------------------------------------------------------------------");
            System.out.println("Query(" + query.getExternalId() + "):\n" + rel);
            System.out.println();
            System.out.println("Cosine:\n" + calcCosine);
            System.out.println();
            System.out.println("Likelihood:\n" + calcLikelihood);
            System.out.println();
            System.out.println("BM25:\n" + calcBM25);
            System.out.println();
        }
    }

    private void loadQueries() throws IOException {

        HashMap<String, String> queryIdToTextMap = new HashMap<>();

        readQueries(queryIdToTextMap);

        InputStream queriesDocInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.REL");
        if (queriesDocInputStream != null) {
            String queriesAnswersCorpus = IOUtil.toString(queriesDocInputStream);

            for(String rawQAnswer: queriesAnswersCorpus.split(" -1\\r\\n\\r\\n")){

                var queryAnswers = rawQAnswer.replace("Query ", "");

                var endAnswersId = queryAnswers.indexOf("\r\n", 0);
                var idAnswers = queryAnswers.substring(0, endAnswersId);

                var startAnswers = queryAnswers.lastIndexOf("\r\n") + 2;
                var textAnswers = queryAnswers.substring(startAnswers);

                var q = queryIdToTextMap.get(idAnswers);

                if (q != null) {
                    var queryUtil = new QueryUtil(indexReader, idAnswers, q, textAnswers.split(" "));
                    queries.add(queryUtil.getQueVec());
                }
            }
        }
    }

    private void readQueries(HashMap<String, String> queries) throws IOException {
        InputStream queriesInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.QUE");
        if (queriesInputStream != null) {
            String queriesCorpus = IOUtil.toString(queriesInputStream);

            for(var rawQ: queriesCorpus.split("#\\r\\n")){

                var endId = rawQ.indexOf("\r\n");
                var idQuery = rawQ.substring(0, endId);
                var textQuery = rawQ.substring((endId + 2));

                queries.put(idQuery, textQuery);
            }
        }
    }
}
