package com.services.userservice.controllers.advices;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.services.userservice.exceptions.BasicRoleUnregistered;
import com.services.userservice.exceptions.RoleNotFound;
import com.services.userservice.exceptions.SimilarValuePresent;
import com.services.userservice.exceptions.UnAuthorized;
import com.services.userservice.exceptions.UserAlreadyRegistered;
import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.exceptions.TokenNotFound;
import com.services.userservice.models.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRegistered.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyRegistered(UserAlreadyRegistered ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("UserAlreadyRegistered");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFound ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("UserNotFound");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnAuthorized.class)
    public ResponseEntity<ErrorResponse> handleIncorrectToken(UnAuthorized ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("InvalidToken");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BasicRoleUnregistered.class)
    public ResponseEntity<ErrorResponse> handleIncorrectRole(BasicRoleUnregistered ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("InvalidRole");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(SimilarValuePresent.class)
    public ResponseEntity<ErrorResponse> handleIncorrectRole(SimilarValuePresent ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("InvalidRole");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TokenNotFound.class)
    public ResponseEntity<ErrorResponse> handleTokenNotPresent(TokenNotFound ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("TokenNotFound");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(RoleNotFound.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFound ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("RoleNotFound");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setError("InternalServerError");
        error.setMessage(ex.getMessage());
        error.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "BadRequest",
                        "message", ex.getMessage(),
                        "statusCode", 400));
    }

}
