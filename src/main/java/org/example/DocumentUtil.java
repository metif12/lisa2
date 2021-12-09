package org.example;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.io.IOException;

public class DocumentUtil {

    private final IndexReader indexReader;
    private final MyTermVec docVec;
    private final Document doc;

    public DocumentUtil(IndexReader indexReader, int docId) throws IOException {

        this.indexReader = indexReader;

        doc = indexReader.document(docId);

        var externalDocId = doc.get("id");

        Terms vector = indexReader.getTermVector(docId, "content");

        this.docVec = new MyTermVec(indexReader, externalDocId, vector, "content");
    }

    public Document getDoc() {
        return doc;
    }

    public MyTermVec getDocVec() {
        return docVec;
    }

}
