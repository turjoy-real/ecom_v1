package com.services.userservice.exceptions;

public class UserAlreadyRegistered extends RuntimeException{
    public UserAlreadyRegistered(String msg){
        super(msg);
    }
}
