package com.example.TicketAutomation.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private String auditId;
    private String userId;
    private String ticketId;
    private String auditAction;
    private String timeStamp;
}
