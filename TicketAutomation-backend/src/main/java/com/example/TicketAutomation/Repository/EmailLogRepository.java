package com.example.TicketAutomation.Repository;

import com.example.TicketAutomation.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog,String> {
}
