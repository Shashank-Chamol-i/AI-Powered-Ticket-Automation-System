package com.example.TicketAutomation.Repository;

import com.example.TicketAutomation.dto.ShowAllAgents;
import com.example.TicketAutomation.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> , JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

}
