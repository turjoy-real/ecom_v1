package com.services.productservice.controllers.advices;

import com.services.productservice.dtos.ExcpetionDto;
import com.services.productservice.exceptions.IncompleteProductInfo;
import com.services.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
// @ControllerAdvice(assignableTypes = {ProductController.class})
public class ProductControllerAdvice {

    @ExceptionHandler(ProductNotFoundException.class)
    // @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExcpetionDto> handleProductNotFoundException(ProductNotFoundException e) {
        ExcpetionDto excpetionDto = new ExcpetionDto();
        excpetionDto.setMessage(e.getMessage());
        excpetionDto.setStatus("Failure");
        ResponseEntity<ExcpetionDto> responseEntity = new ResponseEntity<>(excpetionDto, HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    @ExceptionHandler(IncompleteProductInfo.class)
    public ResponseEntity<ExcpetionDto> handleIncompleteProductInfo(IncompleteProductInfo e) {
        ExcpetionDto excpetionDto = new ExcpetionDto();
        excpetionDto.setMessage(e.getMessage());
        excpetionDto.setStatus("Failure");
        ResponseEntity<ExcpetionDto> responseEntity = new ResponseEntity<>(excpetionDto, HttpStatus.PARTIAL_CONTENT);
        return responseEntity;
    }
}
