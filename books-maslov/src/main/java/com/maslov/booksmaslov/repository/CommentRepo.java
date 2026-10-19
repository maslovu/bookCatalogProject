package com.maslov.booksmaslov.repository;

import com.maslov.booksmaslov.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {

    List<Comment> findByBookId(long bookId);
}
