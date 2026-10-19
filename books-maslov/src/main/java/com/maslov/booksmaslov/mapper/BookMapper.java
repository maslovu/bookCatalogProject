package com.maslov.booksmaslov.mapper;

import com.maslov.booksmaslov.domain.Author;
import com.maslov.booksmaslov.domain.Book;
import com.maslov.booksmaslov.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = EntityLoaderContext.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {

    @Mapping(source = "genre.id", target = "genreId")
    @Mapping(source = "year.id", target = "yearId")
    BookDto toDto(Book entity);

    default String mapAuthors(Set<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            return "";
        }
        return authors.stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));
    }

    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "genreId", target = "genre")
    @Mapping(source = "yearId", target = "year")
    Book toEntity(BookDto bookDto);

    default Set<Author> stringToAuthors(String source) {
        if (source == null || source.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(source.split(","))
                .map(String::trim)
                .map(Author::new)
                .collect(Collectors.toSet());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "genre", source = "genreId")
    @Mapping(target = "year", source = "yearId")
    @Mapping(target = "comments", ignore = true)
    void updateEntityFromDto(BookDto source, @MappingTarget Book target);
}
