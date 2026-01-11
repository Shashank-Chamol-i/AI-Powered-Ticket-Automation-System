package com.example.TicketAutomation.Repository;

import com.example.TicketAutomation.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TicketMessageRepository extends JpaRepository<TicketMessage , String> , JpaSpecificationExecutor<TicketMessage> {

List<TicketMessage>findByTicketId(String ticketId);
}
