package com.example.TicketAutomation.controller;


import com.example.TicketAutomation.dto.AnalysisResponse;
import com.example.TicketAutomation.dto.MessageResponse;
import com.example.TicketAutomation.model.TicketMessage;
import com.example.TicketAutomation.model.User;
import com.example.TicketAutomation.service.AiServices;
import com.example.TicketAutomation.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final AiServices aiServices;

    // In_progress Agent send Message to user and Convert the status into Waiting

    @PostMapping("/agent/send")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<MessageResponse> messageFromAgent(
            @AuthenticationPrincipal User agent,
            @RequestParam(value = "TID", required = true)String ticketId,
            @RequestBody String message
    ){
       MessageResponse response =  messageService.sendMessageFromAgent(agent.getId(),ticketId,message);
        return ResponseEntity.ok(response);
    }

    // IN_Waiting User Send message to Agent and Convert the status from Waiting to In_progress

    @PostMapping("/user/send")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> messageFromUser(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "TID" , required = true)String ticketId,
            @RequestBody String message
    ){

        MessageResponse response = messageService.sendMessageFromUser(user.getId(),ticketId,message);
         aiServices.analyzeWithChat(ticketId);
        return ResponseEntity.ok(response);
    }

    // Conversation
    @GetMapping("/conversation")
    @PreAuthorize("hasRole('USER','SUPPORT_AGENT')")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam (value = "TID" , required = true) String ticketId,
            @AuthenticationPrincipal User request
    ){

        return ResponseEntity.ok(messageService.getConvo(ticketId , request.getId()));
    }
}
