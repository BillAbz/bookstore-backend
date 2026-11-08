package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationExpirationService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReservationExpirationService(
            ReservationRepository reservationRepository,
            ReservationService reservationService) {

        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void expireOldReservations() {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<Reservation> expiredReservations =
                reservationRepository
                        .findByCollectedFalseAndReservationDateBefore(cutoff);

        for (Reservation reservation : expiredReservations) {
            reservationService.expireReservation(
                    reservation.getId(),
                    cutoff
            );
        }
    }
}
