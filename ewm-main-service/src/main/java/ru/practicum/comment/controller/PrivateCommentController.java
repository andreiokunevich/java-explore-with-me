package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.interfaces.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        return commentService.addComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long commentId) {
        return commentService.updateComment(newCommentDto, userId, commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllCommentsOfUserByEvent(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @RequestParam(required = false, defaultValue = "0") int from,
                                                        @RequestParam(required = false, defaultValue = "10") int size) {
        return commentService.getAllCommentsOfUserByEvent(userId, eventId, from, size);
    }
}