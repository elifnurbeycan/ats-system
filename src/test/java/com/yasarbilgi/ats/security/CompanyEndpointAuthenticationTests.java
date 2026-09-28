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
class CompanyEndpointAuthenticationTests extends EndpointAuthenticationTestSupport {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} anonim erişimi reddeder")
    @MethodSource("protectedEndpoints")
    void shouldRequireAuthentication(HttpMethod method, String path) throws Exception {
        mockMvc.perform(request(method, path)).andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                endpoint(HttpMethod.GET, "/api/v1/companies/1/dashboard"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/departments"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/departments"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/departments/1"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/departments/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/departments/1/deactivate"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/departments/1/activate"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/departments/1/managers"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/departments/1/managers"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/departments/1/managers/1/end"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/users"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/users"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/users/1"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/users/1"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/users/1/roles"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/users/1/deactivate"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/users/1/activate"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/users/1/reset-password"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/roles"),
                endpoint(HttpMethod.GET, "/api/v1/companies/1/roles/permissions"),
                endpoint(HttpMethod.POST, "/api/v1/companies/1/roles"),
                endpoint(HttpMethod.PUT, "/api/v1/companies/1/roles/1"),
                endpoint(HttpMethod.PATCH, "/api/v1/companies/1/roles/1/deactivate"));
    }
}
