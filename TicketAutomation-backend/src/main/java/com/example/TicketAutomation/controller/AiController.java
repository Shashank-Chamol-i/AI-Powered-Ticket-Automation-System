package com.example.TicketAutomation.controller;

import com.example.TicketAutomation.dto.AnalysisResponse;
import com.example.TicketAutomation.model.User;
import com.example.TicketAutomation.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AiController {
    private final AiServices aiServices;



    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER','SUPPORT_AGENT')")
    public ResponseEntity<AnalysisResponse> getSummary(
            @AuthenticationPrincipal User request,
            @RequestParam (value = "TID") String ticketId){
    return ResponseEntity.ok( aiServices.getSummary(request.getId(),ticketId));
    }


}
