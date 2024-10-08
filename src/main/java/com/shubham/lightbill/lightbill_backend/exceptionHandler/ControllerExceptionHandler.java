package com.shubham.lightbill.lightbill_backend.exceptionHandler;

import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {
    private ApiResponse<Object> handleSqlExceptions(SQLException e){
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return ApiResponse.error("Integrity constraint violation", HttpStatus.CONFLICT.value());
        } else if (e instanceof SQLSyntaxErrorException) {
            return ApiResponse.error("SQL syntax error", HttpStatus.BAD_REQUEST.value());
        } else {
            return ApiResponse.error("Database error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private ApiResponse<Object> handleDataIntegrityException(DataIntegrityViolationException e){
        return ApiResponse.error("Data Integrity violation" + e.getRootCause(), HttpStatus.CONFLICT.value());
    }

    private ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ApiResponse<Object> res = ApiResponse.error("Validation failure", HttpStatus.BAD_REQUEST.value());
        res.setData(String.join(", ", errorMessages));
        return res;
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResponse<Object> responseException(Exception e){
        log.warn(e.getClass().getName());
        log.warn(Arrays.toString(e.getStackTrace()));
        if(e instanceof MethodArgumentNotValidException){
            return handleValidationException((MethodArgumentNotValidException) e);
        }
        else if(e instanceof SQLException){
            return handleSqlExceptions((SQLException) e);
        } else if (e instanceof DataIntegrityViolationException) {
            return handleDataIntegrityException((DataIntegrityViolationException) e);
        } else {
            return ApiResponse.error(e.getMessage(),HttpStatus.BAD_REQUEST.value());
        }
    }
}
