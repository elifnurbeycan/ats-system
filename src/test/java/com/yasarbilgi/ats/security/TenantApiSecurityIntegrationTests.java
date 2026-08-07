package com.yasarbilgi.ats.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))
                                .authorities(new SimpleGrantedAuthority("USER_VIEW"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Token ve URL şirketi eşleştiğinde isteğin güvenlik filtresini geçip controller'a ulaştığını doğrular.
    @Test
    void shouldAllowMatchingCompanyAccess() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/roles")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))
                                .authorities(new SimpleGrantedAuthority("USER_VIEW"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // Şirket doğru olsa bile gerekli permission bulunmayan isteğin reddedildiğini doğrular.
    @Test
    void shouldRejectRequestWithoutRequiredPermission() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/roles")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Departman kapsamındaki kullanıcının yönetmediği aday sürecine erişemediğini doğrular.
    @Test
    void shouldRejectCandidateProcessOutsideManagedDepartments() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/candidate-processes/99")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L)
                                        .claim("managedDepartmentIds", java.util.List.of()))
                                .authorities(new SimpleGrantedAuthority("ROLE_DEPARTMENT_MANAGER"),
                                        new SimpleGrantedAuthority("CANDIDATE_PROCESS_VIEW"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // Şirket kapsamındaki İK kullanıcısının departman filtresine takılmadığını doğrular.
    @Test
    void shouldAllowCompanyScopedRoleToReachCandidateProcess() throws Exception {
        mockMvc.perform(get("/api/v1/companies/1/candidate-processes/99")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))
                                .authorities(new SimpleGrantedAuthority("ROLE_HR"),
                                        new SimpleGrantedAuthority("CANDIDATE_PROCESS_VIEW"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowDepartmentDeactivateWithRequiredPermission() throws Exception {
        mockMvc.perform(patch("/api/v1/companies/1/departments/99/deactivate")
                        .with(jwt().jwt(token -> token.claim("companyId", 1L))
                                .authorities(new SimpleGrantedAuthority("ROLE_HR"),
                                        new SimpleGrantedAuthority("DEPARTMENT_DEACTIVATE"))))
                .andExpect(status().isNotFound());
    }
}
