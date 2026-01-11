package com.example.TicketAutomation.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListResponseToAdmin {
    private String id;
    private String userId;
    private String title;

}