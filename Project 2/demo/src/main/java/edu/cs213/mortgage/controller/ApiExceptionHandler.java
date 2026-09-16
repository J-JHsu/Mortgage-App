package edu.cs213.mortgage.controller;

import edu.cs213.mortgage.model.MortgageApi.ApiError;
import edu.cs213.mortgage.service.ApiFailure;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(ApiFailure.class)
    public ResponseEntity<ApiError> business(ApiFailure failure) {
        return ResponseEntity.status(failure.status()).body(new ApiError(failure.code(), failure.getMessage()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> malformed() {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST",
                "Supply a JSON object with known fields, integer code arrays, and numeric range bounds."));
    }
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiError> database(SQLException failure) {
        LOG.error("Mortgage database operation failed", failure);
        if ("40001".equals(failure.getSQLState())) {
            return ResponseEntity.status(409).body(new ApiError("PACKAGING_CONFLICT",
                    "Packaging was rolled back because the eligible set changed. Review again."));
        }
        return ResponseEntity.status(503).body(new ApiError("DATABASE_ERROR",
                "The database operation could not complete. Check database availability and search again before packaging."));
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> method() {
        return ResponseEntity.status(405).body(new ApiError("METHOD_NOT_ALLOWED", "Use the documented HTTP method."));
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> media() {
        return ResponseEntity.status(415).body(new ApiError("UNSUPPORTED_MEDIA_TYPE", "Use application/json."));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception failure) {
        LOG.error("Unexpected mortgage API failure", failure);
        return ResponseEntity.internalServerError().body(new ApiError("INTERNAL_ERROR", "The request could not complete."));
    }
}
