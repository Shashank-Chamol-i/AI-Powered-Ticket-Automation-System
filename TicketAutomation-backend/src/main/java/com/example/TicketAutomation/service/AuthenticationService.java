package com.example.TicketAutomation.service;

import com.example.TicketAutomation.dto.AuthenticationRequest;
import com.example.TicketAutomation.dto.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken =  jwtService.generateToken(userDetails);
            return new AuthenticationResponse(jwtToken);

    }
}
