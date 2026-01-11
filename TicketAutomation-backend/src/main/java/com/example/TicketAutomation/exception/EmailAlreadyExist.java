package com.example.TicketAutomation.exception;

public class EmailAlreadyExist extends RuntimeException {
    public EmailAlreadyExist(String message) {
        super(message);
    }
}
