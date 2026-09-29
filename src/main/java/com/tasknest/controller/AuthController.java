package com.tasknest.controller;

import com.tasknest.dto.LoginRequestDto;
import com.tasknest.dto.UserRequestDto;
import com.tasknest.dto.UserResponseDto;
import com.tasknest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and onboarding")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and log in")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        UserResponseDto response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/me/{id}")
    @Operation(summary = "Fetch current session user profile")
    public ResponseEntity<UserResponseDto> getCurrentUser(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
