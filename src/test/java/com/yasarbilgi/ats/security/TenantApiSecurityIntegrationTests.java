package com.yasarbilgi.ats.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TenantApiSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // Şirket endpointlerinin oturum açılmadan çağrılamadığını doğrular.
    @Test
    void shouldRequireAuthenticationForCompanyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Token şirketinden farklı bir şirketin URL üzerinden çağrılamadığını doğrular.
    @Test
    void shouldRejectCrossCompanyAccess() throws Exception {
        mockMvc.perform(get("/api/v1/companies/2/roles")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Token ve URL şirketi eşleştiğinde isteğin güvenlik filtresini geçip controller'a ulaştığını doğrular.
    @Test
    void shouldAllowMatchingCompanyAccess() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/roles")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
