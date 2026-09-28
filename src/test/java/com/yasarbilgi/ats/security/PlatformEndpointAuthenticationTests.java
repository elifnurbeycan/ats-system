package com.yasarbilgi.ats.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformEndpointAuthenticationTests extends EndpointAuthenticationTestSupport {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} anonim erişimi reddeder")
    @MethodSource("protectedEndpoints")
    void shouldRequireAuthentication(HttpMethod method, String path) throws Exception {
        mockMvc.perform(request(method, path)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                endpoint(HttpMethod.GET, "/api/v1/auth/me"),
                endpoint(HttpMethod.GET, "/api/v1/auth/platform/me"),
                endpoint(HttpMethod.GET, "/api/v1/platform/companies"),
                endpoint(HttpMethod.POST, "/api/v1/platform/companies"),
                endpoint(HttpMethod.GET, "/api/v1/platform/companies/1"),
                endpoint(HttpMethod.PUT, "/api/v1/platform/companies/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/platform/companies/1/status"));
    }
}
