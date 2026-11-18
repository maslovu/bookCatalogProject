package com.maslov.booksmaslov.repository;

import com.maslov.booksmaslov.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepo extends JpaRepository<Comment, Long> {

    Page<Comment> findByBookId(long bookId, Pageable pageable);
}
