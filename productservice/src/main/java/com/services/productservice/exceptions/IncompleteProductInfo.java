package com.services.productservice.exceptions;

public class IncompleteProductInfo extends RuntimeException {
    public IncompleteProductInfo(String s) {
        super(s);
    }
}
