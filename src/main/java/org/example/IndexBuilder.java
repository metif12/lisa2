package org.example;

import org.apache.commons.io.IOUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
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

public class IndexBuilder {

    private FieldType contentFieldType;
    private FieldType idFieldType;

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

    private IndexWriter indexWriter;

    public IndexBuilder(IndexWriter indexWriter) {

        this.indexWriter = indexWriter;

        contentFieldType = new FieldType();
        contentFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        contentFieldType.setTokenized(true);
        contentFieldType.setStored(true);
        contentFieldType.setStoreTermVectors(true);  //Store Term Vectors
        contentFieldType.freeze();
    }

    public void build() throws IOException {

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
                    String id = d.substring(0, endID).replace("Document", "");
                    String content = d.substring(endID+2);

                    Document doc = new Document();
                    doc.add(new Field("id", id, StringField.TYPE_STORED));
                    doc.add(new Field("content", content, contentFieldType));
                    indexWriter.addDocument(doc);
                }
            }
        }
    }
}
