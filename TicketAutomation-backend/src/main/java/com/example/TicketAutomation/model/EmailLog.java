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
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false , foreignKey = @ForeignKey(name = "fk_emailLog_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TicketStatus ticketStatus;

    @CreationTimestamp
    private Instant sentAt;
}
