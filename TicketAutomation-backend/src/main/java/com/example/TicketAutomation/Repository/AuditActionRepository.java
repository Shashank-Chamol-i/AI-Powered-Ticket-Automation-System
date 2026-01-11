package com.example.TicketAutomation.Repository;

import com.example.TicketAutomation.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditActionRepository  extends JpaRepository<AuditLog , String> {
}
