package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.entity.Review;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.BookRepository;
import be.ehb.bookstorebackend.repository.ReviewRepository;
import be.ehb.bookstorebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Review createReview(
            Long userId, String isbn, int rating, String comment) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5");
        }

        if (comment != null && comment.length() > 2000) {
            throw new IllegalArgumentException(
                    "Comment cannot exceed 2000 characters");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book not found: " + isbn));

        if (reviewRepository.existsByUserIdAndBookIsbn(userId, isbn)) {
            throw new IllegalArgumentException(
                    "User has already reviewed this book");
        }

        Review review = new Review(user, book, rating, comment);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByBook(String isbn) {
        return reviewRepository.findByBookIsbn(isbn);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteReview(Long reviewId) {

        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException(
                    "Review not found: " + reviewId);
        }

        reviewRepository.deleteById(reviewId);
    }
}
