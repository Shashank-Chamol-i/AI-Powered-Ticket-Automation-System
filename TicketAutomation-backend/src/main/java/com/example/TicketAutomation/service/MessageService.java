package com.example.TicketAutomation.service;

import com.example.TicketAutomation.Repository.TicketMessageRepository;
import com.example.TicketAutomation.Repository.TicketRepository;
import com.example.TicketAutomation.Repository.UserRepository;
import com.example.TicketAutomation.dto.MessageResponse;
import com.example.TicketAutomation.exception.NoSuchTicketIdExist;
import com.example.TicketAutomation.exception.NoSuchUserExist;
import com.example.TicketAutomation.model.*;
import com.example.TicketAutomation.specification.MessageSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;


    @Transactional
    public MessageResponse sendMessageFromAgent(String agentId, String ticketId, String message) {

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()-> new NoSuchTicketIdExist("No such Ticket Exist : "));
        User user = userRepository.findById(agentId).orElseThrow(()-> new NoSuchUserExist("Invalid Agent Id: "));

        if(!ticket.getStatus().equals(TicketStatus.IN_PROGRESS)){
            throw  new IllegalStateException("Ticket not in correct State : ");
        }

        TicketMessage ticketMessage = TicketMessage.builder()
                .ticket(ticket)
                .user(user)
                .message(message)
                .createdAt(Instant.now())
                .build();

        ticket.setStatus(TicketStatus.WAITING_FOR_CUSTOMER);

         ticketRepository.save(ticket);

         TicketMessage response =  ticketMessageRepository.save(ticketMessage);
        return new MessageResponse(
                response.getId(),
                response.getTicket().getId(),
                response.getMessage(),
                response.getUser().getRole().name(),
                response.getUser().getId(),
                response.getCreatedAt()
        );



    }


    @Transactional
    public MessageResponse sendMessageFromUser(String userId, String ticketId, String message) {

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()->new NoSuchUserExist("Invalid User : "));
        User user = userRepository.findById(userId).orElseThrow(()->new NoSuchTicketIdExist("Invalid Ticket"));

        if(!ticket.getStatus().equals(TicketStatus.WAITING_FOR_CUSTOMER)){
            throw new IllegalStateException("Ticket not in correct state : ");
        }
        if(!user.getRole().equals(Role.USER)){
            throw new SecurityException("Not a valid Operation : ");
        }

        TicketMessage ticketMessage = TicketMessage.builder()
                .ticket(ticket)
                .user(user)
                .message(message)
                .createdAt(Instant.now())
                .build();

        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticketRepository.save(ticket);
       TicketMessage response =  ticketMessageRepository.save(ticketMessage);

       return  new MessageResponse(
               response.getId(),
               response.getTicket().getId(),
               response.getMessage(),
               response.getUser().getRole().name(),
               response.getUser().getId(),
               response.getCreatedAt()
       );


    }

    public List<MessageResponse> getConvo(String ticketId , String requestId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()-> new NoSuchTicketIdExist("No such Ticket Exist : "));

        if(ticket.getStatus() == TicketStatus.OPEN){
            throw new IllegalStateException("Ticket not in correct state : ");
        }



        boolean isCreator = ticket.getCreatedBy().getId().equals(requestId);
        boolean isAssigned = ticket.getAssignedTo()!=null && ticket.getAssignedTo().getId().equals(requestId);

        if(!isCreator && !isAssigned){
            throw new IllegalStateException("Not authorized to view the conversation :");
        }

         Specification<TicketMessage> specification = MessageSpecification.getConversations(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findAll(specification);

        return messages.stream()
                .map(m-> new MessageResponse(
                        m.getId(),
                        m.getTicket().getId(),
                        m.getMessage(),
                        m.getUser().getRole().name(),
                        m.getUser().getId(),
                        m.getCreatedAt()
                )).toList();
    }

    public List<MessageResponse> aiThroughputChat(String ticketId){

        Specification <TicketMessage> specification = MessageSpecification.getConversations(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findAll(specification);
        return messages.stream()
                .map(m->new MessageResponse(
                        m.getMessage(),
                        m.getUser().getRole().name()
                )).toList();

    }
}
