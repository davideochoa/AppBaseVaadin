package com.vaadinbaseapp.appvaadin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Mirrors the security headers already enforced by the 3 REST microservices'
 * SecurityConfig on this module's own browser-facing surface, which had none.
 * CSP is deliberately not locked down to default-src 'self' like the REST
 * APIs: Vaadin's dev-mode client bundle relies on eval()/inline bootstrap
 * scripts (a strict CSP needs nonce-based IndexHtmlRequestListener wiring and
 * only works in production mode per Vaadin's own docs), and LoginView loads
 * Google Identity Services from an external origin. Only the directives that
 * don't depend on that work are set.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "frame-ancestors 'self'; object-src 'none'; base-uri 'self'");
        filterChain.doFilter(request, response);
    }
}
