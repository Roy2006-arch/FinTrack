package com.fintrack.security;
import com.fintrack.exception.ApiError;
import com.fintrack.repository.AuthSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final AuthSessionRepository sessions;
    private final ObjectMapper mapper;
    public JwtAuthenticationFilter(JwtService jwt, AuthSessionRepository sessions, ObjectMapper mapper) { this.jwt = jwt; this.sessions = sessions; this.mapper = mapper; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                CurrentUser principal = jwt.parse(header.substring(7));
                if (!sessions.existsByIdAndUserIdAndExpiresAtAfter(principal.sessionId(), principal.id(), Instant.now())) throw new IllegalArgumentException("Session expired");
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext(); response.setStatus(401); response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(), new ApiError(401, "Session is invalid or expired. Please sign in again.", request.getRequestURI())); return;
            }
        }
        chain.doFilter(request, response);
    }
}
