package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .errors(ExceptionUtils.getRootCauseStackTraceList(e))
                .message(e.getMessage())
                .reason("Запрашиваемые данные не найдены.")
                .status(HttpStatus.NOT_FOUND.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(Exception e) {
        return ApiError.builder()
                .errors(ExceptionUtils.getRootCauseStackTraceList(e))
                .message(e.getMessage())
                .reason("Произошла внутренняя ошибка сервера.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ValidationException.class, NumberFormatException.class,
            MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(RuntimeException e) {
        return ApiError.builder()
                .errors(ExceptionUtils.getRootCauseStackTraceList(e))
                .message(e.getMessage())
                .reason("Запрос к серверу неверен.")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(Exception e) {
        return ApiError.builder()
                .errors(ExceptionUtils.getRootCauseStackTraceList(e))
                .message(e.getMessage())
                .reason("Конфликт при работе с данными.")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}