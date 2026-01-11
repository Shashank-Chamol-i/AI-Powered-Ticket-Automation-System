package com.example.TicketAutomation.specification;

import com.example.TicketAutomation.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> agentOnly(){
        return(root,query,cb)->{
            return cb.equal(root.get("role"),"SUPPORT_AGENT");
        };
    }
    public static Specification<User> enabledAgent(){
        return (root,query,cb)->{
            return cb.isTrue(root.get("enabled"));
        };
    }
}
