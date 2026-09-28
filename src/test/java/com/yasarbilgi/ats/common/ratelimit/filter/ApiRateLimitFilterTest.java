package com.yasarbilgi.ats.common.ratelimit.filter;

import com.yasarbilgi.ats.common.exception.TooManyRequestsException;
import com.yasarbilgi.ats.common.ratelimit.service.ClientIpResolver;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class ApiRateLimitFilterTest {

    private final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    private final ClientIpResolver ipResolver = new ClientIpResolver(false);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-28T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void sixthPasswordResetWithinWindowIsRejected() throws Exception {
        ApiRateLimitFilter filter = filter();
        MockHttpServletRequest request = request("POST",
                "/api/v1/companies/1/users/2/reset-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 6; i++) filter.doFilter(request, response, chain);

        verify(chain, times(5)).doFilter(request, response);
        verify(resolver).resolveException(eq(request), eq(response), isNull(),
                any(TooManyRequestsException.class));
    }

    @Test
    void differentCallersHaveIndependentLimits() throws Exception {
        ApiRateLimitFilter filter = filter();
        MockHttpServletRequest first = request("POST", "/api/v1/companies/1/users/2/reset-password");
        MockHttpServletRequest second = request("POST", "/api/v1/companies/1/users/3/reset-password");
        first.setRemoteAddr("192.0.2.10");
        second.setRemoteAddr("192.0.2.11");
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) filter.doFilter(first, new MockHttpServletResponse(), chain);
        filter.doFilter(second, new MockHttpServletResponse(), chain);

        verify(chain, times(6)).doFilter(any(), any());
        verifyNoInteractions(resolver);
    }

    @Test
    void optionsRequestsAreNeverLimited() throws Exception {
        ApiRateLimitFilter filter = filter();
        MockHttpServletRequest request = request("OPTIONS", "/api/v1/companies/1/users");
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain, times(10)).doFilter(any(), any());
        verifyNoInteractions(resolver);
    }

    private ApiRateLimitFilter filter() {
        return new ApiRateLimitFilter(ipResolver, resolver, clock, true,
                5, 20, 10, 20, 20, 120, 600);
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        request.setRemoteAddr("192.0.2.1");
        return request;
    }
}
