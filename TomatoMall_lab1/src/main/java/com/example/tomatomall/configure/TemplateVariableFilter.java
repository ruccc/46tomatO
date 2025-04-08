package com.example.tomatomall.configure;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TemplateVariableFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request.getRequestURI().contains("{{") || request.getQueryString() != null && request.getQueryString().contains("{{")) {
            String fixedUri = request.getRequestURI().replaceAll("\\{\\{.*?}}", "1");
            request.getRequestDispatcher(fixedUri).forward(request, response);
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            String body = new String(cachedRequest.getBody());
            if (body.contains("{{")) {
                body = body.replaceAll("\\{\\{.*?}}", "1");
                cachedRequest.setBody(body.getBytes());
            }
            chain.doFilter(cachedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}