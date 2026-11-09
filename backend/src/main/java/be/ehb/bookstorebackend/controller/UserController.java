package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.dto.UserRegistrationRequest;
import be.ehb.bookstorebackend.dto.UserResponse;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.service.UserService;
import be.ehb.bookstorebackend.dto.UserLoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import be.ehb.bookstorebackend.dto.UserLoginResponse;
import be.ehb.bookstorebackend.service.JwtService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        User savedUser = userService.registerUser(user);

        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginRequest request) {

        User user = userService.authenticateUser(
                request.email(),
                request.password()
        );

        String token = jwtService.generateToken(user);

        UserLoginResponse response = new UserLoginResponse(
                token,
                "Bearer",
                3600
        );

        return ResponseEntity.ok(response);
    }
}
