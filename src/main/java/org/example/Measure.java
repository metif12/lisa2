package org.example;

import org.apache.commons.io.IOUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Measure {
    public Measure() {
        HashMap<String, String> queries = new HashMap<>();
        ArrayList<QueryWithDoc> qwd = new ArrayList<>();

        InputStream queriesInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.QUE");
        if (queriesInputStream != null) {
            String queriesCorpus = IOUtil.toString(queriesInputStream);

            var startQuery = 0;

            while (true){

                var endQuery = queriesCorpus.indexOf('#', startQuery);

                if(endQuery == -1) break;

                String rawQuery = queriesCorpus.substring(startQuery, (endQuery - 1)).trim();
                var endId = rawQuery.indexOf("\r\n");
                var idQuery = rawQuery.substring(0,endId).trim();
                var textQuery = rawQuery.substring((endId+2),(rawQuery.length()-1)).trim();

                startQuery = endQuery+1;

                queries.put(idQuery,textQuery);
            }
        }

        InputStream queriesDocInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.QUE");
        if (queriesDocInputStream != null) {
            String queriesAnswersCorpus = IOUtil.toString(queriesDocInputStream);

            var startQueryAnswers = 0;

            while (true){

                var endQueryAnswers = queriesAnswersCorpus.indexOf("-1\r\n", startQueryAnswers);

                if(endQueryAnswers == -1) break;

                var queryAnswers = queriesAnswersCorpus.substring(startQueryAnswers,(endQueryAnswers-1)).trim().replace("Query ","").replace(" Relevant Refs: ","");
                var endAnswersId = queryAnswers.indexOf("\r\n");
                var idAnswers = queryAnswers.substring(0,endAnswersId).trim();
                var textAnswers = queryAnswers.substring((endAnswersId+2),(queryAnswers.length()-1)).trim();

                startQueryAnswers = endQueryAnswers+4;

                var q = queries.get(idAnswers);

                if(q != null){
                    qwd.add(new QueryWithDoc(idAnswers, q, textAnswers.split(" ")));
                }
            }
        }
    }
}
