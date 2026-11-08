package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationExpirationServiceTest {

    private ReservationRepository reservationRepository;
    private ReservationService reservationService;
    private ReservationExpirationService expirationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        reservationService = mock(ReservationService.class);

        expirationService = new ReservationExpirationService(
                reservationRepository,
                reservationService
        );
    }

    @Test
    void shouldProcessExpiredReservations() {

        Reservation first = new Reservation();
        first.setId(1L);

        Reservation second = new Reservation();
        second.setId(2L);

        when(reservationRepository
                .findByCollectedFalseAndReservationDateBefore(
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(first, second));

        expirationService.expireOldReservations();

        ArgumentCaptor<LocalDateTime> cutoffCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(reservationRepository)
                .findByCollectedFalseAndReservationDateBefore(
                        cutoffCaptor.capture()
                );

        LocalDateTime cutoff = cutoffCaptor.getValue();

        verify(reservationService)
                .expireReservation(1L, cutoff);

        verify(reservationService)
                .expireReservation(2L, cutoff);
    }

    @Test
    void shouldDoNothingWhenNoReservationsExpired() {

        when(reservationRepository
                .findByCollectedFalseAndReservationDateBefore(
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of());

        expirationService.expireOldReservations();

        verify(reservationService, never())
                .expireReservation(
                        anyLong(),
                        any(LocalDateTime.class)
                );
    }
}
