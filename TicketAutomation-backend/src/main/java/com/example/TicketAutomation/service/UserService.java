package com.example.TicketAutomation.service;

import com.example.TicketAutomation.Repository.TicketRepository;
import com.example.TicketAutomation.Repository.UserRepository;
import com.example.TicketAutomation.dto.RegisterRequest;
import com.example.TicketAutomation.dto.RegisterResponse;
import com.example.TicketAutomation.dto.ShowAllAgents;
import com.example.TicketAutomation.exception.EmailAlreadyExist;
import com.example.TicketAutomation.exception.InvalidRoleSelection;
import com.example.TicketAutomation.model.Role;
import com.example.TicketAutomation.model.TicketStatus;
import com.example.TicketAutomation.model.User;
import com.example.TicketAutomation.specification.TicketSpecification;
import com.example.TicketAutomation.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public RegisterResponse register(RegisterRequest registerRequest){
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw  new EmailAlreadyExist("Email already exist try another one : ");
         }

        if(registerRequest.getRole() != Role.USER){
            throw new InvalidRoleSelection("Public registration only allowed for User role :");
        }
        Role role = registerRequest.getRole();
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .enabled(true)
                .build();

            User savedUser =  userRepository.save(user);
            return new RegisterResponse(true,savedUser.getFirstName()+" : Successfully Registered",savedUser.getId());
    }



    public List<ShowAllAgents> showAllAgents() {
        Specification<User> specification =
                Specification.allOf(UserSpecification.agentOnly(),UserSpecification.enabledAgent());

        Sort sort = Sort.by(Sort.Direction.ASC,"updatedAt");
        return userRepository.findAll(specification,sort)
                .stream()
                .map(user-> new ShowAllAgents(
                        user.getId(),
                        user.getFirstName(),
                        user.getEmail(),
                        user.getRole().name()
                )).toList();
    }


    public List<ShowAllAgents> getDbList() {
        return  userRepository.findAll()
                 .stream()
                 .map(user-> new ShowAllAgents(
                         user.getId(),
                         user.getFirstName(),
                         user.getEmail(),
                         user.getRole().name()
                 )).toList();
    }

    public ShowAllAgents assignAgent(User user , String userId){
        if(user.getRole() != Role.ADMIN){
            throw new SecurityException("Not Allowed to Perform Operation : ");
        }

        User agent = userRepository.findById(userId).orElseThrow(()->new RuntimeException("No such User Exist : "));

       agent.setRole(Role.SUPPORT_AGENT);
       User savedAgent = userRepository.save(agent);

       return new ShowAllAgents(
               agent.getFirstName(),
               agent.getEmail()
       );

    }
}
