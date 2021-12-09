package ir.lisa;

import org.apache.commons.io.IOUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.example.Main;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LISA {

    Directory directory;
    DirectoryReader directoryReader;
    Analyzer analyzer;
    Path indexPath;

    ArrayList<Query> queries = new ArrayList();

    class Query {
        public String text;
        public String exID;
        public HashMap<String, Integer> termFrequencies;
        public ArrayList<String> relevantDocExIDs;
        public ArrayList<String> terms;

        public Query(String text, String exID, String[] relevantDocExIDs) {
            this.text = text;
            this.exID = exID;
            this.relevantDocExIDs = new ArrayList<String>(List.of(relevantDocExIDs));
            this.termFrequencies = new HashMap<>();
            this.terms = new ArrayList<>();

            var porterStemmer = new PorterStemmer();

            var tokens = text
                    .toLowerCase()
                    .replace("\r\n", " ")
                    .replace(".", " ")
                    .replace(",", " ")
                    .split(" ");

            for (var token : tokens) {
                if (!token.equals("") && !EnglishAnalyzer.ENGLISH_STOP_WORDS_SET.contains(token)) {

                    porterStemmer.setCurrent(token);

                    var current = porterStemmer.getCurrent();

                    if (!terms.contains(current)) {
                        terms.add(current);
                        termFrequencies.put(current, 1);
                    } else {
                        termFrequencies.replace(current, termFrequencies.get(current) + 1);
                    }
                }
            }
        }
    }

    class MyAnalyzer extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(String s) {
            TokenStreamComponents standard_ts = new TokenStreamComponents(new StandardTokenizer());
            TokenStreamComponents lowercase_ts = new TokenStreamComponents(standard_ts.getSource(), new LowerCaseFilter(standard_ts.getTokenStream()));
            StopFilter stopFilter = new StopFilter(lowercase_ts.getTokenStream(), EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            TokenStreamComponents stopword_ts = new TokenStreamComponents(lowercase_ts.getSource(), stopFilter);
            return new TokenStreamComponents(stopword_ts.getSource(), new PorterStemFilter(stopword_ts.getTokenStream()));
        }
    }

    class Score {
        double score;
        String exID;

        public Score(double score, String exID) {
            this.score = score;
            this.exID = exID;
        }
    }

    public LISA() throws IOException {

        indexPath = Path.of("index");
        analyzer = new MyAnalyzer();

        if (!Files.exists(indexPath))
            Files.createDirectory(indexPath);

        directory = FSDirectory.open(indexPath);


        buildIndex();

        directoryReader = DirectoryReader.open(directory);
    }

    private IndexWriterConfig getConfig(){
        var config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        return config;
    }

    private void close() throws IOException {
        directoryReader.close();
        directory.close();
    }

    private void buildIndex() throws IOException {

        var indexWriter = new IndexWriter(directory, getConfig());

        indexWriter.commit();

        parseCorpus(indexWriter);

        indexWriter.commit();
        indexWriter.close();
    }

    private void parseCorpus(IndexWriter indexWriter) throws IOException {
        var corpus_names = new String[]{
                "LISA0.001",
                "LISA0.501",
                "LISA1.001",
                "LISA1.501",
                "LISA2.001",
                "LISA2.501",
                "LISA3.001",
                "LISA3.501",
                "LISA4.001",
                "LISA4.501",
                "LISA5.001",
                "LISA5.501",
                "LISA5.627",
                "LISA5.850",
        };

        var contentFieldType = new FieldType();

        contentFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        contentFieldType.setTokenized(true);
        contentFieldType.setStored(true);
        contentFieldType.setStoreTermVectors(true);
        contentFieldType.freeze();

        for (String corpus_name : corpus_names) {
            InputStream inputStream = Main.class.getResourceAsStream("\\..\\..\\" + corpus_name);
            if (inputStream != null) {
                String text = IOUtil.toString(inputStream);

                for (String rawDoc : text.split("\\r\\n\\*{44}\\r\\n")) {

                    String d = rawDoc.replaceAll("\\r\\n *?\\r\\n", "\r\n\r\n");
                    int endID = d.indexOf("\r");
                    String id = d.substring(0, endID).replace("Document", "").trim();
                    String content = d.substring(endID + 2);

                    Document doc = new Document();
                    doc.add(new Field("id", id, StringField.TYPE_STORED));
                    doc.add(new Field("content", content, contentFieldType));
                    indexWriter.addDocument(doc);
                }
            }
        }
    }

    private void loadQueries() throws IOException {

        HashMap<String, Query> queryHashMap = new HashMap<>();

        InputStream queriesInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.QUE");
        InputStream queriesDocInputStream = Main.class.getResourceAsStream("\\..\\..\\LISA.REL");

        if (queriesInputStream != null) {
            String queriesCorpus = IOUtil.toString(queriesInputStream);

            for (var rawQ : queriesCorpus.split("#\\r\\n")) {

                var endId = rawQ.indexOf("\r\n");
                var idQuery = rawQ.substring(0, endId).trim();
                var textQuery = rawQ.substring((endId + 2));

                queryHashMap.put(idQuery, new Query(textQuery, idQuery, new String[]{}));
            }
        }

        if (queriesDocInputStream != null) {
            String queriesAnswersCorpus = IOUtil.toString(queriesDocInputStream);

            for (String rawQAnswer : queriesAnswersCorpus.split(" -1\\r\\n\\r\\n")) {

                var queryAnswers = rawQAnswer.replace("Query ", "");

                var endAnswersId = queryAnswers.indexOf("\r\n", 0);
                var idAnswers = queryAnswers.substring(0, endAnswersId).trim();

                var startAnswers = queryAnswers.lastIndexOf("\r\n") + 2;
                var textAnswers = queryAnswers.substring(startAnswers).replace(" -1","");

                var query = queryHashMap.get(idAnswers);

                if (query != null) {
                    queries.add(new Query(query.text, query.exID, textAnswers.split(" ")));
                }
            }
        }
    }

    private ArrayList<Score> cosine(Query query) throws IOException {

        var scores = new ArrayList<Score>();

        int N = directoryReader.maxDoc();

        //for calc avgFieldLength
        for (int docId = 0; docId < directoryReader.maxDoc(); docId++) {

            double sumUp = 0;
            double sumDownQue = 0;
            double sumDownDoc = 0;

            var doc = directoryReader.document(docId);

            var externalDocId = doc.get("id");

            Terms vector = directoryReader.getTermVector(docId, "content");

            if (vector == null) continue;

            TermsEnum terms = vector.iterator();

            BytesRef bytesRef;

            while ((bytesRef = terms.next()) != null) {

                var doc_term = bytesRef.utf8ToString();

                //if (!query.terms.contains(doc_term)) continue;

                var doc_tf = terms.totalTermFreq();

                //if (doc_tf <= 0) continue;

                var que_tf = query.termFrequencies.get(doc_term);
                var doc_df = directoryReader.docFreq(new Term("content", doc_term));
                double doc_idf = Math.log10((float) N / doc_df);
                var doc_weight = Math.log10(doc_tf + 1) * doc_idf;
                var q_weight = Math.log10((que_tf != null ? que_tf : 0) + 1) * doc_idf;

                sumUp += q_weight * doc_weight;
                sumDownQue += q_weight * q_weight;
                sumDownDoc += doc_weight * doc_weight;

            }

            double score = sumUp / (Math.sqrt(sumDownDoc) * Math.sqrt(sumDownQue));

            if(score > 0) scores.add(new Score(score, externalDocId));
        }

        scores.sort((o1, o2) -> Double.compare(o1.score, o2.score) *-1);

        return scores;
    }


    int hit = 20;

    private void printMeasurementOfScores(Query query, ArrayList<Score> scores) {
        StringBuilder t = new StringBuilder();

        t.append('\n');
        t.append(String.format("Query: %s", query.text.replace("\r\n"," ")));

        t.append('\n');
        t.append("relevant: ");
        for (var i = 0; i < hit && i < query.relevantDocExIDs.size() ; i++) {

            String exId = query.relevantDocExIDs.get(i);

            t.append(String.format("%4d, ", Integer.valueOf(exId)));
        }

        t.append('\n');
        t.append(String.format("TOP@%d: ", hit));
        for (var i = 0; i < hit && i < scores.size() ; i++) {

            String exId = scores.get(i).exID;
            double score = scores.get(i).score;

            t.append(String.format("%4d(%1.3f), ", Integer.valueOf(exId), score));
        }

        double recall = getRecall(query, scores, hit);
        double precision = getPrecision(query, scores, hit);
        double fScore = getFScore(query, scores, hit);

        t.append('\n');
        t.append(String.format("recall=%1.3f    precision=%1.3f    f-score=%1.3f", recall, precision, fScore));

        t.append('\n');
        t.append("---------------------------------------------------------------------------------");
        System.out.println(t);
    }

    private double getFScore(Query query, ArrayList<Score> scores, int k) {
        return (double) 1 / ((float) 0.5 * (1 / getRecall(query, scores, k))) + ((1 - (float) 0.5) * (1 / getPrecision(query, scores, k)));
    }

    private double getRecall(Query query, ArrayList<Score> scores, int k) {
//        return (double) getTP(query,scores,k) / (getTP(query,scores,k) + getFN(query,scores,k));
        return (double) getTP(query, scores, k) / query.relevantDocExIDs.size();
    }

    private double getPrecision(Query query, ArrayList<Score> scores, int k) {
//        return (double) getTP(query,scores,k) / (getTP(query,scores,k) + getFP(query,scores,k));
        return (double) getTP(query, scores, k) / k;
    }

    private int getTN(Query query, ArrayList<Score> scores, int k) {
        return scores.size() - getFP(query, scores, k);
    }

    private int getFN(Query query, ArrayList<Score> scores, int k) {
        return query.relevantDocExIDs.size() - getTP(query, scores, k);
    }

    private int getFP(Query query, ArrayList<Score> scores, int k) {
        return k - getTP(query, scores, k);
    }

    private int getTP(Query query, ArrayList<Score> scores, int k) {
        int tp = 0;

        for (var i = 0; i < k; i++) {
            if (query.relevantDocExIDs.contains(scores.get(i).exID)) {
                tp++;
            }
        }

        return tp;
    }


    public static void main(String[] args) throws IOException, ParseException {

        System.out.println("IR PROJECT: LISA Corpus");

        var lisa = new LISA();

        lisa.buildIndex();
        lisa.loadQueries();


        for (var query : lisa.queries) {
            var cosine_scores = lisa.cosine(query);

            lisa.printMeasurementOfScores(query, cosine_scores);
        }
    }
}