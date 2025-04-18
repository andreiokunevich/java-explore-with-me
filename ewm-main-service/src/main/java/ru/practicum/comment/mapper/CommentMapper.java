package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.enums.CommentState;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.UserCommentDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static Comment toComment(NewCommentDto newCommentDto, User author, Event event) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setState(CommentState.PENDING);
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        if (comment.getAuthor() != null) {
            commentDto.setAuthor(UserMapper.toUserCommentDto(comment.getAuthor()));
        } else {
            commentDto.setAuthor(new UserCommentDto("DELETED"));
        }
        commentDto.setEvent(EventMapper.toEventShortDto(comment.getEvent()));
        commentDto.setCommentState(comment.getState());
        return commentDto;
    }
}