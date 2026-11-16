package com.maslov.booksmaslov.service;

import com.maslov.booksmaslov.mapper.BookMapper;
import com.maslov.booksmaslov.repository.BookRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookAiServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private BookRepo bookRepo;

    @Mock
    private VectorSearchService vectorSearchService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookAiService bookAiService;

    @Test
    void generateBookDescription_WhenCalled_ReturnsGeneratedDescription() {
        // Given
        String title = "Spring Boot в примерах";
        String authors = "Автор А";
        String genre = "Программирование";
        String expectedContent = "Краткое описание книги Spring Boot в примерах";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedContent);

        // When
        String result = bookAiService.generateBookDescription(title, authors, genre);

        // Then
        assertNotNull(result);
        assertEquals(expectedContent, result);
    }

    @Test
    void recommendBookByComment_WhenNoCommentsFound_ReturnsNotFoundMessage() {
        // Given
        String userQuery = "посоветуй книгу по космосу";
        when(vectorSearchService.searchSimilarComments(userQuery, 5)).thenReturn(List.of());

        // When
        String result = bookAiService.recommendBookByComment(userQuery);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Не удалось найти комментарии"));
        verify(chatClient, never()).prompt();
    }

    @Test
    void recommendBookByComment_WhenCommentsFound_ReturnsRecommendation() {
        // Given
        String userQuery = "посоветуй книгу по Python";
        List<Map<String, Object>> mockComments = List.of(
                Map.of("text", "Отличная книга по Python", "bookId", "1")
        );
        when(vectorSearchService.searchSimilarComments(userQuery, 5)).thenReturn(mockComments);

        when(bookRepo.findByIdIn(anyList())).thenReturn(List.of());

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Python编程 — Автор Б");

        // When
        String result = bookAiService.recommendBookByComment(userQuery);

        // Then
        assertNotNull(result);
        assertEquals("Python编程 — Автор Б", result);
        verify(chatClient).prompt();
    }

    @Test
    void clearRecommendationCache_WhenCalled_ClearsCache() {
        // When
        bookAiService.clearRecommendationCache();

        // Then
        verifyNoInteractions(vectorSearchService, bookRepo, chatClient);
    }

    @Test
    void storeCommentEmbedding_WhenCalled_DelegatesToVectorSearch() {
        // Given
        String commentText = "Отличная книга";
        long bookId = 1L;

        // When
        bookAiService.storeCommentEmbedding(commentText, bookId);

        // Then
        verify(vectorSearchService).storeComment(commentText, bookId);
    }
}
