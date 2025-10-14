package br.gov.mma.facial.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        logger.error("[AUTH-ENTRY] Unauthorized access attempt: {} {} | Auth header present: {} | Error: {}",
                    request.getMethod(), 
                    request.getRequestURI(),
                    authHeader != null && !authHeader.isEmpty(),
                    authException.getMessage());
        logger.debug("[AUTH-ENTRY] Full exception:", authException);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}