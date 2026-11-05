package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
