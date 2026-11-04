package com.example.claudedemo.rag;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rag")
public class RagController {

    public record ScoredDocument(String text, double score) {
        public int scorePercent() {
            return (int) Math.round(Math.max(score, 0) * 100);
        }
    }

    private static final int TOP_K = 3;

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore store;
    private final ChatClient chatClient;

    public RagController(EmbeddingModel embeddingModel, EmbeddingStore store,
                         ChatClient.Builder builder) {
        this.embeddingModel = embeddingModel;
        this.store = store;
        this.chatClient = builder.build();
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("documents", store.all());
        return "rag";
    }

    @PostMapping("/documents")
    public String addDocument(@RequestParam String text, RedirectAttributes redirectAttributes) {
        float[] embedding = embeddingModel.embed(text);
        store.add(text, embedding);
        redirectAttributes.addFlashAttribute("notice",
                "Document embedded (" + embedding.length + " dims) and stored.");
        return "redirect:/rag";
    }

    @PostMapping("/query")
    public String query(@RequestParam String query,
                        @RequestParam(defaultValue = "false") boolean answer,
                        Model model) {
        float[] queryEmbedding = embeddingModel.embed(query);

        List<ScoredDocument> results = store.all().stream()
                .map(doc -> new ScoredDocument(doc.text(),
                        CosineSimilarity.of(queryEmbedding, doc.embedding())))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .limit(TOP_K)
                .toList();

        model.addAttribute("documents", store.all());
        model.addAttribute("query", query);
        model.addAttribute("results", results);

        if (answer && !results.isEmpty()) {
            String context = results.stream()
                    .map(ScoredDocument::text)
                    .collect(Collectors.joining("\n- ", "- ", ""));
            String prompt = """
                    Answer the question using only the context below.
                    If the context doesn't contain the answer, say so.

                    Context:
                    %s

                    Question: %s""".formatted(context, query);
            model.addAttribute("answer",
                    chatClient.prompt().user(prompt).call().content());
        }
        return "rag";
    }

    @PostMapping("/clear")
    public String clear() {
        store.clear();
        return "redirect:/rag";
    }
}
