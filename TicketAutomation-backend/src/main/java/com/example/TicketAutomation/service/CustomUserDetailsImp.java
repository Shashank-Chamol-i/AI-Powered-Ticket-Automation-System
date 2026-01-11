package com.example.TicketAutomation.service;

import com.example.TicketAutomation.Repository.UserRepository;
import com.example.TicketAutomation.exception.NoSuchUserExist;
import com.example.TicketAutomation.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsImp implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            return userRepository.findByEmail(email)
                    .orElseThrow(()-> new NoSuchUserExist("No such User exist : "));



    }
}
