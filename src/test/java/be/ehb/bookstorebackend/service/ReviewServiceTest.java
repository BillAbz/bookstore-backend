package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.entity.Review;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.BookRepository;
import be.ehb.bookstorebackend.repository.ReviewRepository;
import be.ehb.bookstorebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    private BookRepository bookRepository;
    private ReviewService reviewService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        userRepository = mock(UserRepository.class);
        bookRepository = mock(BookRepository.class);

        reviewService = new ReviewService(
                reviewRepository,
                userRepository,
                bookRepository
        );

        user = new User(
                "Bilal",
                "Test",
                "bilal.test@example.com",
                "hashedPassword"
        );
        user.setId(1L);

        book = new Book(
                "9780743273565",
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "A classic novel",
                new BigDecimal("19.99"),
                5
        );
    }

    @Test
    void shouldCreateReviewSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(book.getIsbn()))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByUserIdAndBookIsbn(
                1L, book.getIsbn()))
                .thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Review result = reviewService.createReview(
                1L, book.getIsbn(), 5, "Excellent book!"
        );

        assertEquals(5, result.getRating());
        assertEquals("Excellent book!", result.getComment());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertNotNull(result.getCreatedAt());

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldRejectDuplicateReview() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(book.getIsbn()))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByUserIdAndBookIsbn(
                1L, book.getIsbn()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(
                        1L, book.getIsbn(), 4, "Second review"
                ));

        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    @Test
    void shouldRejectInvalidRating() {

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(
                        1L, book.getIsbn(), 6, "Invalid rating"
                ));

        verifyNoInteractions(
                userRepository,
                bookRepository,
                reviewRepository
        );
    }

    @Test
    void shouldRejectCommentExceedingLimit() {

        String longComment = "A".repeat(2001);

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(
                        1L, book.getIsbn(), 5, longComment
                ));

        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    @Test
    void shouldReturnReviewsForBook() {

        Review review = new Review(
                user, book, 5, "Great book"
        );

        when(reviewRepository.findByBookIsbn(book.getIsbn()))
                .thenReturn(List.of(review));

        List<Review> result =
                reviewService.getReviewsByBook(book.getIsbn());

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());

        verify(reviewRepository)
                .findByBookIsbn(book.getIsbn());
    }

    @Test
    void shouldDeleteExistingReview() {

        when(reviewRepository.existsById(1L))
                .thenReturn(true);

        reviewService.deleteReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void shouldRejectDeletingUnknownReview() {

        when(reviewRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.deleteReview(999L));

        verify(reviewRepository, never())
                .deleteById(999L);
    }
}
