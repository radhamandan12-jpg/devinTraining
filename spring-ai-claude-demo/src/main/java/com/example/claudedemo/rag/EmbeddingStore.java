package com.example.claudedemo.rag;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingStore {

    public record EmbeddedDocument(String text, float[] embedding) {
    }

    private final List<EmbeddedDocument> documents = new CopyOnWriteArrayList<>();

    public void add(String text, float[] embedding) {
        documents.add(new EmbeddedDocument(text, embedding));
    }

    public List<EmbeddedDocument> all() {
        return List.copyOf(documents);
    }

    public void clear() {
        documents.clear();
    }
}
