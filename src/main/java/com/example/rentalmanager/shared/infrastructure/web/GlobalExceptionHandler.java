package com.example.rentalmanager.shared.infrastructure.web;

import com.example.rentalmanager.shared.domain.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;

/**
 * Translates domain and infrastructure exceptions into RFC 9457 Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Mono<ProblemDetail> handleDomainException(DomainException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Domain Rule Violation");
        problem.setType(URI.create("https://api.rentalmanager.example.com/errors/domain-rule-violation"));
        problem.setProperty("timestamp", Instant.now());
        return Mono.just(problem);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidationException(WebExchangeBindException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://api.rentalmanager.example.com/errors/validation-failed"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList());
        return Mono.just(problem);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGenericException(Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.rentalmanager.example.com/errors/internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return Mono.just(problem);
    }
}
