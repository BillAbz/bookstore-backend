package be.ehb.bookstorebackend.service;

import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.repository.UserRepository;
import be.ehb.bookstorebackend.exception.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(
                userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserWithHashedPassword() {
        User user = new User(
                "Bilal",
                "Test",
                "bilal.test@example.com",
                "Test12345!"
        );

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode("Test12345!"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(user);

        assertEquals("hashedPassword", result.getPassword());
        verify(passwordEncoder).encode("Test12345!");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User user = new User(
                "Bilal",
                "Test",
                "bilal.test@example.com",
                "Test12345!"
        );

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> userService.registerUser(user));

        assertEquals("Email already registered",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldRejectEmptyEmail() {
        User user = new User(
                "Bilal", "Test", "", "Test12345!"
        );

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(user));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectEmptyPassword() {
        User user = new User(
                "Bilal",
                "Test",
                "bilal.test@example.com",
                ""
        );

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(user));

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldAuthenticateUserWithCorrectPassword() {
        User user = new User(
                "Bilal", "Test", "bilal.test@example.com",
                "hashedPassword"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "Test12345!", "hashedPassword"))
                .thenReturn(true);

        User result = userService.authenticateUser(
                user.getEmail(), "Test12345!");

        assertEquals(user.getEmail(), result.getEmail());
        verify(passwordEncoder).matches(
                "Test12345!", "hashedPassword");
    }

    @Test
    void shouldRejectIncorrectPassword() {
        User user = new User(
                "Bilal", "Test", "bilal.test@example.com",
                "hashedPassword"
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword", "hashedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser(
                        user.getEmail(), "WrongPassword"));
    }

    @Test
    void shouldRejectUnknownEmail() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(java.util.Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser(
                        email, "Test12345!"));

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

}
