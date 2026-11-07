package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.dto.ReviewRequest;
import be.ehb.bookstorebackend.dto.ReviewResponse;
import be.ehb.bookstorebackend.entity.Review;
import be.ehb.bookstorebackend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request) {

        Review review = reviewService.createReview(
                request.userId(),
                request.isbn(),
                request.rating(),
                request.comment()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(review));
    }

    @GetMapping("/book/{isbn}")
    public List<ReviewResponse> getReviewsByBook(
            @PathVariable String isbn) {

        return reviewService.getReviewsByBook(isbn)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<ReviewResponse> getReviewsByUser(
            @PathVariable Long userId) {

        return reviewService.getReviewsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id) {

        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getBook().getIsbn(),
                review.getBook().getTitle(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
