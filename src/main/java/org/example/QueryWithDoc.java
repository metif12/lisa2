package org.example;

import java.util.Arrays;
import java.util.Objects;

public class QueryWithDoc {
    private String id;
    private String query;
    private String[] docs;

    public QueryWithDoc(String id, String query, String[] docs) {
        this.id = id;
        this.query = query;
        this.docs = docs;
    }

    public String getQuery() {
        return query;
    }

    public String getId() { return id; }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getDocs() {
        return docs;
    }

    public void setDocs(String[] docs) {
        this.docs = docs;
    }

    @Override
    public String toString() {
        return "QueryWithDoc{" +
                "id='" + id + '\'' +
                ", query=" + query + '\'' +
                ", docs=" + Arrays.toString(docs) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryWithDoc that = (QueryWithDoc) o;
        return Objects.equals(id, that.id) && Objects.equals(query, that.query) && Arrays.equals(docs, that.docs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, query, Arrays.hashCode(docs));
    }
}
