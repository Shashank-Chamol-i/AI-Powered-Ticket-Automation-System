package com.example.TicketAutomation.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class TicketDetailResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private String  assignedTo;
    private Instant createdAt;
    private Instant resolvedAt;

}
