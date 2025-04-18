package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.enums.CommentState;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.interfaces.CommentService;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private static final Sort SORT = Sort.by("created").descending();

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsOfEvent(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, SORT);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено.");
        }
        List<Comment> comments = commentRepository.findAllByEventIdAndState(eventId, CommentState.APPROVED, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден."));

        if (comment.getState() != CommentState.APPROVED) {
            throw new ConflictException("Комментарий существует, но еще не опубликован (ожидает модерации или отклонен)");
        }

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));

        Comment comment = CommentMapper.toComment(newCommentDto, user, event);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        Comment comment = commentRepository.findByIdAndStateNot(commentId, CommentState.REJECTED)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден либо отклонен администратором."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Нельзя редактировать чужие комментарии.");
        }

        comment.setText(newCommentDto.getText());

        if (comment.getState() == CommentState.APPROVED) {
            comment.setState(CommentState.PENDING);
        }

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден."));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Комментарий написан не Вами. Удаление невозможно.");
        }

        if (comment.getState() == CommentState.REJECTED) {
            throw new ConflictException("Нельзя удалить отклонённый комментарий.");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsOfUserByEvent(Long userId, Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, SORT);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено.");
        }

        List<Comment> comments = commentRepository.findAllByAuthorIdAndEventIdAndState(userId, eventId, CommentState.APPROVED, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public void deleteAllCommentByAdmin(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено.");
        }

        commentRepository.deleteAllByEventId(eventId);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден."));

        commentRepository.delete(comment);
    }

    @Override
    public CommentDto approveOrRejectCommentByAdmin(Long commentId, CommentState commentState) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не найден."));

        if (comment.getState() != CommentState.PENDING) {
            throw new ConflictException("Комментарий уже либо одобрен, либо отклонен.");
        }

        if (commentState != CommentState.APPROVED && commentState != CommentState.REJECTED) {
            throw new ValidationException("Недопустимый статус комментария.");
        }

        comment.setState(commentState);

        return CommentMapper.toCommentDto((commentRepository.save(comment)));
    }
}