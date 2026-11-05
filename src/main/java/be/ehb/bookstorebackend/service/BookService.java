
package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.repository.BookRepository;
import org.springframework.stereotype.Service;
import be.ehb.bookstorebackend.exception.BookNotFoundException;
import be.ehb.bookstorebackend.exception.BookAlreadyExistsException;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // List all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Find a book by ISBN
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn);
    }

    // Add a new book

    public Book addBook(Book book) {
        validateBook(book);

        if (bookRepository.existsById(book.getIsbn())) {
            throw new BookAlreadyExistsException(book.getIsbn());
        }

        return bookRepository.save(book);
    }


    // Update an existing book

    public Book updateBook(String isbn, Book updatedBook) {
        Book existingBook = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));

        validateBook(updatedBook);

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setDescription(updatedBook.getDescription());
        existingBook.setPrice(updatedBook.getPrice());
        existingBook.setStock(updatedBook.getStock());

        return bookRepository.save(existingBook);
    }


    // Delete a book

    public void deleteBook(String isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new BookNotFoundException(isbn);
        }

        bookRepository.deleteById(isbn);
    }


    // Validate book information
    private void validateBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (book.getPrice() == null ||
                book.getPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative"
            );
        }

        if (book.getStock() < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot be negative"
            );
        }
    }
}
