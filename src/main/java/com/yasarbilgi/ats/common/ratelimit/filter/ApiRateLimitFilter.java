package com.yasarbilgi.ats.common.ratelimit.filter;

import com.yasarbilgi.ats.common.exception.TooManyRequestsException;
import com.yasarbilgi.ats.common.ratelimit.service.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final long MINUTE = 60_000L;
    private final Map<String, WindowState> windows = new ConcurrentHashMap<>();
    private final ClientIpResolver clientIpResolver;
    private final HandlerExceptionResolver exceptionResolver;
    private final Clock clock;
    private final boolean enabled;
    private final int resetPasswordLimit;
    private final int userCreationLimit;
    private final int companyCreationLimit;
    private final int cvUploadLimit;
    private final int exportLimit;
    private final int writeLimit;
    private final int readLimit;

    @Autowired
    public ApiRateLimitFilter(
            ClientIpResolver clientIpResolver,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            @Value("${security.rate-limit.enabled:true}") boolean enabled,
            @Value("${security.rate-limit.reset-password-per-15-minutes:5}") int resetPasswordLimit,
            @Value("${security.rate-limit.user-creation-per-hour:20}") int userCreationLimit,
            @Value("${security.rate-limit.company-creation-per-hour:10}") int companyCreationLimit,
            @Value("${security.rate-limit.cv-upload-per-10-minutes:20}") int cvUploadLimit,
            @Value("${security.rate-limit.export-per-minute:20}") int exportLimit,
            @Value("${security.rate-limit.write-per-minute:120}") int writeLimit,
            @Value("${security.rate-limit.read-per-minute:600}") int readLimit) {
        this(clientIpResolver, exceptionResolver, Clock.systemUTC(), enabled, resetPasswordLimit,
                userCreationLimit, companyCreationLimit, cvUploadLimit, exportLimit, writeLimit, readLimit);
    }

    ApiRateLimitFilter(ClientIpResolver clientIpResolver, HandlerExceptionResolver exceptionResolver, Clock clock,
                       boolean enabled, int resetPasswordLimit, int userCreationLimit, int companyCreationLimit,
                       int cvUploadLimit, int exportLimit, int writeLimit, int readLimit) {
        this.clientIpResolver = clientIpResolver;
        this.exceptionResolver = exceptionResolver;
        this.clock = clock;
        this.enabled = enabled;
        this.resetPasswordLimit = positive(resetPasswordLimit);
        this.userCreationLimit = positive(userCreationLimit);
        this.companyCreationLimit = positive(companyCreationLimit);
        this.cvUploadLimit = positive(cvUploadLimit);
        this.exportLimit = positive(exportLimit);
        this.writeLimit = positive(writeLimit);
        this.readLimit = positive(readLimit);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LimitRule rule = ruleFor(request);
        String key = rule.name() + ':' + callerKey(request);
        long now = clock.millis();
        WindowState state = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAtMillis() >= rule.windowMillis()) {
                return new WindowState(now, 1);
            }
            return new WindowState(current.startedAtMillis(), current.requestCount() + 1);
        });

        if (state.requestCount() > rule.maximumRequests()) {
            long retryAfter = Math.max(1,
                    (state.startedAtMillis() + rule.windowMillis() - now + 999) / 1000);
            exceptionResolver.resolveException(request, response, null,
                    new TooManyRequestsException(
                            "Çok fazla istek gönderildi. Lütfen daha sonra tekrar deneyin.", retryAfter));
            return;
        }

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAtMillis() >= 60 * MINUTE);
        }
        filterChain.doFilter(request, response);
    }

    private LimitRule ruleFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && path.endsWith("/reset-password")) {
            return new LimitRule("reset-password", resetPasswordLimit, 15 * MINUTE);
        }
        if ("POST".equals(method) && path.equals("/api/v1/platform/companies")) {
            return new LimitRule("company-create", companyCreationLimit, 60 * MINUTE);
        }
        if ("POST".equals(method) && path.matches("/api/v1/companies/[^/]+/users/?")) {
            return new LimitRule("user-create", userCreationLimit, 60 * MINUTE);
        }
        if ("POST".equals(method) && path.matches("/api/v1/companies/[^/]+/candidates/[^/]+/cv/?")) {
            return new LimitRule("cv-upload", cvUploadLimit, 10 * MINUTE);
        }
        if ("GET".equals(method) && path.toLowerCase().contains("export")) {
            return new LimitRule("export", exportLimit, MINUTE);
        }
        if ("GET".equals(method)) {
            return new LimitRule("read", readLimit, MINUTE);
        }
        return new LimitRule("write", writeLimit, MINUTE);
    }

    private String callerKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null && !authentication.getName().isBlank()) {
            return "principal:" + authentication.getName();
        }
        return "ip:" + clientIpResolver.resolve(request);
    }

    private static int positive(int value) {
        if (value < 1) throw new IllegalArgumentException("Rate limit değerleri en az 1 olmalıdır.");
        return value;
    }

    private record LimitRule(String name, int maximumRequests, long windowMillis) {}
    private record WindowState(long startedAtMillis, int requestCount) {}
}
