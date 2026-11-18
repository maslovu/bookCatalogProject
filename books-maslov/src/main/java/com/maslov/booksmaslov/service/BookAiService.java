package com.maslov.booksmaslov.service;

import com.maslov.booksmaslov.dto.BookDto;
import com.maslov.booksmaslov.mapper.BookMapper;
import com.maslov.booksmaslov.repository.BookRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAiService {

    private final ChatClient chatClient;
    private final BookRepo bookRepo;
    private final VectorSearchService vectorSearchService;
    private final BookMapper bookMapper;

    public String generateBookDescription(String title, String authors, String genre) {
        log.info("Generating AI description for book: {}", title);
        String prompt = "Сгенерируй краткое описание книги на русском языке: '" + title + "', авторы: " + authors + ", жанр: " + genre;
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Cacheable(value = "ai-recommendations", key = "#userQuery")
    public String recommendBookByComment(String userQuery) {
        log.info("RAG: searching for book based on user query: {}", userQuery);

        // 1. RETRIEVAL: ищем релевантные комментарии через vector search
        List<Map<String, Object>> relevantComments = vectorSearchService.searchSimilarComments(userQuery, 5);
        log.info("RAG: found {} similar comments", relevantComments.size());

        if (relevantComments.isEmpty()) {
            return "Не удалось найти комментарии, релевантные вашему запросу. Попробуйте другой запрос.";
        }

        // 2. AUGMENTATION: формируем контекст из комментариев
        String commentsContext = relevantComments.stream()
                .map(c -> "Комментарий: " + c.get("text") + " (книга ID: " + c.get("bookId") + ")")
                .collect(Collectors.joining("\n"));

        // Получаем список ID книг из комментариев
        List<Long> bookIds = relevantComments.stream()
                .map(c -> Long.valueOf(c.get("bookId").toString()))
                .toList();

        // Получаем названия книг из БД
        List<BookDto> books = bookRepo.findByIdIn(bookIds).stream()
                .map(bookMapper::toDto)
                .toList();

        String booksContext = books.stream()
                .map(b -> "ID: " + b.id() + ", Название: " + b.title() + ", Авторы: " + b.authors())
                .collect(Collectors.joining("\n"));

        // 3. GENERATION: отправляем в модель с контекстом
        String prompt = "У тебя есть только следующие комментарии и книги. На основе них порекомендуй книгу.\n\n" +
                "Запрос пользователя: " + userQuery + "\n\n" +
                "Комментарии:\n" + commentsContext + "\n\n" +
                "Доступные книги:\n" + booksContext + "\n\n" +
                "Правила:\n" +
                "1. Рекомендуй ТОЛЬКО книгу из списка 'Доступные книги' выше\n" +
                "2. Если в комментариях нет информации о подходящей книге — напиши 'Не могу порекомендовать на основе доступных данных'\n" +
                "3. Не придумывай книги, которых нет в списке\n" +
                "4. Ответь в формате: 'Название книги — Автор'\n\n";

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @CacheEvict(value = "ai-recommendations", allEntries = true)
    public void clearRecommendationCache() {
        log.info("Cleared AI recommendation cache");
    }

    @CacheEvict(value = "ai-recommendations", allEntries = true)
    public void storeCommentEmbedding(String commentText, long bookId) {
        vectorSearchService.storeComment(commentText, bookId);
    }
}
