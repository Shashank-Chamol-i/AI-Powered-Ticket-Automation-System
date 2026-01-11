package com.example.TicketAutomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriorityAndStatusResponse {
    private String priority;
    private String status;
}
