package com.yasarbilgi.ats.security;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/** Shared helpers for feature-scoped anonymous endpoint contracts. */
public abstract class EndpointAuthenticationTestSupport {
    protected static Arguments endpoint(HttpMethod method, String path) {
        return arguments(method, path);
    }
}
