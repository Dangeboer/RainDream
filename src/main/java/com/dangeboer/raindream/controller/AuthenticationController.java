package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.dto.LoginRequest;
import com.dangeboer.raindream.model.dto.LoginResponse;
import com.dangeboer.raindream.model.dto.RegisterRequest;
import com.dangeboer.raindream.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(@RequestBody RegisterRequest registerRequest) {
        return authenticationService.register(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getPhone());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }
}
