package com.example.TicketAutomation.dto;


import com.example.TicketAutomation.model.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First Name cannot Be Blank")
    @NotNull(message = "Null Not Allowed")
    private String firstName;

    @NotBlank(message = "Last Name cannot be blank")
    @NotNull(message = "Null Not Allowed ")
    private String lastName;

    @Email(message = "Invalid Email")
    @Column(unique = true,nullable = false)
    @NotBlank(message =  "Email Must Required Blank Field not allowed")
    private String email;

    @NotBlank(message = "Password Must required")
    @Size(min = 8 , message = "Password Must be at-least 8 character")
    private String password;

    private Role role = Role.USER;
}
