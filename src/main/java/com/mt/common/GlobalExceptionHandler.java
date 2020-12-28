package com.mt.common;

import com.mt.common.idempotent.exception.ChangeNotFoundException;
import com.mt.common.idempotent.exception.CustomByteArraySerializationException;
import com.mt.common.idempotent.exception.HangingTransactionException;
import com.mt.common.idempotent.exception.RollbackNotSupportedException;
import com.mt.common.validation.ValidationErrorException;
import com.mt.common.validation.ValidationFailedException;
import com.mt.common.rest.exception.*;
import com.mt.common.sql.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            TransactionSystemException.class,
            IllegalArgumentException.class,
            ObjectOptimisticLockingFailureException.class,
            JwtTokenExtractException.class,
            UnsupportedQueryException.class,
            MaxPageSizeExceedException.class,
            EmptyWhereClauseException.class,
            UnsupportedPatchOperationException.class,
            UpdateFiledValueException.class,
            HangingTransactionException.class,
            RollbackNotSupportedException.class,
            PatchCommandExpectNotMatchException.class,
            AggregateNotExistException.class,
            AggregatePatchException.class,
            QueryBuilderNotFoundException.class,
            EmptyQueryValueException.class,
            UnknownWhereClauseException.class,
            ChangeNotFoundException.class,
            UserIdNotFoundException.class,
            ValidationFailedException.class,
            AggregateOutdatedException.class,
            IllegalJwtException.class
    })
    protected ResponseEntity<Object> handle400Exception(RuntimeException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(AppConstant.HTTP_HEADER_ERROR_ID, errorMessage.getErrorId());
        return handleExceptionInternal(ex, errorMessage, httpHeaders, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {
            RuntimeException.class,
            JwtTokenRetrievalException.class,
            DeepCopyException.class,
            CustomByteArraySerializationException.class,
            ValidationErrorException.class
    })
    protected ResponseEntity<Object> handle500Exception(RuntimeException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(AppConstant.HTTP_HEADER_ERROR_ID, errorMessage.getErrorId());
        return handleExceptionInternal(ex, errorMessage, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    //@note duplicate key exception should result in 400
    @ExceptionHandler(value = {
            DataIntegrityViolationException.class,
    })
    protected ResponseEntity<Object> handle200Exception(RuntimeException ex, WebRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(AppConstant.HTTP_HEADER_SUPPRESS, AppConstant.HTTP_HEADER_SUPPRESS_REASON_INTEGRITY_VIOLATION);
        return handleExceptionInternal(ex, null, httpHeaders, HttpStatus.OK, request);
    }
}
