package com.example.tomatomall.configure;

import com.example.tomatomall.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
class ResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getDeclaringClass().getPackage().getName()
                .startsWith("com.example.tomatomall.controller.product")
                && !returnType.getParameterType().equals(Result.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) {
            return Result.success();
        }

        if (body instanceof String && !String.class.equals(returnType.getParameterType())) {
            try {
                body = objectMapper.readValue((String) body, returnType.getParameterType());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert response body", e);
            }
        }

        return Result.success(body);
    }
}