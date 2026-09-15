package com.erzhena.spaceexplorer_api.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // бин, чтобы Spring нашел его на старте + бин обслуживающий контроллеры
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SnapiUnavailableException.class)
    public ProblemDetail handleSnapiUnavailable(SnapiUnavailableException e) {
        log.warn("SNAPI request failed", e);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("News source unavailable");
        problem.setDetail("Could not reach the Spaceflight News API. Please try again later.");
        return problem;
    }

    @ExceptionHandler(InvalidCursorException.class)
    public ProblemDetail handleInvalidCursor(InvalidCursorException e) {
        log.warn("Invalid cursor received", e);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid cursor");
        problem.setDetail("The provided cursor is malformed.");
        return problem;
    }
}