package com.example.TicketAutomation.exception;

import com.example.TicketAutomation.dto.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler  {

  @ExceptionHandler(EmailAlreadyExist.class)
    public ResponseEntity<RegisterResponse> handleEmailExist(EmailAlreadyExist e){
      RegisterResponse response = new RegisterResponse(false,e.getMessage(),null);
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidRoleSelection.class)
    public ResponseEntity<RegisterResponse> handleInvalidRole(InvalidRoleSelection e){
      RegisterResponse response = new RegisterResponse(false,e.getMessage(),null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
