package com.appbasevaadin.mssecurity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private HttpServletRequest request;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(
                new SecurityErrorResponseWriter(new ObjectMapper().findAndRegisterModules()), new ClientIpResolver());
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        filterChain = mock(FilterChain.class);
    }

    @Test
    void allowsRequestsUpToTheLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
        verify(filterChain, org.mockito.Mockito.times(5))
                .doFilter(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsTheRequestAfterTheLimitIsExceeded() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse sixthResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, sixthResponse, filterChain);

        assertThat(sixthResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(sixthResponse.getContentAsString()).contains("RATE_LIMITED");
    }

    @Test
    void doesNotRateLimitOtherPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/refresh");

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, org.mockito.Mockito.times(10))
                .doFilter(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.any());
    }
}
