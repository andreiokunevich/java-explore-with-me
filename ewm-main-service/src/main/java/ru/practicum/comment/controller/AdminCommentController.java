package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.enums.CommentState;
import ru.practicum.comment.service.interfaces.CommentService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllCommentsByAdmin(@PathVariable Long eventId) {
        commentService.deleteAllCommentByAdmin(eventId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/comments/{commentId}/approve")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto approveOrRejectCommentByAdmin(@PathVariable Long commentId,
                                                    @RequestParam CommentState commentState) {
        return commentService.approveOrRejectCommentByAdmin(commentId, commentState);
    }
}