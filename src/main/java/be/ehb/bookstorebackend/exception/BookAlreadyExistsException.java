package be.ehb.bookstorebackend.exception;

public class BookAlreadyExistsException extends RuntimeException {

    public BookAlreadyExistsException(String isbn) {
        super("Book already exists with ISBN: " + isbn);
    }
}
