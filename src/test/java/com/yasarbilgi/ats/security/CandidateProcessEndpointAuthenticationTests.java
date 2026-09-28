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
class CandidateProcessEndpointAuthenticationTests extends EndpointAuthenticationTestSupport {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} anonim erişimi reddeder")
    @MethodSource("protectedEndpoints")
    void shouldRequireAuthentication(HttpMethod method, String path) throws Exception {
        mockMvc.perform(request(method, path)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidate-processes"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidate-processes/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/candidate-processes/1/stage"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidate-processes/1/compensation"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/candidate-processes/1/compensation"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidate-processes/1/stage-history"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/pipelines/1/positions/1/board"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/candidate-processes/1/interviews"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/candidate-processes/1/interviews"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/audit-logs"));
    }
}
