
package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(
            @PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedBook);
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<Book> updateBook(
            @PathVariable String isbn,
            @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(isbn, book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return ResponseEntity.noContent().build();
    }
}
