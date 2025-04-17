package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.enums.CommentState;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventIdAndState(Long eventId, CommentState state, Pageable pageable);

    Optional<Comment> findByIdAndStateNot(Long commentId, CommentState state);

    List<Comment> findAllByAuthorIdAndEventIdAndState(Long userId, Long eventId, CommentState state, Pageable pageable);

    void deleteAllByEventId(Long eventId);

    List<Comment> findAllByAuthorId(Long userId);
}