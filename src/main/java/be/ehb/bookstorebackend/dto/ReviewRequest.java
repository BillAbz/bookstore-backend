package be.ehb.bookstorebackend.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotBlank(message = "ISBN is required")
        String isbn,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Minimum rating is 1")
        @Max(value = 5, message = "Maximum rating is 5")
        Integer rating,

        @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
        String comment

) {
}
