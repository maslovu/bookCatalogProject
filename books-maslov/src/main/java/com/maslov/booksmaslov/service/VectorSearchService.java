package com.maslov.booksmaslov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final VectorStore vectorStore;

    public void storeComment(String commentText, long bookId) {
        log.info("Storing embedding for comment: {}", commentText);

        vectorStore.add(List.of(
                new Document(
                        commentText,
                        Map.of("bookId", bookId, "text", commentText)
                )
        ));
    }

    public List<Map<String, Object>> searchSimilarComments(String query, int topK) {
        log.info("Searching similar comments for query: {}", query);

        var results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );

        return results.stream()
                .map(r -> {
                    var metadata = r.getMetadata();
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("text", r.getText());
                    result.put("bookId", metadata != null ? metadata.get("bookId") : null);
                    result.put("score", r.getScore());
                    return result;
                })
                .toList();
    }
}
