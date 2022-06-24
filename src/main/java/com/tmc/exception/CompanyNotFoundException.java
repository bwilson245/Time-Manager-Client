package com.tmc.exception;

public class CompanyNotFoundException extends RuntimeException{
    public CompanyNotFoundException() {
    }
    public CompanyNotFoundException(String message) {
        super(message);
    }
    public CompanyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public CompanyNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        String message = "CompanyNotFoundException: {Could not find company: " + this.getMessage()  + "}";
        return message;
    }
}
