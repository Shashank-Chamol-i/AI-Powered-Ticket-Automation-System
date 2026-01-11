package com.example.TicketAutomation.dto;

import com.example.TicketAutomation.model.Category;
import com.example.TicketAutomation.model.Priority;
import com.example.TicketAutomation.model.Ticket;
import com.example.TicketAutomation.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilter {
    String createdBy;
    TicketStatus status;
    Category category;
    Priority priority;

}
