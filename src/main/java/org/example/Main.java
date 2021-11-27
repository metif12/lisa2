package org.example;

import org.apache.commons.io.IOUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_ID = "id";
    public static final String[] CORPUS_NAMES = new String[]{
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

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("IR PROJECT: LISA Corpus");

        BM25SimilarityOriginal similarity = new BM25SimilarityOriginal();

        Analyzer analyzer = new MyAnalyzer();
        Path indexPath = Files.createTempDirectory("tempIndex");
        Directory directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //set similarity
        config.setSimilarity(similarity);

        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();

        for (String corpus_name : CORPUS_NAMES) {
            InputStream inputStream = Main.class.getResourceAsStream("\\..\\..\\" + corpus_name);
            if (inputStream != null) {
                String text = IOUtil.toString(inputStream);

                for (String rawDoc : text.split("\\r\\n\\*{44}\\r\\n")) {

                    String d = rawDoc.replaceAll("\\r\\n *?\\r\\n", "\r\n\r\n");
                    int endID = d.indexOf("\r");
                    String id = d.substring(0, endID).replace("Document", "").trim();
                    int endContent = d.length() - 1;
                    String content = d.substring(endID, endContent).trim();

                    //System.out.println(id);

                    Document doc = new Document();
                    doc.add(new Field("id", id, StringField.TYPE_STORED));
                    doc.add(new Field("content", content, TextField.TYPE_STORED));
                    indexWriter.addDocument(doc);
                }
            }
        }


        indexWriter.commit();
        indexWriter.close();

        // Now search the index:
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        //set similarity
        indexSearcher.setSimilarity(similarity);

        // Parse a simple query that searches for "text":
        SimpleQueryParser parser = new SimpleQueryParser(analyzer, FIELD_CONTENT);
        Query query = parser.parse("I AM INTERESTED IN THE IDENTIFICATION AND EVALUATION OF NOVEL\n" +
                "COMPUTER ARCHITECTURES, FOR INSTANCE, INCREASED PARALLELISM, BOTH\n" +
                "IN SIMD AND MIMD MACHINES.  I AM ALSO INTERESTED IN INFORMATION ABOUT\n" +
                "ASSOCIATIVE STORES OR MEMORIES AND ASSOCIATIVE PROCESSORS.\n" +
                "COMPUTER ARCHITECTURES, ASSOCIATIVE PROCESSORS, ASSOCIATIVE STORES\n" +
                "ASSOCIATIVE MEMORY.");
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;

        // Iterate through the results:
        for (ScoreDoc hit : hits) {
            Document hitDoc = indexSearcher.doc(hit.doc);
            System.out.println("found: #" + hitDoc.get(FIELD_ID) + " - " + hitDoc.get(FIELD_CONTENT));
        }

        directoryReader.close();
        directory.close();
    }
}
