package com.maslov.booksmaslov.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.maslov.booksmaslov.domain.Genre;
import com.maslov.booksmaslov.domain.YearOfPublish;
import com.maslov.booksmaslov.validator.ExistingId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record BookDto(
        Long id,
        @NotBlank String title,
        @NotBlank String authors,
        @NotNull @ExistingId(entityClass = YearOfPublish.class) Long yearId,
        @NotNull @ExistingId(entityClass = Genre.class) Long genreId) implements Serializable {
}
