package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.dto.ReservationRequest;
import be.ehb.bookstorebackend.dto.ReservationResponse;
import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId = Long.valueOf(jwt.getSubject());

        if (!authenticatedUserId.equals(request.userId())) {
            throw new AccessDeniedException(
                    "You cannot create a reservation for another user"
            );
        }

        Reservation reservation = reservationService.createReservation(
                authenticatedUserId,
                request.isbn()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(reservation));
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> getReservationsByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId = Long.valueOf(jwt.getSubject());

        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException(
                    "You can only view your own reservations"
            );
        }

        return reservationService.getReservationsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/book/{isbn}")
    public List<ReservationResponse> getReservationsByBook(
            @PathVariable String isbn) {

        return reservationService.getReservationsByBook(isbn)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId = Long.valueOf(jwt.getSubject());

        reservationService.cancelReservation(
                id,
                authenticatedUserId
        );

        return ResponseEntity.noContent().build();
    }


    private ReservationResponse toResponse(
            Reservation reservation) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getBook().getIsbn(),
                reservation.getBook().getTitle(),
                reservation.getReservationDate(),
                reservation.isCollected()
        );
    }

    @PatchMapping("/{id}/collect")
    public ResponseEntity<ReservationResponse> collectReservation(
            @PathVariable Long id) {

        Reservation reservation =
                reservationService.collectReservation(id);

        return ResponseEntity.ok(toResponse(reservation));
    }
}
