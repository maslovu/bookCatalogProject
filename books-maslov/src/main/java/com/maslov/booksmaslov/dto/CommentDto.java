package com.maslov.booksmaslov.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

public record CommentDto(Long id, String text, Long bookId) implements Serializable {

    @JsonCreator
    public CommentDto {
        // Можно добавить валидацию, если требуется
        // if (text == null || text.isBlank()) { throw new IllegalArgumentException("Text cannot be empty");}
    }
}
