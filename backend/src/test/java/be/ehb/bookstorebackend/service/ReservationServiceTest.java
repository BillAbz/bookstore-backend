package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.entity.Reservation;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.BookRepository;
import be.ehb.bookstorebackend.repository.ReservationRepository;
import be.ehb.bookstorebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private ReservationService reservationService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);

        reservationService = new ReservationService(
                reservationRepository,
                bookRepository,
                userRepository
        );

        user = new User(
                "Bilal", "Test",
                "bilal.test@example.com",
                "hashedPassword"
        );
        user.setId(1L);

        book = new Book(
                "9780134685991",
                "Effective Java",
                "Joshua Bloch",
                "Java programming guide",
                new BigDecimal("45.99"),
                10
        );
    }

    @Test
    void shouldCreateReservationAndDecreaseStock() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findByIsbnForUpdate(book.getIsbn()))
                .thenReturn(Optional.of(book));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(
                1L, book.getIsbn()
        );

        assertEquals(9, book.getStock());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());

        verify(bookRepository).save(book);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenOutOfStock() {
        book.setStock(0);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findByIsbnForUpdate(book.getIsbn()))
                .thenReturn(Optional.of(book));

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(
                        1L, book.getIsbn()
                ));

        verify(reservationRepository, never())
                .save(any(Reservation.class));

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void shouldRejectReservationForUnknownUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(
                        999L, book.getIsbn()
                ));

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }


    @Test
    void shouldCancelReservationAndRestoreStock() {
        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        when(bookRepository.findByIsbnForUpdate(book.getIsbn()))
                .thenReturn(Optional.of(book));

        reservationService.cancelReservation(1L, 1L);

        assertEquals(11, book.getStock());

        verify(bookRepository).findByIsbnForUpdate(book.getIsbn());
        verify(bookRepository).save(book);
        verify(reservationRepository).delete(reservation);
    }


    @Test
    void shouldRejectCancellationOfUnknownReservation() {

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.cancelReservation(999L, 1L)
        );

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void shouldRejectCancellationByAnotherUser() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                AccessDeniedException.class,
                () -> reservationService.cancelReservation(1L, 999L)
        );

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void shouldExpireOldReservationAndRestoreStock() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        reservation.setReservationDate(cutoff.minusHours(1));

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        when(bookRepository.findByIsbnForUpdate(book.getIsbn()))
                .thenReturn(Optional.of(book));

        reservationService.expireReservation(1L, cutoff);

        assertEquals(11, book.getStock());

        verify(bookRepository).save(book);
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void shouldNotExpireCollectedReservation() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setCollected(true);

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        reservation.setReservationDate(cutoff.minusHours(1));

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.expireReservation(1L, cutoff);

        assertEquals(10, book.getStock());

        verify(bookRepository, never()).save(any(Book.class));
        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void shouldNotExpireRecentReservation() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setReservationDate(LocalDateTime.now());

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.expireReservation(1L, cutoff);

        assertEquals(10, book.getStock());

        verify(bookRepository, never()).save(any(Book.class));
        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void shouldCollectReservationSuccessfully() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setReservationDate(LocalDateTime.now().minusHours(1));

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.collectReservation(1L);

        assertTrue(result.isCollected());

        verify(reservationRepository).save(reservation);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldRejectAlreadyCollectedReservation() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setCollected(true);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.collectReservation(1L)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectCollectingExpiredReservation() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setReservationDate(LocalDateTime.now().minusHours(25));

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.collectReservation(1L)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectCancellingCollectedReservation() {

        Reservation reservation = new Reservation(user, book);
        reservation.setId(1L);
        reservation.setCollected(true);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.cancelReservation(1L, 1L)
        );

        verify(bookRepository, never())
                .save(any(Book.class));

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }
}
