package com.example.elhabashyback.configuration.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        writeProblem(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required or the access token is invalid.",
                "UNAUTHENTICATED"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {
        writeProblem(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource.",
                "FORBIDDEN"
        );
    }

    private void writeProblem(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String title,
            String detail,
            String errorCode
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("errorCode", errorCode);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
