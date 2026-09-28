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
class CandidateEndpointAuthenticationTests extends EndpointAuthenticationTestSupport {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} anonim erişimi reddeder")
    @MethodSource("protectedEndpoints")
    void shouldRequireAuthentication(HttpMethod method, String path) throws Exception {
        mockMvc.perform(request(method, path)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/candidates/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/candidates/1/deactivate"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/candidates/1/activate"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/activities"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidates/1/interactions"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/interactions"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/candidates/1/interactions/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/candidates/1/interactions/1/deactivate"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/follow-ups"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidates/1/follow-ups"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/candidates/1/follow-ups/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/candidates/1/follow-ups/1/status"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/notes"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidates/1/notes"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/notes/evaluations"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidates/1/notes/evaluations"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/cv"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidates/1/cv/download"),
                endpoint(HttpMethod.DELETE, "/api/v1/companies/1/candidates/1/cv"));
    }
}
