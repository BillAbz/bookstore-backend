package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.dto.ReservationRequest;
import be.ehb.bookstorebackend.dto.ReservationResponse;
import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.service.ReservationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody ReservationRequest request) {

        Reservation reservation =
                reservationService.createReservation(
                        request.userId(),
                        request.isbn()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(reservation));
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> getReservationsByUser(
            @PathVariable Long userId) {

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
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id) {

        reservationService.cancelReservation(id);
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
}
