package com.tmc.exception;

public class EmployeeNotFoundException extends RuntimeException{
    public EmployeeNotFoundException() {
    }
    public EmployeeNotFoundException(String message) {
        super(message);
    }
    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public EmployeeNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        String message = "EmployeeNotFoundException: {Could not find employee: " + this.getMessage()  + "}";
        return message;
    }
}
