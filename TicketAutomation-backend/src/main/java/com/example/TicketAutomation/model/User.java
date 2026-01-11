package com.example.TicketAutomation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean enabled;

    @CreationTimestamp

    private Instant createdAt;

    @UpdateTimestamp

    private Instant updatedAt;

    @OneToMany(mappedBy = "createdBy" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonIgnore
    private List<Ticket> createdTickets = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonIgnore
    private List<Ticket> assignedTickets = new ArrayList<>();


    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonIgnore
    private List<TicketMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<EmailLog> emails = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<AuditLog> audits = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
