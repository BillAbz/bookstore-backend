package be.ehb.bookstorebackend.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        String isbn,
        String bookTitle,
        LocalDateTime reservationDate,
        boolean collected
) {
}
