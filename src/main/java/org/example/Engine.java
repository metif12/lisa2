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

public class Engine {

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

    private Directory directory;
    private IndexWriterConfig config;
    private DirectoryReader directoryReader;

    final private Similarity similarity;
    final private Analyzer analyzer;

    public Engine(Analyzer analyzer , Similarity similarity) throws IOException {
        this.analyzer = analyzer;
        this.similarity = similarity;
    }

    public Engine(Analyzer analyzer) throws IOException {
        this(analyzer, null);
    }

    public void start() throws IOException {

        String similarityClassName = (similarity != null) ? similarity.getClass().getSimpleName() : "Default";

        Path indexPath = Path.of("index_" + similarityClassName);

        if(!Files.exists(indexPath)) Files.createDirectory(indexPath);

        directory = FSDirectory.open(indexPath);
        config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        //set similarity
//        if (similarity!=null) config.setSimilarity(similarity);

        buildIndex();

        directoryReader = DirectoryReader.open(directory);
    }

    private void buildIndex() throws IOException {

        var indexWriter = new IndexWriter(directory, config);
        var indexBuilder = new IndexBuilder(indexWriter);

        indexBuilder.build();
    }

    public IndexSearcher getIndexSearcher() throws IOException {

        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        //set similarity
//        if (similarity!=null) indexSearcher.setSimilarity(similarity);

        return indexSearcher;
    }

    public DirectoryReader getDirectoryReader(){
        return directoryReader;
    }

    public Analyzer getAnalyzer(){
        return analyzer;
    }

    public void closeDirectory() throws IOException {
        directoryReader.close();
        directory.close();
    }
}
