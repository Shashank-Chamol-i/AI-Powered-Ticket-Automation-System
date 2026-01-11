package com.example.TicketAutomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TicketAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketAutomationApplication.class, args);
	}

}
