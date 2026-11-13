package com.maslov.booksmaslov.repository;

import com.maslov.booksmaslov.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepo extends JpaRepository<Comment, Long> {

    Page<Comment> findByBookId(long bookId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Comment> findByTextContaining(@Param("keyword") String keyword, Pageable pageable);
}
