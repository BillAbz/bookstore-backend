package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.adapter.OpenLibraryBookAdapter;
import be.ehb.bookstorebackend.client.OpenLibraryClient;
import be.ehb.bookstorebackend.entity.Book;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class BookImportService {

    private final OpenLibraryClient openLibraryClient;
    private final OpenLibraryBookAdapter bookAdapter;
    private final BookService bookService;

    public BookImportService(
            OpenLibraryClient openLibraryClient,
            OpenLibraryBookAdapter bookAdapter,
            BookService bookService) {

        this.openLibraryClient = openLibraryClient;
        this.bookAdapter = bookAdapter;
        this.bookService = bookService;
    }

    public Book importBookByIsbn(String isbn) {

        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException(
                    "ISBN is required"
            );
        }

        // Fetch book information from Open Library
        JsonNode bookJson =
                openLibraryClient.getBookByIsbn(isbn);

        // Convert external data into our Book entity
        Book book = bookAdapter.toBook(isbn, bookJson);

        // Save the book using existing business rules
        return bookService.addBook(book);
    }
}
