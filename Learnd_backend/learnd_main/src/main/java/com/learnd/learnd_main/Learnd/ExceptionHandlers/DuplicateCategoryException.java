package com.learnd.learnd_main.Learnd.ExceptionHandlers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DuplicateCategoryException {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(DataIntegrityViolationException ex) {

        //if instance of db's ConstraintViolationException
        // and constraint exists and has the name of unique_name_for_user
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException constraint &&
                constraint.getConstraintName() != null &&
                constraint.getConstraintName().equalsIgnoreCase("unique_name_for_user ")) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Category already exists for this user");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid data");
    }
}

