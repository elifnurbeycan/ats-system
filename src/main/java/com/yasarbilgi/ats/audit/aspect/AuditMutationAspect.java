package com.yasarbilgi.ats.audit.aspect;

import com.yasarbilgi.ats.audit.entity.AuditLog;
import com.yasarbilgi.ats.audit.service.AuditLogService;
import com.yasarbilgi.ats.audit.service.AuditPayloadSanitizer;
import com.yasarbilgi.ats.common.ratelimit.service.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditMutationAspect {
    private final AuditLogService auditLogService;
    private final AuditPayloadSanitizer sanitizer;
    private final HttpServletRequest request;
    private final ClientIpResolver clientIpResolver;

    @Around("within(com.yasarbilgi.ats..controller..*) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object auditSuccessfulMutation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            persist(joinPoint, result);
        } catch (RuntimeException exception) {
            log.error("Audit kaydi olusturulamadi: {} {}", request.getMethod(), request.getRequestURI(), exception);
        }
        return result;
    }

    private void persist(ProceedingJoinPoint joinPoint, Object result) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Map<String, Object> pathVariables = pathVariables(method, joinPoint.getArgs());
        Long companyId = numberValue(pathVariables.remove("companyId"));
        if (companyId == null) return;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long actorUserId = null;
        String actorReference = authentication == null ? "system" : authentication.getName();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            Number userId = jwt.getToken().getClaim("userId");
            actorUserId = userId == null ? null : userId.longValue();
            actorReference = jwt.getToken().getSubject();
        }

        int status = result instanceof ResponseEntity<?> response ? response.getStatusCode().value() : 200;
        Object responseBody = result instanceof ResponseEntity<?> response ? response.getBody() : result;
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();

        auditLogService.record(AuditLog.builder()
                .companyId(companyId).actorUserId(actorUserId)
                .actorReference(actorReference == null ? "unknown" : actorReference)
                .action(toAction(method.getName()))
                .resourceType(method.getDeclaringClass().getSimpleName().replace("Controller", ""))
                .resourceReference(resourceReference(pathVariables))
                .httpMethod(request.getMethod()).requestPath(request.getRequestURI())
                .requestData(sanitizer.serialize(requestBody(method, joinPoint.getArgs())))
                .responseData(sanitizer.serialize(responseBody))
                .ipAddress(clientIpResolver.resolve(request)).userAgent(limit(request.getHeader("User-Agent"), 500))
                .requestId(limit(requestId, 100)).httpStatus(status).occurredAt(Instant.now()).build());
    }

    private Map<String, Object> pathVariables(Method method, Object[] args) {
        Map<String, Object> values = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            PathVariable annotation = parameters[index].getAnnotation(PathVariable.class);
            if (annotation == null) continue;
            String name = !annotation.name().isBlank() ? annotation.name()
                    : !annotation.value().isBlank() ? annotation.value() : parameters[index].getName();
            values.put(name, args[index]);
        }
        return values;
    }

    private Object requestBody(Method method, Object[] args) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int index = 0; index < annotations.length; index++) {
            for (Annotation annotation : annotations[index]) {
                if (annotation instanceof RequestBody) return args[index];
            }
        }
        return null;
    }

    private String resourceReference(Map<String, Object> pathVariables) {
        if (pathVariables.isEmpty()) return null;
        StringJoiner joiner = new StringJoiner(",");
        pathVariables.forEach((key, value) -> joiner.add(key + "=" + value));
        return limit(joiner.toString(), 500);
    }

    private Long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private String toAction(String methodName) {
        return methodName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
    }

    private String limit(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
