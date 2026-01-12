package com.example.TicketAutomation.service;


import com.example.TicketAutomation.Repository.AuditActionRepository;
import com.example.TicketAutomation.Repository.EmailLogRepository;
import com.example.TicketAutomation.Repository.TicketRepository;
import com.example.TicketAutomation.Repository.UserRepository;
import com.example.TicketAutomation.dto.*;
import com.example.TicketAutomation.exception.InvalidRoleSelection;
import com.example.TicketAutomation.exception.NoSuchTicketIdExist;
import com.example.TicketAutomation.exception.NoSuchUserExist;
import com.example.TicketAutomation.model.*;
import com.example.TicketAutomation.specification.TicketSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditActionRepository auditActionRepository;
    private final EmailService emailService;
    private final EmailLogRepository emailLogRepository;


    @Transactional
    public TicketResponse create(TicketRequest ticketRequest,User user){
        System.out.println("CHECK POINT 1");
        User userId =  userRepository.findById(user.getId())
                .orElseThrow(()-> new NoSuchUserExist("No Such User Exist !"));

        Category category;
            if(ticketRequest.getCategory()!=null){
                category = ticketRequest.getCategory();
            }else {
                throw new InvalidRoleSelection("Category cannot be null : ");
            }
        System.out.println("CHECK POINT 2");
        Ticket ticket = Ticket.builder()
                .createdBy(userId)
                .title(ticketRequest.getTitle())
                .description(ticketRequest.getDescription())
                .status(TicketStatus.OPEN)
                .category(category)
                .build();

        System.out.println("CHECK POINT 3");

           Ticket savedTicket =  ticketRepository.save(ticket);
           emailService.sendEmail(
                   userId.getEmail(),
                   "Ticket created successfully : ",
                   "<h3>Hello "+ userId.getFirstName() +"</h3>"
                   +"<p> Your Ticket has been generated successfully with <br/>Ticket-Id : <strong>"
                           +savedTicket.getId()+"</strong> and current status of ticket is <strong> "+ ticket.getStatus()+"<strong> </p>"
           );

        System.out.println("CHECK POINT 4");


           EmailLog emailLog = EmailLog.builder()
                   .emailType(EmailType.TICKET_CREATED)
                   .sentAt(Instant.now())
                   .ticketStatus(TicketStatus.OPEN)
                   .user(userId)
                   .build();
           emailLogRepository.save(emailLog);
           return mapToResponse(savedTicket);
    }

    public TicketResponse mapToResponse(Ticket savedTicket){
        TicketResponse ticketResponse = new TicketResponse();
        ticketResponse.setUserId(savedTicket.getCreatedBy().getId());
        ticketResponse.setId(savedTicket.getId());
        ticketResponse.setTitle(savedTicket.getTitle());

        return ticketResponse;
    }

    public TicketDetailResponse getTicket(String ticketId , User user) throws AccessDeniedException {

        Ticket ticket =  ticketRepository.findById(ticketId)
                .orElseThrow(()-> new NoSuchTicketIdExist("No Such Ticket Id exist !"));
          boolean ticketOwner =   ticket.getCreatedBy().getId().equals(user.getId());
          boolean isAssigned =   ticket.getAssignedTo()!=null  && ticket.getAssignedTo().getId().equals(user.getId());
          boolean isAdmin =      user.getRole() == Role.ADMIN;

          if(!(ticketOwner || isAssigned || isAdmin)){
              throw new AccessDeniedException("Not authorized to view this ticket");
          }

        return getTicketDetail(ticket);
    }

    public TicketDetailResponse getTicketDetail(Ticket ticket){
        TicketDetailResponse ticketDetailResponse = new TicketDetailResponse();
        String statusResult = ticket.getStatus() == null ? "Status not Assigned yet" : ticket.getStatus().name();
        String priorityResult = ticket.getPriority() == null ? "Priority not Assigned yet " : ticket.getPriority().name();
        String assignedResult = ticket.getAssignedTo() == null ? "Ticket not Assigned yet " : ticket.getAssignedTo().getId();
        Instant resolvedResult = ticket.getResolvedAt() == null ? null : ticket.getResolvedAt();
        ticketDetailResponse.setId(ticket.getId());
        ticketDetailResponse.setUserId(ticket.getCreatedBy().getId());
        ticketDetailResponse.setTitle(ticket.getTitle());
        ticketDetailResponse.setDescription(ticket.getDescription());
        ticketDetailResponse.setStatus(statusResult);
        ticketDetailResponse.setPriority(priorityResult);
        ticketDetailResponse.setCategory(ticket.getCategory().name());
        ticketDetailResponse.setAssignedTo(assignedResult);
        ticketDetailResponse.setCreatedAt(ticket.getCreatedAt());
        ticketDetailResponse.setResolvedAt(resolvedResult);

        return ticketDetailResponse;
    }

    public List<TicketDetailResponse> getUserTicket (String userId){
        if(ticketRepository.findByCreatedById(userId).isEmpty()){
            throw new NoSuchUserExist("No Tickets Created yet : ");
        }else{
            return ticketRepository.findByCreatedById(userId)
                    .stream()
                    .map(this::getTicketDetail)
                    .toList();
        }
    }


    public List<TicketDetailResponse> filterTicket(TicketFilter filter) {
        return ticketRepository.findAll(TicketSpecification.filterWith(filter))
                .stream()
                .map(this::getTicketDetail)
                .toList();
    }


    public List<ListResponseToAdmin> getOpenUnassignAsc() {
        Specification<Ticket> specification =
                Specification.allOf(TicketSpecification.hasStatus(),TicketSpecification.noAssignment());

        Sort sort = Sort.by(Sort.Direction.ASC,"createdAt");

        return ticketRepository.findAll(specification,sort)
                .stream()
                .map(ticket -> new ListResponseToAdmin(
                        ticket.getId(),
                        ticket.getCreatedBy().getId(),
                        ticket.getTitle()
                )).toList();
    }

    @Transactional
    public AssignmentResponse performAssignment( User admin , String ticketId, String userId){

        if(admin.getRole()!=Role.ADMIN){
            throw new SecurityException("Admin only Operation : ");
        }

         User user = userRepository.findById(userId).orElseThrow(()-> new NoSuchUserExist("No such User Exist : "));
         Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()->new NoSuchTicketIdExist("No such Ticket Id exist :"));


         ticket.setAssignedTo(user);
         ticket.setStatus(TicketStatus.IN_PROGRESS);

         user.setUpdatedAt(Instant.now());

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .ticket(ticket)
                .action(AuditAction.TICKET_ASSIGNED)
                .createdAt(Instant.now())
                .build();
        User savedUser = userRepository.save(user);
        Ticket savedTicket = ticketRepository.save(ticket);
        AuditLog savedAudit = auditActionRepository.save(auditLog);

        AssignmentResponse response = new AssignmentResponse(
                savedAudit.getId(),
                savedAudit.getUser().getId(),
                savedAudit.getTicket().getId(),
                savedAudit.getAction().name(),
                savedAudit.getCreatedAt().toString()
        );
        emailService.sendEmail(
                ticket.getCreatedBy().getEmail(),
                "Ticket Assigned successfully : ",
                "<h3>Hello "+ ticket.getCreatedBy().getFirstName() +"</h3>"
                        +"<p> Your Ticket has been  successfully assigned to Support Agent <br/> Ticket-Id : <strong>"
                        +savedTicket.getId()+"</strong> <br> Current status of ticket is <strong> "+ ticket.getStatus()+"<strong> </p>"
        );
        EmailLog emailLog = EmailLog.builder()
                .emailType(EmailType.TICKET_ASSIGNMENT)
                .sentAt(Instant.now())
                .ticketStatus(TicketStatus.OPEN)
                .user(savedTicket.getCreatedBy())
                .build();
            emailLogRepository.save(emailLog);

        return response;
    }

    public List <TicketDetailResponse>agentAssignedTickets(String agentId) {
        Specification <Ticket> specification = Specification.allOf(TicketSpecification.assignedToAgent(agentId),TicketSpecification.agentWithActiveStatus());
                Sort sort = Sort.by(Sort.Direction.ASC,"createdAt");

                return ticketRepository.findAll(specification,sort)
                        .stream().map(this::getTicketDetail)
                        .toList();
    }

    @Transactional
    public List<PriorityAndStatusResponse> assignPriority(String ticketId ,String agentId, String priority) {
       Ticket ticket =  ticketRepository.findById(ticketId).orElseThrow(()-> new NoSuchTicketIdExist("No such Ticket Exist"));
        User user = userRepository.findById(agentId).orElseThrow(()-> new NoSuchUserExist("No Such Agent Exist"));

       ticket.setStatus(TicketStatus.IN_PROGRESS);
       ticket.setPriority(Priority.valueOf(priority));



       AuditLog auditLog = AuditLog.builder()
               .user(user)
               .ticket(ticket)
               .action(AuditAction.STATUS_CHANGED)
               .createdAt(Instant.now())
               .build();

       Ticket savedTicket = ticketRepository.save(ticket);
       User savedUser = userRepository.save(user);
       AuditLog savedLog = auditActionRepository.save(auditLog);

       PriorityAndStatusResponse priorityAndStatusResponse = new PriorityAndStatusResponse(
               ticket.getPriority().name(),
               ticket.getStatus().name()
       );

        return List.of(priorityAndStatusResponse);

    }


    @Transactional
    public TicketDetailResponse resolveTicket(String ticketId, String agentId) {
        // Ticket Must be waiting , agent must be assigned Agent , Ticket must not be closed

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()->new NoSuchTicketIdExist("Invalid Ticket Id : "));
        User user = userRepository.findById(agentId).orElseThrow(()->new NoSuchUserExist("Invalid Agent Id : "));


        if(!ticket.getStatus().equals(TicketStatus.IN_PROGRESS)){
            throw  new IllegalStateException("Ticket not in correct State ");
        }

        if(!ticket.getAssignedTo().getId().equals(agentId)){
            throw new SecurityException("Invalid Assignment : ");
        }

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(Instant.now());

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .ticket(ticket)
                .action(AuditAction.TICKET_RESOLVED)
                .createdAt(Instant.now())
                .build();
        auditActionRepository.save(auditLog);
        Ticket savedTicket = ticketRepository.save(ticket);

        emailService.sendEmail(
                savedTicket.getCreatedBy().getEmail(),
                "Ticket Resolved successfully : ",
                "<h3>Hello "+ savedTicket.getCreatedBy().getFirstName()+"</h3>"
                        +"<p> Your Ticket has been  successfully Resolved <br/> Ticket-Id : <strong>"
                        +savedTicket.getId()+"</strong> <br/> Current status of ticket is <strong> "+ savedTicket.getStatus()+"<strong> </p>"
        );
        EmailLog emailLog = EmailLog.builder()
                .emailType(EmailType.TICKET_RESOLVED)
                .sentAt(Instant.now())
                .ticketStatus(TicketStatus.OPEN)
                .user(savedTicket.getCreatedBy())
                .build();
        emailLogRepository.save(emailLog);

        return getTicketDetail(savedTicket);

    }

    @Transactional
    public TicketDetailResponse closeTicket(String userId, String ticketId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new NoSuchUserExist("No such User Exist : "));
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()->new NoSuchTicketIdExist("Invalid  Ticket : "));

        if(ticket.getStatus().equals(TicketStatus.CLOSED)){
           throw new IllegalStateException("Ticket is Already Closed : ");
        }

        if(!ticket.getStatus().equals(TicketStatus.RESOLVED)){
            throw new IllegalStateException("Ticket is not resolved yet : ");
        }

        if(!ticket.getCreatedBy().getId().equals(userId)){
            throw new SecurityException("Only ticket Creator can close the ticket");
        }


        ticket.setStatus(TicketStatus.CLOSED);

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .ticket(ticket)
                .action(AuditAction.TICKET_CLOSED)
                .createdAt(Instant.now())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        auditActionRepository.save(auditLog);
        emailService.sendEmail(
                user.getEmail(),
                "Ticket Closed successfully : ",
                "<h3>Hello "+ savedTicket.getCreatedBy().getFirstName() +"</h3>"
                        +"<p> Your Ticket has been  successfully Closed <br/> Ticket-Id : <strong>"
                        +savedTicket.getId()+"</strong> <br> Current status of ticket is <strong> "+ savedTicket.getStatus()+"<strong> </p>"
        );
        EmailLog emailLog = EmailLog.builder()
                .emailType(EmailType.TICKET_CLOSED)
                .sentAt(Instant.now())
                .ticketStatus(TicketStatus.OPEN)
                .user(savedTicket.getCreatedBy())
                .build();
        emailLogRepository.save(emailLog);

        return getTicketDetail(savedTicket);

    }

    public void deleteTicket(User user, String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new RuntimeException("No such Ticket Exist : "));

        if(ticket.getStatus() != TicketStatus.OPEN){
            throw  new IllegalStateException("Delete Operation Cannot be performed on Assigned Ticket : ");
        }

        if(ticket.getCreatedBy().getId().equals(user.getId())){
            ticketRepository.deleteById(ticketId);
        }else{
            throw new IllegalStateException("Ticket can only be deleted by the ticket Owner");
        }

    }
}
