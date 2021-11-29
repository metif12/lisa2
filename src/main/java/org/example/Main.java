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
import org.apache.lucene.search.similarities.Similarity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static org.example.Engine.FIELD_CONTENT;
import static org.example.Engine.FIELD_ID;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("IR PROJECT: LISA Corpus");

        MyAnalyzer analyzer = new MyAnalyzer();
        Engine engineBM25 = new Engine(new MyBM25(), analyzer);
        Engine engineMyQueryLikelihood = new Engine(new MyQueryLikelihood(), analyzer);

        IndexSearcher indexSearcherBM25 = engineBM25.getIndexSearcher();
        IndexSearcher indexSearcherQueryLikelihood = engineMyQueryLikelihood.getIndexSearcher();



        // Parse a simple query that searches for "text":
        SimpleQueryParser parser = new SimpleQueryParser(analyzer, FIELD_CONTENT);
        Query query = parser.parse("I AM INTERESTED IN THE IDENTIFICATION AND EVALUATION OF NOVEL\n" +
                "COMPUTER ARCHITECTURES, FOR INSTANCE, INCREASED PARALLELISM, BOTH\n" +
                "IN SIMD AND MIMD MACHINES.  I AM ALSO INTERESTED IN INFORMATION ABOUT\n" +
                "ASSOCIATIVE STORES OR MEMORIES AND ASSOCIATIVE PROCESSORS.\n" +
                "COMPUTER ARCHITECTURES, ASSOCIATIVE PROCESSORS, ASSOCIATIVE STORES\n" +
                "ASSOCIATIVE MEMORY.");
        ScoreDoc[] hits = indexSearcherBM25.search(query, 10).scoreDocs;

        // Iterate through the results:
        for (ScoreDoc hit : hits) {
            Document hitDoc = indexSearcherBM25.doc(hit.doc);
            System.out.println("#" + hitDoc.get(FIELD_ID) + " - " + hitDoc.get(FIELD_CONTENT));
            System.out.println("*************************************************");
        }

        engineBM25.closeDirectory();
        engineMyQueryLikelihood.closeDirectory();
    }
}
