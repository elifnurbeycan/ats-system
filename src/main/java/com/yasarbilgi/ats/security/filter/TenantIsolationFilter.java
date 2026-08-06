package com.yasarbilgi.ats.security.filter;

import com.yasarbilgi.ats.security.handler.SecurityErrorWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.*;

@Component
@RequiredArgsConstructor
public class TenantIsolationFilter extends OncePerRequestFilter {

    private static final Pattern COMPANY_PATH =
            Pattern.compile("^/api/v1/companies/(\\d+)(?:/.*)?$");

    private final SecurityErrorWriter errorWriter;

    // Token şirketi ile URL'deki şirketi karşılaştırarak tenant geçişini engeller.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Matcher matcher = COMPANY_PATH.matcher(request.getRequestURI().substring(
                Math.min(request.getContextPath().length(), request.getRequestURI().length())));
        if (!matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Number tokenCompanyId = jwtAuthentication.getToken().getClaim("companyId");
        long requestedCompanyId = Long.parseLong(matcher.group(1));
        if (tokenCompanyId == null || tokenCompanyId.longValue() != requestedCompanyId) {
            errorWriter.write(response, HttpStatus.FORBIDDEN.value(),
                    "Başka bir şirkete ait verilere erişemezsiniz.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
