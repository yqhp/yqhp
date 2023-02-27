package com.yqhp.file.web.controller.advice;

import com.yqhp.common.minio.exception.MinioException;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.common.web.model.Response;
import com.yqhp.common.web.model.ResponseCode;
import com.yqhp.file.web.enums.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@RestControllerAdvice({"com.yqhp.file.web.controller"})
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error("unexpected exception", e);
        return new Response<>(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Response handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        return CollectionUtils.isEmpty(fieldErrors)
                ? new Response<>(HttpStatus.BAD_REQUEST)
                : new Response<>(ResponseCodeEnum.FIELD_ERRORS, fieldErrors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Response handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        return CollectionUtils.isEmpty(constraintViolations)
                ? new Response<>(HttpStatus.BAD_REQUEST)
                : new Response<>(ResponseCodeEnum.FIELD_ERRORS,
                constraintViolations.stream().map(violation -> Map.of("defaultMessage", violation.getMessage())).collect(Collectors.toList()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        return CollectionUtils.isEmpty(fieldErrors)
                ? new Response<>(HttpStatus.BAD_REQUEST)
                : new Response<>(ResponseCodeEnum.FIELD_ERRORS, fieldErrors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Response handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Response handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Response handleIllegalArgumentException(IllegalArgumentException e) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /*************************************************************************************************/

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MinioException.class)
    public Response handleMinioException(MinioException e) {
        log.error("minio err", e);
        return new Response<>(ResponseCodeEnum.OSS_ERROR, e.getCause().getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServiceException.class)
    public Response handleServiceException(ServiceException e) {
        ResponseCode responseCode = e.getResponseCode();
        return new Response<>(responseCode);
    }

}
