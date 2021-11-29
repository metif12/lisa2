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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Engine {

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

    private final Directory directory;
    private final IndexWriterConfig config;
    private final Similarity similarity;
    private final DirectoryReader directoryReader;


    public Engine(Similarity similarity, Analyzer analyzer) throws IOException {

        this.similarity = similarity;

        String similarityClassName = similarity.getClass().getSimpleName();
        Path indexPath = Files.createDirectory(Path.of("index_" + similarityClassName));

        directory = FSDirectory.open(indexPath);
        config = new IndexWriterConfig(analyzer);
        directoryReader = DirectoryReader.open(directory);

        //set similarity
        config.setSimilarity(similarity);

        buildIndex();
    }

    private void buildIndex() throws IOException {
        final IndexWriter indexWriter;
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();

        parseCorpus(indexWriter);

        indexWriter.commit();
        indexWriter.close();
    }

    private void parseCorpus(IndexWriter indexWriter) throws IOException {
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
    }

    public IndexSearcher getIndexSearcher() throws IOException {

        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        //set similarity
        indexSearcher.setSimilarity(similarity);

        return indexSearcher;
    }

    public void closeDirectory() throws IOException {
        directoryReader.close();
        directory.close();
    }
}
