package com.maslov.booksmaslov.service;

import com.maslov.booksmaslov.dto.CommentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentProcessingService {

    private final CommentService commentService;
    private final BookAiService bookAiService;

    /**
     * Этот метод выступает "оберткой". Он ловит конфликты и перезапускает процесс.
     */
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.backoff-delay:1000}"))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processWithRetry(List<CommentEvent> events) {
        commentService.createCommentFromBatch(events);
        
        // Сохраняем эмбеддинги для RAG
        for (CommentEvent event : events) {
            try {
                bookAiService.storeCommentEmbedding(event.comment(), event.bookId());
                log.info("Stored embedding for comment on book: {}", event.bookId());
            } catch (Exception e) {
                log.error("Failed to store embedding for book: {}", event.bookId(), e);
            }
        }
    }
}
