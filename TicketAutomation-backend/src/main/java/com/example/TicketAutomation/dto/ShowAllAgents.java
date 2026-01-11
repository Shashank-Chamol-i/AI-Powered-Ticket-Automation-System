package com.example.TicketAutomation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowAllAgents {
    private String id;
    private String firstName;
    private String email;
    private String Role;

    public ShowAllAgents(String firstName , String email){
        this.firstName = firstName;
        this.email = email;
    }
    public ShowAllAgents(String id , String firstName , String email ){
        this.firstName = firstName;
        this.email = email;
    }

}
