package com.example.TicketAutomation.exception;

public class NoSuchTicketIdExist extends RuntimeException {
    public NoSuchTicketIdExist(String message) {
        super(message);
    }
}
