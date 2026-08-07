package com.yasarbilgi.ats.common.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = Math.max(retryAfterSeconds, 1);
    }
}
