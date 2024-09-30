package com.oclock.event_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oclock.event_backend.dto.ErrorResponse;
import com.oclock.event_backend.util.APIsErrorCodesConstants;
import com.oclock.event_backend.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractJwtFromRequest(request);

            if (token != null) {
                String email = extractEmailFromToken(token, response);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    authenticateUser(email, token, request, response);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, "JWT_EXPIRED", String.format(APIsErrorCodesConstants.JWT_EXPIRED, e.getMessage()));
            return;
        } catch (Exception e) {
            handleException(response, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", APIsErrorCodesConstants.INTERNAL_SERVER_ERROR);
            return;
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private String extractEmailFromToken(String token, HttpServletResponse response) throws IOException {
        try {
            return jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            handleException(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", String.format(APIsErrorCodesConstants.INVALID_TOKEN, e.getMessage()));
            return null;
        }
    }

    private void authenticateUser(String email, String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

        if (jwtUtil.validateToken(token, userDetails)) {
            if (!userDetails.isEnabled()) {
                handleException(
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "USER_DISABLED",
                        APIsErrorCodesConstants.USER_DISABLED
                );
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            handleException(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", String.format(APIsErrorCodesConstants.INVALID_TOKEN, "")
            );
            return;
        }
    }

    private void handleException(
            HttpServletResponse response, HttpStatus status, String errorCode, String message
    ) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status.value());
            response.setContentType("application/json");
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .errorCode(errorCode)
                    .message(message)
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
        }
    }
}
