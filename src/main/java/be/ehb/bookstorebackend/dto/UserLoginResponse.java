package be.ehb.bookstorebackend.dto;

public record UserLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
