package com.yasarbilgi.ats.security;

import com.yasarbilgi.ats.common.exception.TooManyRequestsException;
import com.yasarbilgi.ats.common.ratelimit.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTests {

    @Test
    void blocksAccountAtConfiguredFailureLimitAndReturnsRedisTtl() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null, "5");
        when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(420L);
        LoginAttemptService service = new LoginAttemptService(redis, 10, 300, 5, 900);

        TooManyRequestsException exception = assertThrows(TooManyRequestsException.class,
                () -> service.checkAllowed("10.0.0.1", "acme:user@example.com", "/api/v1/auth/login"));

        assertEquals(420L, exception.getRetryAfterSeconds());
    }

    @Test
    void redisFailureDoesNotDisableLogin() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RedisConnectionFailureException("offline"));
        LoginAttemptService service = new LoginAttemptService(redis, 10, 300, 5, 900);

        assertDoesNotThrow(() ->
                service.checkAllowed("10.0.0.1", "acme:user@example.com", "/api/v1/auth/login"));
    }

    @Test
    void successfulLoginClearsOnlyTheAccountCounter() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        LoginAttemptService service = new LoginAttemptService(redis, 10, 300, 5, 900);

        service.recordSuccess("acme:user@example.com");

        verify(redis).delete(anyString());
        verifyNoMoreInteractions(redis);
    }
}
