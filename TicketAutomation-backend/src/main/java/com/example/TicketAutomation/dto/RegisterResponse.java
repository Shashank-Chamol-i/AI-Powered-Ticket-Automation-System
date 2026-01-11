package com.example.TicketAutomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String message;
    private String userId;
}
