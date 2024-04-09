package com.eum.haetsal.chat.domain.base;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseResponseEntity<T> extends ResponseEntity<Map<String, Object>> {

    public BaseResponseEntity(HttpStatus status, T result) {
        super(createBody(status, result), status);
    }

    public BaseResponseEntity(HttpStatus status) {
        super(createBody(status, null), status);
    }

    public BaseResponseEntity(Throwable e) {
        super(createErrorBody(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static <T> Map<String, Object> createBody(HttpStatus status, T result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", status.is2xxSuccessful());
        body.put("code", status.value());
        body.put("message", status.getReasonPhrase());
        body.put("result", result);
        return body;
    }

    private static Map<String, Object> createErrorBody(Throwable e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("result", e.getMessage());
        return body;
    }
}
