package com.rabo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RaboExceptionController {
	 @ExceptionHandler(value = RaboException.class)
	   public ResponseEntity<Object> exception(RaboException exception) {
	      return new ResponseEntity<>("Error in Rabo Transaction Handler", HttpStatus.INTERNAL_SERVER_ERROR);
	   }
}
