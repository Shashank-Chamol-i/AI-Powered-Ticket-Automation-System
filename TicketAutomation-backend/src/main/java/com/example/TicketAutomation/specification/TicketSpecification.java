package com.example.TicketAutomation.specification;

import com.example.TicketAutomation.dto.TicketFilter;
import com.example.TicketAutomation.exception.NoSuchUserExist;
import com.example.TicketAutomation.model.Ticket;
import com.example.TicketAutomation.model.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {
    public static Specification<Ticket> filterWith(TicketFilter filter){
        return (root,query,cb)->{

            List<Predicate> predicates = new ArrayList<>();

            if(filter.getCreatedBy()!=null){
                predicates.add(cb.equal(root.get("createdBy").get("id"),filter.getCreatedBy()));
            }else{
                throw new NoSuchUserExist("No such User exist ");
            }

            if(filter.getStatus()!=null){
                predicates.add(cb.equal(root.get("status"),filter.getStatus()));
            }

            if(filter.getCategory()!=null){
                predicates.add(cb.equal(root.get("category"),filter.getCategory()));
            }
            if(filter.getPriority()!=null){
                predicates.add(cb.equal(root.get("priority"),filter.getPriority()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

   public static Specification<Ticket> hasStatus(){
        return (root,query,cb)->{
            return cb.equal(root.get("status"),"OPEN");
       };
   }
   public static Specification<Ticket> noAssignment(){
        return (root,query,cb)->{
          return cb.isNull(root.get("assignedTo"));
        };
   }


   public static Specification<Ticket> assignedToAgent(String agentId){
        return (root,query,cb)->{
            return cb.equal(root.get("assignedTo").get("id"),agentId);
        };
   }

   public static Specification<Ticket> agentWithActiveStatus(){
        return (root,query,cb)->{
           return root.get("status").in(
                   TicketStatus.IN_PROGRESS,
                   TicketStatus.WAITING_FOR_CUSTOMER
           );
        };
   }




}
