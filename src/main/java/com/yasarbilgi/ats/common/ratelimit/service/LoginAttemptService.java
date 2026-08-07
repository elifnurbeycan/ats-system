package com.yasarbilgi.ats.common.ratelimit.service;

import com.yasarbilgi.ats.common.exception.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {
    private static final String PREFIX = "ats:ratelimit:login:";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]); "
                    + "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; "
                    + "return count;", Long.class);

    private final StringRedisTemplate redisTemplate;
    private final int ipFailureLimit;
    private final long ipWindowSeconds;
    private final int accountFailureLimit;
    private final long accountWindowSeconds;

    public LoginAttemptService(
            StringRedisTemplate redisTemplate,
            @Value("${security.login-protection.ip-failure-limit:10}") int ipFailureLimit,
            @Value("${security.login-protection.ip-window-seconds:300}") long ipWindowSeconds,
            @Value("${security.login-protection.account-failure-limit:5}") int accountFailureLimit,
            @Value("${security.login-protection.account-window-seconds:900}") long accountWindowSeconds) {
        this.redisTemplate = redisTemplate;
        this.ipFailureLimit = positive(ipFailureLimit, "IP başarısız deneme limiti");
        this.ipWindowSeconds = positive(ipWindowSeconds, "IP zaman penceresi");
        this.accountFailureLimit = positive(accountFailureLimit, "Hesap başarısız deneme limiti");
        this.accountWindowSeconds = positive(accountWindowSeconds, "Hesap zaman penceresi");
    }

    public void checkAllowed(String ipAddress, String credentialKey, String endpoint) {
        try {
            checkKey(ipKey(ipAddress, endpoint), ipFailureLimit,
                    "Çok fazla başarısız giriş denemesi. Lütfen daha sonra tekrar deneyin.");
            checkKey(accountKey(credentialKey), accountFailureLimit,
                    "Bu hesap için çok fazla başarısız giriş denemesi. Lütfen daha sonra tekrar deneyin.");
        } catch (DataAccessException exception) {
            log.warn("Redis kullanılamadığı için giriş rate-limit kontrolü fail-open çalıştı: {}",
                    exception.getMessage());
        }
    }

    public void recordFailure(String ipAddress, String credentialKey, String endpoint) {
        try {
            increment(ipKey(ipAddress, endpoint), ipWindowSeconds);
            increment(accountKey(credentialKey), accountWindowSeconds);
        } catch (DataAccessException exception) {
            log.warn("Başarısız giriş denemesi Redis'e kaydedilemedi: {}", exception.getMessage());
        }
    }

    public void recordSuccess(String credentialKey) {
        try {
            redisTemplate.delete(accountKey(credentialKey));
        } catch (DataAccessException exception) {
            log.warn("Başarılı giriş sonrası hesap sayacı Redis'ten temizlenemedi: {}", exception.getMessage());
        }
    }

    private void checkKey(String key, int limit, String message) {
        String rawValue = redisTemplate.opsForValue().get(key);
        if (rawValue == null) return;
        long attempts;
        try {
            attempts = Long.parseLong(rawValue);
        } catch (NumberFormatException exception) {
            log.warn("Geçersiz rate-limit sayacı temizlendi: key={}", key);
            redisTemplate.delete(key);
            return;
        }
        if (attempts < limit) return;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        throw new TooManyRequestsException(message, ttl == null || ttl < 1 ? 1 : ttl);
    }

    private void increment(String key, long ttlSeconds) {
        redisTemplate.execute(INCREMENT_WITH_TTL, List.of(key), String.valueOf(ttlSeconds));
    }

    private String ipKey(String ipAddress, String endpoint) {
        return PREFIX + "ip:" + digest(normalize(ipAddress)) + ":" + digest(endpoint);
    }

    private String accountKey(String credentialKey) {
        return PREFIX + "account:" + digest(normalize(credentialKey));
    }

    private String normalize(String value) {
        return value == null ? "unknown" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalize(value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algoritması kullanılamıyor.", exception);
        }
    }

    private int positive(int value, String name) {
        if (value < 1) throw new IllegalArgumentException(name + " pozitif olmalıdır.");
        return value;
    }

    private long positive(long value, String name) {
        if (value < 1) throw new IllegalArgumentException(name + " pozitif olmalıdır.");
        return value;
    }
}
