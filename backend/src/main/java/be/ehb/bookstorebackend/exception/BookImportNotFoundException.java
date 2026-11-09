package be.ehb.bookstorebackend.exception;

public class BookImportNotFoundException extends RuntimeException {

    public BookImportNotFoundException(String isbn) {
        super("Book not found in Open Library: " + isbn);
    }
}
