package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream().map(err -> String.format("Поле: %s. Ошибка: %s. Значение: %s", err.getField(), err.getDefaultMessage(), err.getRejectedValue())).collect(Collectors.toList());
        String message = errors.isEmpty() ? "Ошибка валидации запроса" : errors.get(0);
        log.warn("400 Bad Request (валидация): {}", message);
        return ApiError.builder().errors(errors).message(message).reason("Некорректно составленный запрос.").status(HttpStatus.BAD_REQUEST.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String message = String.format("Отсутствует обязательный параметр запроса: %s", e.getParameterName());
        log.warn("400 Bad Request: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Некорректно составленный запрос.").status(HttpStatus.BAD_REQUEST.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("Некорректное значение параметра '%s': '%s'", e.getName(), e.getValue());
        log.warn("400 Bad Request: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Некорректно составленный запрос.").status(HttpStatus.BAD_REQUEST.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler({BadRequestException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Некорректные параметры запроса";
        log.warn("400 Bad Request: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Некорректно составленный запрос.").status(HttpStatus.BAD_REQUEST.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        String message = e.getMessage() != null ? e.getMessage() : "Запрашиваемый объект не найден";
        log.warn("404 Not Found: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Требуемый объект не найден.").status(HttpStatus.NOT_FOUND.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        String message = e.getMessage() != null ? e.getMessage() : "Нарушены условия выполнения операции";
        log.warn("409 Conflict: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Для запрашиваемой операции не выполнены условия.").status(HttpStatus.CONFLICT.name()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        log.warn("409 DataIntegrityViolation: {}", message);
        return ApiError.builder().errors(Collections.emptyList()).message(message).reason("Нарушено ограничение целостности данных.").status(HttpStatus.CONFLICT.name()).timestamp(LocalDateTime.now()).build();
    }
}