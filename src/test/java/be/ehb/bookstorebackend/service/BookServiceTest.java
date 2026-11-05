package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.ehb.bookstorebackend.exception.BookAlreadyExistsException;
import be.ehb.bookstorebackend.exception.BookNotFoundException;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private BookRepository bookRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        bookService = new BookService(bookRepository);
    }

    @Test
    void shouldReturnAllBooks() {

        Book book = new Book(
                "9780134685991",
                "Effective Java",
                "Joshua Bloch",
                "Java programming guide",
                new BigDecimal("45.99"),
                10
        );

        when(bookRepository.findAll())
                .thenReturn(List.of(book));

        List<Book> result = bookService.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("Effective Java", result.get(0).getTitle());

        verify(bookRepository).findAll();
    }


    @Test
    void shouldAddNewBook() {
        Book book = new Book(
                "1234567890", "Java Basics", "Test Author",
                "Learning Java", new BigDecimal("25.00"), 5
        );

        when(bookRepository.existsById(book.getIsbn()))
                .thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.addBook(book);

        assertEquals("Java Basics", result.getTitle());
        assertEquals(5, result.getStock());
        verify(bookRepository).save(book);
    }

    @Test
    void shouldRejectDuplicateIsbn() {
        Book book = new Book(
                "1234567890", "Java Basics", "Test Author",
                "Learning Java", new BigDecimal("25.00"), 5
        );

        when(bookRepository.existsById(book.getIsbn()))
                .thenReturn(true);

        assertThrows(BookAlreadyExistsException.class,
                () -> bookService.addBook(book));

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldRejectNegativePrice() {
        Book book = new Book(
                "1234567891", "Invalid Book", "Test Author",
                "Negative price", new BigDecimal("-10.00"), 5
        );

        assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook(book));

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldRejectNegativeStock() {
        Book book = new Book(
                "1234567892", "Invalid Stock", "Test Author",
                "Negative stock", new BigDecimal("20.00"), -2
        );

        assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook(book));

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldRejectUpdatingNonExistingBook() {
        String isbn = "9999999999";

        Book book = new Book(
                isbn, "Unknown Book", "Test Author",
                "Not in database", new BigDecimal("20.00"), 5
        );

        when(bookRepository.findById(isbn))
                .thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.updateBook(isbn, book));

        verify(bookRepository, never()).save(any(Book.class));
    }

}
