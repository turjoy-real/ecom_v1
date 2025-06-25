package com.services.userservice.exceptions;

public class SimilarValuePresent extends RuntimeException {
    public SimilarValuePresent(String message) {
        super(message);
    }
}
