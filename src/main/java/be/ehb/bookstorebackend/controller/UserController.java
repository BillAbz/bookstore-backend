package be.ehb.bookstorebackend.controller;

import be.ehb.bookstorebackend.dto.UserRegistrationRequest;
import be.ehb.bookstorebackend.dto.UserResponse;
import be.ehb.bookstorebackend.entity.User;
import be.ehb.bookstorebackend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
