package be.ehb.bookstorebackend.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
