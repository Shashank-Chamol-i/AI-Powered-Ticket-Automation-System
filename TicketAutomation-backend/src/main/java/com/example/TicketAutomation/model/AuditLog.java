package com.example.TicketAutomation.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy  = GenerationType.UUID)
    private String id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false , foreignKey = @ForeignKey(name = "fk_auditLog_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id",nullable = false,foreignKey = @ForeignKey(name = "fk_auditLog_ticket"))
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AuditAction action;

    @CreationTimestamp
    private Instant createdAt;
}
