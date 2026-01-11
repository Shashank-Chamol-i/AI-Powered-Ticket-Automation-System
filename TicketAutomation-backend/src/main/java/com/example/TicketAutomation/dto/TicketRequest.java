package com.example.TicketAutomation.dto;

import com.example.TicketAutomation.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    private String userId;

    @NotBlank(message = "Title Cannot be Empty")
    @Size( min = 5, max = 250 ,message = "5 - 150 Character at-least required")
    private String title;

    @NotBlank(message = "Description cannot be Empty ")
    @Size(min = 20 ,max = 5000, message = "20 - 5000 Character at -least required")
    private String description;

    @NotNull
    private Category category;
}
