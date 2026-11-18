package com.maslov.booksmaslov.service;

import com.maslov.booksmaslov.dto.CommentDto;
import com.maslov.booksmaslov.dto.CommentEvent;
import com.maslov.booksmaslov.dto.CommentRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    Page<CommentDto> getAllCommentForBook(long bookId, int page, int size);

    CommentDto createComment(CommentRequest comment, long bookId);

    void createCommentFromBatch(List<CommentEvent> comments);

    CommentDto updateComment(CommentRequest newComment, long commentId, long bookId);

    void deleteComment(long commentId, long bookId);
}
