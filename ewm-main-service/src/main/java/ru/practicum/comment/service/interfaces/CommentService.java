package ru.practicum.comment.service.interfaces;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.enums.CommentState;

import java.util.List;

public interface CommentService {
    List<CommentDto> getAllCommentsOfEvent(Long eventId, int from, int size);

    CommentDto getCommentById(Long commentId);

    CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getAllCommentsOfUserByEvent(Long userId, Long eventId, int from, int size);

    void deleteAllCommentByAdmin(Long eventId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto approveOrRejectCommentByAdmin(Long commentId, CommentState commentState);
}
