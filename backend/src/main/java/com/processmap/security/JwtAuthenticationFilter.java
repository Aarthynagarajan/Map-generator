package com.processmap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Execute the filter for async dispatches (required for SSE).
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        log.info(
                "JWT FILTER | Dispatcher={} | URI={}",
                request.getDispatcherType(),
                request.getRequestURI()
        );

        try {

            String token = parseBearerToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {

                UUID userId = jwtTokenProvider.extractUserId(token);
                String role = jwtTokenProvider.extractRole(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + role)
                                )
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info(
                        "Authenticated user {} during {} dispatch",
                        userId,
                        request.getDispatcherType()
                );

            } else {

                log.warn(
                        "No valid JWT token found for {} {}",
                        request.getMethod(),
                        request.getRequestURI()
                );
            }

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

            log.error("JWT authentication failed", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }

        return null;
    }
}