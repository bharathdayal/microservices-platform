package com.example.order_service.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyStore store;

    public IdempotencyFilter(IdempotencyStore store){
        this.store=store;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!"POST".equals(request.getMethod())
                || !"/orders".equals(request.getRequestURI())) {

            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        store.findExisting(key).ifPresent(existing -> {
            try {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                    {"orderId":"%s"}
                """.formatted(existing.getId()));
                response.flushBuffer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (response.isCommitted()) {
            return;
        }


        filterChain.doFilter(request, response);
    }
}
