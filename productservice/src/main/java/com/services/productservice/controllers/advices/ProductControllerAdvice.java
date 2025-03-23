package com.services.productservice.controllers.advices;

import com.services.productservice.dtos.ExcpetionDto;
import com.services.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
//@ControllerAdvice(assignableTypes = {ProductController.class})
public class ProductControllerAdvice {

    @ExceptionHandler(ProductNotFoundException.class)
    //@ResponseStatus(HttpStatus.NOT_FOUND)
    private ResponseEntity<ExcpetionDto> handleProductNotFoundException(ProductNotFoundException e) {
        ExcpetionDto excpetionDto = new ExcpetionDto();
        excpetionDto.setMessage(e.getMessage());
        excpetionDto.setStatus("Failure");
        ResponseEntity<ExcpetionDto> responseEntity = new ResponseEntity<>(excpetionDto, HttpStatus.NOT_FOUND);
        return responseEntity;
    }
}
