package com.yasarbilgi.ats.security.filter;

import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.security.handler.SecurityErrorWriter;
import com.yasarbilgi.ats.security.service.DataScopeService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.regex.*;

@Component
@RequiredArgsConstructor
public class DepartmentDataScopeFilter extends OncePerRequestFilter {

    private static final Pattern PROCESS_PATH = Pattern.compile(
            "^/api/v1/companies/(\\d+)/candidate-processes/(\\d+)(?:/.*)?$");
    private static final Pattern CANDIDATE_PATH = Pattern.compile(
            "^/api/v1/companies/(\\d+)/candidates/(\\d+)(?:/.*)?$");

    private final DataScopeService dataScopeService;
    private final CandidateProcessRepository processRepository;
    private final SecurityErrorWriter errorWriter;

    // Departman yöneticisinin aday ve süreç alt kaynaklarına yalnızca kendi kapsamından erişmesini sağlar.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (dataScopeService.hasCompanyScope() || !dataScopeService.hasDepartmentScope()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI().substring(
                Math.min(request.getContextPath().length(), request.getRequestURI().length()));
        Set<Long> departmentIds = dataScopeService.getManagedDepartmentIds();
        Matcher processMatcher = PROCESS_PATH.matcher(path);
        if (processMatcher.matches() && !canAccessProcess(processMatcher, departmentIds)) {
            deny(response);
            return;
        }
        Matcher candidateMatcher = CANDIDATE_PATH.matcher(path);
        if (candidateMatcher.matches() && !canAccessCandidate(candidateMatcher, departmentIds)) {
            deny(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    // Sürecin yönetilen departmanlardan birinde bulunduğunu kontrol eder.
    private boolean canAccessProcess(Matcher matcher, Set<Long> departmentIds) {
        return !departmentIds.isEmpty() && processRepository
                .existsByCompanyIdAndIdAndPositionDepartmentIdInAndActiveTrue(
                        Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), departmentIds);
    }

    // Adayın yönetilen departmanlardan birinde aktif süreci bulunduğunu kontrol eder.
    private boolean canAccessCandidate(Matcher matcher, Set<Long> departmentIds) {
        return !departmentIds.isEmpty() && processRepository
                .existsByCompanyIdAndCandidateIdAndPositionDepartmentIdInAndActiveTrue(
                        Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), departmentIds);
    }

    // Kapsam dışı kaynak isteğine standart 403 yanıtı döner.
    private void deny(HttpServletResponse response) throws IOException {
        errorWriter.write(response, HttpStatus.FORBIDDEN.value(),
                "Bu departmana ait verilere erişim yetkiniz bulunmuyor.");
    }
}
