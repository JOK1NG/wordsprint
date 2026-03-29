package com.example.wordsprint.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getCode())
                .body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<Result<Void>> handleBadRequestException(Exception ex) {
        return ResponseEntity.badRequest()
                .body(Result.fail(ErrorCode.BAD_REQUEST, "请求参数不合法"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(Result.fail(ErrorCode.INTERNAL_ERROR, "系统异常"));
    }
}
