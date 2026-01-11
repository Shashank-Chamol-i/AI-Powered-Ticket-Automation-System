package com.example.TicketAutomation.specification;

import com.example.TicketAutomation.model.TicketMessage;
import org.springframework.data.jpa.domain.Specification;

public class MessageSpecification {

    public static Specification<TicketMessage> getConversations(String ticketId){
        return (root, query, criteriaBuilder) -> {

            if (query != null) {
                query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
            }else{
                throw new NullPointerException("No such query exist : ");
            }

            return criteriaBuilder.equal(root.get("ticket").get("id"),ticketId);
        };
    }
}
