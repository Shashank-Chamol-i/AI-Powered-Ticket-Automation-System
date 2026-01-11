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
public class AiMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id" , nullable = false , foreignKey = @ForeignKey(name = "fk_aiMeta_ticket"))
    private Ticket ticket;

    @Column(length = 2000)
    private String summary;

    @Column(name = "predicted_category")
    @Enumerated(EnumType.STRING)
    private Category predicatedCategory;

    @Column(name = "predicted_priority")
    @Enumerated(EnumType.STRING)
    private Priority predictedPriority;

    @CreationTimestamp
    private Instant createdAt;

    private boolean locked;

   private int changeCount = 0;

   @PreUpdate
    public void onUpdate(){
       this.changeCount = this.changeCount+1;
   }

}
