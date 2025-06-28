package com.services.productservice.controllers.advices;

import com.services.productservice.dtos.ExcpetionDto;
import com.services.productservice.exceptions.IncompleteProductInfo;
import com.services.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.services.productservice.exceptions.NotFoundException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExcpetionDto> handleMismatch(MethodArgumentTypeMismatchException ex) {
        ExcpetionDto error = new ExcpetionDto();
        error.setStatus("BadRequest");
        error.setMessage("Invalid query parameter: " + ex.getName());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExcpetionDto> handleNotFoundException(NotFoundException e) {
        ExcpetionDto excpetionDto = new ExcpetionDto();
        excpetionDto.setMessage(e.getMessage());
        excpetionDto.setStatus("Failure");
        return new ResponseEntity<>(excpetionDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExcpetionDto> handleIllegalArgumentException(IllegalArgumentException e) {
        ExcpetionDto excpetionDto = new ExcpetionDto();
        excpetionDto.setMessage(e.getMessage());
        excpetionDto.setStatus("Failure");
        return new ResponseEntity<>(excpetionDto, HttpStatus.BAD_REQUEST);
    }

}
