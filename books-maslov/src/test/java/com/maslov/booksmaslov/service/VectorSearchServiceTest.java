package com.maslov.booksmaslov.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorSearchServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private VectorSearchService vectorSearchService;

    @SuppressWarnings("unchecked")
    @Test
    void storeComment_WhenCalled_StoresDocumentInVectorStore() {
        // Given
        String commentText = "Отличная книга по Java";
        long bookId = 42L;

        // When
        vectorSearchService.storeComment(commentText, bookId);

        // Then
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertEquals(1, documents.size());

        Document document = documents.get(0);
        assertEquals(commentText, document.getText());
        assertEquals(bookId, document.getMetadata().get("bookId"));
        assertEquals(commentText, document.getMetadata().get("text"));
    }

    @Test
    void searchSimilarComments_WhenResultsFound_ReturnsFormattedResults() {
        // Given
        String query = "посоветуй книгу по Python";
        int topK = 5;

        Document mockDoc1 = new Document("Отличная книга по Python", Map.of("bookId", "1", "text", "Отличная книга по Python"));
        mockDoc1 = Document.builder().text(mockDoc1.getText()).metadata(mockDoc1.getMetadata()).score(0.95).build();

        Document mockDoc2 = new Document("Хорошая книга по Python", Map.of("bookId", "2", "text", "Хорошая книга по Python"));
        mockDoc2 = Document.builder().text(mockDoc2.getText()).metadata(mockDoc2.getMetadata()).score(0.85).build();

        when(vectorStore.similaritySearch((SearchRequest) any()))
                .thenReturn(List.of(mockDoc1, mockDoc2));

        // When
        List<Map<String, Object>> results = vectorSearchService.searchSimilarComments(query, topK);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());

        Map<String, Object> firstResult = results.get(0);
        assertEquals("Отличная книга по Python", firstResult.get("text"));
        assertEquals("1", firstResult.get("bookId"));
        assertEquals(0.95, firstResult.get("score"));

        verify(vectorStore).similaritySearch(argThat((SearchRequest request) ->
                request.getQuery().equals(query) && request.getTopK() == topK
        ));
    }

    @Test
    void searchSimilarComments_WhenNoResultsFound_ReturnsEmptyList() {
        // Given
        String query = "несуществующий запрос";
        int topK = 5;

        when(vectorStore.similaritySearch((SearchRequest) any())).thenReturn(List.of());

        // When
        List<Map<String, Object>> results = vectorSearchService.searchSimilarComments(query, topK);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchSimilarComments_WhenMetadataIsEmpty_ReturnsNullValues() {
        // Given
        String query = "тест";
        int topK = 1;

        Document mockDoc = new Document("Тестовый комментарий", Map.of());

        when(vectorStore.similaritySearch((SearchRequest) any())).thenReturn(List.of(mockDoc));

        // When
        List<Map<String, Object>> results = vectorSearchService.searchSimilarComments(query, topK);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());

        Map<String, Object> result = results.get(0);
        assertEquals("Тестовый комментарий", result.get("text"));
        assertNull(result.get("bookId"));
    }
}
