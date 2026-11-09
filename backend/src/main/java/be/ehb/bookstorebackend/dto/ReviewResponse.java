package be.ehb.bookstorebackend.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long userId,
        String isbn,
        String bookTitle,
        int rating,
        String comment,
        LocalDateTime createdAt
) {
}
