package com.example.TicketAutomation.controller;


import com.example.TicketAutomation.dto.*;
import com.example.TicketAutomation.model.*;
import com.example.TicketAutomation.service.AiServices;
import com.example.TicketAutomation.service.TicketService;
import com.example.TicketAutomation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final AiServices aiServices;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TicketResponse> create(@AuthenticationPrincipal User user ,@RequestBody TicketRequest ticketRequest){
        TicketResponse response =  ticketService.create(ticketRequest ,user);
        aiServices.analyzeOpenTicket(response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String ticketId){
        ticketService.deleteTicket(user,ticketId);
       return  ResponseEntity.noContent().build();
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<TicketDetailResponse> getTicket(@AuthenticationPrincipal User user,@PathVariable String  ticketId) throws AccessDeniedException {
    return ResponseEntity.ok(ticketService.getTicket(ticketId,user));
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<TicketDetailResponse>> getUserTicket( @AuthenticationPrincipal User user){
        return ResponseEntity.ok(ticketService.getUserTicket(user.getId()));
    }

   @GetMapping("/filter")
   @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<TicketDetailResponse>> getTicketFilter(
            @AuthenticationPrincipal User user,
           @RequestParam (required = false)TicketStatus status,
           @RequestParam (required = false)Category category,
           @RequestParam (required = false)Priority priority

           ){
            TicketFilter filter = new TicketFilter(user.getId(),status,category,priority);
            return ResponseEntity.ok(ticketService.filterTicket(filter));
   }

    // Admin Only
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListResponseToAdmin>> getOpenUnassignAsc(){
        return ResponseEntity.ok(ticketService.getOpenUnassignAsc());
    }

    //Admin only
    @GetMapping("/get/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShowAllAgents>> getDbList(){
        return ResponseEntity.ok(userService.getDbList());
    }

    // Admin only
    @PostMapping("/create/agent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowAllAgents> createAgent(
            @AuthenticationPrincipal User admin,
            @RequestParam String userId){

        return ResponseEntity.ok(userService.assignAgent(admin , userId));
    }

    // Admin Only
    @GetMapping("/agent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShowAllAgents>> showAllAgentsList(){
        return ResponseEntity.ok(userService.showAllAgents());
    }
    // AdminOnly
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> performAssignments(
            @AuthenticationPrincipal User admin,
            @RequestParam(value = "TID" , required = true) String ticketId,
            @RequestParam(value = "AID",required = true) String userId){
        return ResponseEntity.ok(ticketService.performAssignment(admin,ticketId,userId));
    }

    //  Agent
    @GetMapping ("/assignTicket")
    @PreAuthorize("hasAnyRole('SUPPORT_AGENT')")
    public ResponseEntity<List<TicketDetailResponse>> getMyTickets(@AuthenticationPrincipal User agent){
        return ResponseEntity.ok(ticketService.agentAssignedTickets(agent.getId()));
    }

    // Agent Update Providing Priority
    @PostMapping("/agent/action")
    @PreAuthorize("hasAnyRole('SUPPORT_AGENT')")
    public ResponseEntity<List<PriorityAndStatusResponse>> assignPriority(
            @RequestHeader (value = "TID" , required = true) String ticketId ,
            @AuthenticationPrincipal User agent,
            @RequestHeader (value = "PRI" ,  required = true) String priority){
        return ResponseEntity.ok(ticketService.assignPriority(ticketId ,agent.getId(), priority));
    }

    // Resolve Ticket
    @PostMapping("/agent/resolve")
    @PreAuthorize("hasAnyRole('SUPPORT_AGENT')")
    public ResponseEntity<TicketDetailResponse> resolveTicket(
            @RequestHeader(value = "TID" , required = true) String ticketId,
            @AuthenticationPrincipal User agent){
        return ResponseEntity.ok(ticketService.resolveTicket(ticketId,agent.getId()));
    }

    // Close Ticket
    @PostMapping("/user/close")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<TicketDetailResponse> closeTicket(
            @AuthenticationPrincipal User user,
            @RequestParam (value = "TID",required = true) String ticketId
    ){
        return ResponseEntity.ok(ticketService.closeTicket(user.getId(),ticketId));
    }




}
