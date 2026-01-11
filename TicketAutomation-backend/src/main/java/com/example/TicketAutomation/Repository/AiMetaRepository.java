package com.example.TicketAutomation.Repository;


import com.example.TicketAutomation.model.AiMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AiMetaRepository  extends JpaRepository<AiMetaData, String > {
    AiMetaData findByTicketId(String ticketId);
}
