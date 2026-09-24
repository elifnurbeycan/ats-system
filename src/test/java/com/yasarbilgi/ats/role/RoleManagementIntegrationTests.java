package com.yasarbilgi.ats.role;

import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.permission.entity.PermissionCategory;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.permission.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoleManagementIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired CompanyRepository companyRepository;
    @Autowired PermissionRepository permissionRepository;

    private Long companyId;

    @BeforeEach
    void setUp() {
        Company company = companyRepository.save(Company.builder()
                .name("Dynamic Role Test").code("dynamic-role-test-" + System.nanoTime())
                .status(CompanyStatus.ACTIVE).build());
        companyId = company.getId();
        permissionRepository.save(Permission.builder().code(PermissionCode.CANDIDATE_VIEW)
                .name("Aday görüntüleme").category(PermissionCategory.CANDIDATE)
                .systemPermission(true).displayOrder(1).build());
        permissionRepository.save(Permission.builder().code(PermissionCode.CANDIDATE_CREATE)
                .name("Aday oluşturma").category(PermissionCategory.CANDIDATE)
                .systemPermission(true).displayOrder(2).build());
    }

    @Test
    void companyAdminCanCreateUpdateAndDeactivateCustomRole() throws Exception {
        String createBody = """
                {"name":"İşe Alım Uzmanı","description":"Dinamik rol","dataScope":"COMPANY",
                 "permissions":["CANDIDATE_VIEW","CANDIDATE_CREATE"]}
                """;
        String response = mockMvc.perform(post("/api/v1/companies/{companyId}/roles", companyId)
                        .with(jwt().jwt(token -> token.claim("companyId", companyId))
                                .authorities(new SimpleGrantedAuthority("ROLE_COMPANY_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("İşe Alım Uzmanı"))
                .andExpect(jsonPath("$.data.systemRole").value(false))
                .andExpect(jsonPath("$.data.permissions.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        long roleId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).path("data").path("id").asLong();

        String updateBody = """
                {"name":"Kıdemli İşe Alım Uzmanı","description":"Güncellendi","dataScope":"DEPARTMENT",
                 "permissions":["CANDIDATE_VIEW"]}
                """;
        mockMvc.perform(put("/api/v1/companies/{companyId}/roles/{roleId}", companyId, roleId)
                        .with(jwt().jwt(token -> token.claim("companyId", companyId))
                                .authorities(new SimpleGrantedAuthority("ROLE_COMPANY_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Kıdemli İşe Alım Uzmanı"))
                .andExpect(jsonPath("$.data.dataScope").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data.permissions.length()").value(1));

        mockMvc.perform(patch("/api/v1/companies/{companyId}/roles/{roleId}/deactivate", companyId, roleId)
                        .with(jwt().jwt(token -> token.claim("companyId", companyId))
                                .authorities(new SimpleGrantedAuthority("ROLE_COMPANY_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void normalUserCannotCreateRole() throws Exception {
        mockMvc.perform(post("/api/v1/companies/{companyId}/roles", companyId)
                        .with(jwt().jwt(token -> token.claim("companyId", companyId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Yetkisiz\",\"dataScope\":\"COMPANY\",\"permissions\":[\"CANDIDATE_VIEW\"]}"))
                .andExpect(status().isForbidden());
    }
}
