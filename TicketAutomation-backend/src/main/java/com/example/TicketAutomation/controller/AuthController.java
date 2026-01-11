package com.example.TicketAutomation.controller;


import com.example.TicketAutomation.dto.AuthenticationRequest;
import com.example.TicketAutomation.dto.AuthenticationResponse;
import com.example.TicketAutomation.dto.RegisterRequest;
import com.example.TicketAutomation.dto.RegisterResponse;
import com.example.TicketAutomation.service.AuthenticationService;
import com.example.TicketAutomation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://127.0.0.2:5500")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest){
         RegisterResponse response = userService.register(registerRequest);
         return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }



}
