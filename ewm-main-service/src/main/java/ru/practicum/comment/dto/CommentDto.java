package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.comment.model.enums.CommentState;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserCommentDto;

import java.time.LocalDateTime;

import static ru.practicum.util.Constant.DATE_TIME_FORMAT;

@Data
public class CommentDto {
    private Long id;

    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime created;

    private UserCommentDto author;

    private EventShortDto event;

    private CommentState commentState;
}