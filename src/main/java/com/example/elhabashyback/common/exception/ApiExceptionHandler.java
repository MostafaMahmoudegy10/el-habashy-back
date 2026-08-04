package com.example.elhabashyback.common.exception;

import com.example.elhabashyback.media.exception.MediaUploadException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mail.MailException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MailException.class)
    ProblemDetail mailUnavailable(MailException exception) {
        ProblemDetail problem = problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Email service is temporarily unavailable. Please try again later.");
        problem.setProperty("errorCode", "EMAIL_SERVICE_UNAVAILABLE");
        return problem;
    }

    @ExceptionHandler(DisabledException.class)
    ProblemDetail disabled(DisabledException exception) {
        return problem(HttpStatus.FORBIDDEN, "Account is not activated. Check your email.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({AuthenticationException.class, UnauthorizedException.class})
    ProblemDetail unauthorized(Exception exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid authentication credentials");
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ProblemDetail conflict(Exception exception) {
        return problem(HttpStatus.CONFLICT, exception instanceof ConflictException
                ? exception.getMessage() : "The requested data already exists");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    ProblemDetail badRequest(BadRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MediaUploadException.class)
    ProblemDetail mediaUpload(MediaUploadException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_GATEWAY, exception.getMessage());
        problem.setProperty("errorCode", "MEDIA_UPLOAD_FAILED");
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("about:blank"));
        return problem;
    }
}
