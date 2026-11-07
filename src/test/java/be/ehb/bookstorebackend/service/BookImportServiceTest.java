package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.adapter.OpenLibraryBookAdapter;
import be.ehb.bookstorebackend.client.OpenLibraryClient;
import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.exception.BookAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookImportServiceTest {

    private OpenLibraryClient openLibraryClient;
    private OpenLibraryBookAdapter bookAdapter;
    private BookService bookService;
    private BookImportService bookImportService;

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        openLibraryClient = mock(OpenLibraryClient.class);
        bookAdapter = mock(OpenLibraryBookAdapter.class);
        bookService = mock(BookService.class);

        bookImportService = new BookImportService(
                openLibraryClient,
                bookAdapter,
                bookService
        );

        jsonMapper = new JsonMapper();
    }

    @Test
    void shouldImportBookSuccessfully() throws Exception {
        String isbn = "9780743273565";

        JsonNode json = jsonMapper.readTree("""
                {
                  "title": "The Great Gatsby",
                  "number_of_pages": 180
                }
                """);

        Book book = new Book(
                isbn,
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "Classic novel",
                new BigDecimal("19.99"),
                5
        );

        when(openLibraryClient.getBookByIsbn(isbn))
                .thenReturn(json);

        when(bookAdapter.toBook(isbn, json))
                .thenReturn(book);

        when(bookService.addBook(book))
                .thenReturn(book);

        Book result = bookImportService.importBookByIsbn(isbn);

        assertEquals(isbn, result.getIsbn());
        assertEquals("The Great Gatsby", result.getTitle());
        assertEquals(new BigDecimal("19.99"), result.getPrice());

        verify(openLibraryClient).getBookByIsbn(isbn);
        verify(bookAdapter).toBook(isbn, json);
        verify(bookService).addBook(book);
    }

    @Test
    void shouldRejectEmptyIsbn() {
        assertThrows(IllegalArgumentException.class,
                () -> bookImportService.importBookByIsbn(""));

        verifyNoInteractions(
                openLibraryClient,
                bookAdapter,
                bookService
        );
    }

    @Test
    void shouldRejectNullIsbn() {
        assertThrows(IllegalArgumentException.class,
                () -> bookImportService.importBookByIsbn(null));

        verifyNoInteractions(
                openLibraryClient,
                bookAdapter,
                bookService
        );
    }

    @Test
    void shouldPropagateDuplicateIsbnError() throws Exception {
        String isbn = "9780743273565";

        JsonNode json = jsonMapper.readTree("""
                {
                  "title": "The Great Gatsby"
                }
                """);

        Book book = new Book(
                isbn,
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "Classic novel",
                new BigDecimal("19.99"),
                5
        );

        when(openLibraryClient.getBookByIsbn(isbn))
                .thenReturn(json);

        when(bookAdapter.toBook(isbn, json))
                .thenReturn(book);

        when(bookService.addBook(book))
                .thenThrow(new BookAlreadyExistsException(isbn));

        assertThrows(BookAlreadyExistsException.class,
                () -> bookImportService.importBookByIsbn(isbn));

        verify(bookService).addBook(book);
    }
}
