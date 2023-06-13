/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.web.controller.advice;

import com.yqhp.agent.web.enums.ResponseCodeEnum;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.common.web.model.Response;
import com.yqhp.common.web.model.ResponseCode;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@RestControllerAdvice({"com.yqhp.agent.web.controller"})
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        if (e instanceof AccessDeniedException) {
            throw (AccessDeniedException) e;
        }

        log.error("unexpected exception", e);

        return new Response<>(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
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
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Response handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
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

    @ExceptionHandler(FeignException.class)
    public void handleFeignException(FeignException feignException, HttpServletResponse response) throws IOException {
        response.setStatus(feignException.status());
        Collection<String> contentTypeValues = feignException.responseHeaders().get("content-type");
        if (!CollectionUtils.isEmpty(contentTypeValues)) {
            String contentType = contentTypeValues.iterator().next();
            response.setContentType(contentType);
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(feignException.contentUTF8());
    }

    /*************************************************************************************************/

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServiceException.class)
    public Response handleServiceException(ServiceException e) {
        ResponseCode responseCode = e.getResponseCode();
        return new Response<>(responseCode);
    }

}
