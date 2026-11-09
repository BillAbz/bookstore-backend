package be.ehb.bookstorebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotFound(
            BookNotFoundException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ProblemDetail handleBookAlreadyExists(
            BookAlreadyExistsException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(
            IllegalArgumentException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(BookImportNotFoundException.class)
    public ProblemDetail handleBookImportNotFound(
            BookImportNotFoundException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(OpenLibraryUnavailableException.class)
    public ProblemDetail handleOpenLibraryUnavailable(
            OpenLibraryUnavailableException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
    }
}
