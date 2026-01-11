package com.example.TicketAutomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String messageId;
    private String ticketId;
    private String message;
    private String senderType;
    private String senderId;
    private Instant sendAt;

    public MessageResponse(String message , String senderType){
        this.message = message;
        this.senderType = senderType;
    }
}
