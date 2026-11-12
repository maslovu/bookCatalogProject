package com.maslov.booksmaslov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAiService {

    private final ChatClient chatClient;

    public String generateBookDescription(String title, String authors, String genre) {
        log.info("Generating AI description for book: {}", title);
        String prompt = "Сгенерируй краткое описание книги на русском языке: '" + title + "', авторы: " + authors + ", жанр: " + genre;
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
