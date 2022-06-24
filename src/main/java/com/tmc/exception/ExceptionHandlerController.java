package com.tmc.exception;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { RuntimeException.class, TimesheetNotFoundException.class, EmployeeNotFoundException.class,
                CustomerNotFoundException.class, CompanyNotFoundException.class, UncheckedExecutionException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        Map<String, Object> bodyOfResponse = new HashMap<>();
        bodyOfResponse.put("timestamp", new Date());
        bodyOfResponse.put("code", HttpStatus.CONFLICT.value());
        bodyOfResponse.put("exception", ex.getClass().toString());
        bodyOfResponse.put("message", ex.getMessage());
        bodyOfResponse.put("path", request.getDescription(false).split("=")[1]);
        ex.printStackTrace();
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
}
