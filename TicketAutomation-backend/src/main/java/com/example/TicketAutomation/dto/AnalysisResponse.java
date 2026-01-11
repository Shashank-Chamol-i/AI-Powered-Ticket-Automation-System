package com.example.TicketAutomation.dto;


import com.example.TicketAutomation.model.Category;
import com.example.TicketAutomation.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private Category predictedCategory;
    private Priority predictedPriority;
    private String summary;

}
