package com.example.TicketAutomation.exception;

public class NoSuchUserExist extends RuntimeException {
    public NoSuchUserExist(String message) {
        super(message);
    }
}
