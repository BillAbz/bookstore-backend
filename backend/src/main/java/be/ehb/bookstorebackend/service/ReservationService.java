package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.BookRepository;
import be.ehb.bookstorebackend.repository.ReservationRepository;
import be.ehb.bookstorebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Reservation createReservation(Long userId, String isbn) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        Book book = bookRepository.findByIsbnForUpdate(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book not found: " + isbn));

        if (book.getStock() <= 0) {
            throw new IllegalArgumentException(
                    "Book is out of stock");
        }

        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        Reservation reservation = new Reservation(user, book);
        return reservationRepository.save(reservation);
    }


    @Transactional
    public void cancelReservation(Long reservationId, Long authenticatedUserId) {

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId));

        if (!reservation.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You cannot cancel another user's reservation"
            );
        }

        if (reservation.isCollected()) {
            throw new IllegalArgumentException(
                    "Collected reservations cannot be cancelled"
            );
        }

        String isbn = reservation.getBook().getIsbn();

        Book book = bookRepository.findByIsbnForUpdate(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book not found: " + isbn));

        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        reservationRepository.delete(reservation);
    }

    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getReservationsByBook(String isbn) {
        return reservationRepository.findByBookIsbn(isbn);
    }

    @Transactional
    public void expireReservation(Long reservationId, LocalDateTime cutoff) {

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElse(null);

        if (reservation == null) {
            return;
        }

        if (reservation.isCollected()
                || !reservation.getReservationDate().isBefore(cutoff)) {
            return;
        }

        Book book = bookRepository
                .findByIsbnForUpdate(reservation.getBook().getIsbn())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book not found for reservation"
                ));

        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        reservationRepository.delete(reservation);
    }

    @Transactional
    public Reservation collectReservation(Long reservationId) {

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId
                ));

        if (reservation.isCollected()) {
            throw new IllegalArgumentException(
                    "Reservation has already been collected"
            );
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        if (reservation.getReservationDate().isBefore(cutoff)) {
            throw new IllegalArgumentException(
                    "Reservation has expired"
            );
        }

        reservation.setCollected(true);

        return reservationRepository.save(reservation);
    }
}
