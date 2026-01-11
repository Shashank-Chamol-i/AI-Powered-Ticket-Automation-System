package com.example.TicketAutomation.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id",nullable = false,foreignKey = @ForeignKey(name="fk_ticketMessage_ticket"))
    private Ticket ticket;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false,foreignKey = @ForeignKey(name = "fk_ticketMessage_user"))
    private User user;

    @Column(length = 2000)
    private String message;

    @CreationTimestamp
    private Instant createdAt;
}
