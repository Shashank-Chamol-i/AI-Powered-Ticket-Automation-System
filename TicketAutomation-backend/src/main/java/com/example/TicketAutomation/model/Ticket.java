package com.example.TicketAutomation.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    @Column(length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by" , nullable = false , foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to",nullable = true,foreignKey = @ForeignKey(name = "fk_ticket_assigned_to"))
    private User assignedTo;

    @CreationTimestamp
    private Instant createdAt;
    private Instant resolvedAt;


    @OneToMany(mappedBy = "ticket" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonIgnore
    private List<TicketMessage>messages = new ArrayList<>();


    @OneToMany(mappedBy = "ticket",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<AiMetaData> metaData = new ArrayList<>();

    @OneToMany(mappedBy = "ticket",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<AuditLog> auditLogs = new ArrayList<>();

}
