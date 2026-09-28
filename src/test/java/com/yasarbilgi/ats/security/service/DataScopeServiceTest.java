package com.yasarbilgi.ats.security.service;

import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.common.exception.ForbiddenException;
import com.yasarbilgi.ats.department.entity.Department;
import com.yasarbilgi.ats.department.entity.DepartmentManagerAssignment;
import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
import com.yasarbilgi.ats.role.entity.DataScope;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.entity.UserStatus;
import com.yasarbilgi.ats.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataScopeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentManagerAssignmentRepository assignmentRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customDepartmentRoleUsesUsersOwnDepartment() {
        Company company = Company.builder().id(1L).name("Yaşar Bilgi").code("yasar-bilgi")
                .status(CompanyStatus.ACTIVE).build();
        Department javaDepartment = Department.builder().id(10L).company(company)
                .name("Java").code("JAVA").build();
        Role teamLead = Role.builder().id(20L).company(company).code("CUSTOM_TEAM_LEAD")
                .name("Ekip Lideri").dataScope(DataScope.DEPARTMENT).systemRole(false).build();
        User user = User.builder().id(30L).company(company).firstName("Java").lastName("Lideri")
                .email("javalider@yasar.com").keycloakUserId("keycloak-java-lead")
                .department(javaDepartment).status(UserStatus.ACTIVE).roles(Set.of(teamLead)).build();

        when(userRepository.findByKeycloakUserIdAndActiveTrue("keycloak-java-lead"))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findAllByCompanyIdAndUserIdAndActiveTrue(1L, 30L))
                .thenReturn(List.of());
        authenticate("keycloak-java-lead");

        DataScopeService service = new DataScopeService(userRepository, assignmentRepository);

        assertThat(service.hasCompanyScope()).isFalse();
        assertThat(service.hasDepartmentScope()).isTrue();
        assertThat(service.getManagedDepartmentIds()).containsExactly(10L);
        assertThat(service.getCurrentUserId()).isEqualTo(30L);
    }

    @Test
    void companyScopedRoleCanAccessWholeCompanyWithoutDepartmentIds() {
        authenticateWithAuthorities("company-admin", "ROLE_COMPANY_ADMIN");
        DataScopeService service = new DataScopeService(userRepository, assignmentRepository);

        assertThat(service.hasCompanyScope()).isTrue();
        assertThat(service.getManagedDepartmentIds()).isEmpty();
        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void departmentScopeIncludesOwnAndActiveAssignmentsOnly() {
        Company company = Company.builder().id(1L).name("Company").code("company")
                .status(CompanyStatus.ACTIVE).build();
        Department ownDepartment = Department.builder().id(10L).company(company)
                .name("Java").code("JAVA").build();
        Department assignedDepartment = Department.builder().id(11L).company(company)
                .name("Mali İşler").code("MALI").build();
        Department inactiveDepartment = Department.builder().id(12L).company(company)
                .name("Pasif").code("PASIF").build();
        inactiveDepartment.deactivate();
        Role manager = Role.builder().code("DEPARTMENT_MANAGER").name("Ekip lideri")
                .dataScope(DataScope.DEPARTMENT).build();
        User user = User.builder().id(30L).company(company).firstName("Department")
                .lastName("Manager").email("manager@example.test").keycloakUserId("department-manager")
                .department(ownDepartment).roles(Set.of(manager)).build();
        DepartmentManagerAssignment activeAssignment = DepartmentManagerAssignment.builder()
                .company(company).department(assignedDepartment).user(user).startedAt(Instant.now()).build();
        DepartmentManagerAssignment inactiveAssignment = DepartmentManagerAssignment.builder()
                .company(company).department(inactiveDepartment).user(user).startedAt(Instant.now()).build();

        when(userRepository.findByKeycloakUserIdAndActiveTrue("department-manager"))
                .thenReturn(Optional.of(user));
        when(assignmentRepository.findAllByCompanyIdAndUserIdAndActiveTrue(1L, 30L))
                .thenReturn(List.of(activeAssignment, inactiveAssignment));
        authenticate("department-manager");

        DataScopeService service = new DataScopeService(userRepository, assignmentRepository);

        assertThat(service.getManagedDepartmentIds()).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void unauthenticatedScopeAccessIsForbidden() {
        DataScopeService service = new DataScopeService(userRepository, assignmentRepository);

        assertThatThrownBy(service::hasCompanyScope).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(service::getManagedDepartmentIds).isInstanceOf(ForbiddenException.class);
    }

    private void authenticate(String subject) {
        Instant now = Instant.now();
        Jwt jwt = new Jwt("token", now, now.plusSeconds(300),
                java.util.Map.of("alg", "none"), java.util.Map.of("sub", subject));
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, java.util.List.of()));
    }

    private void authenticateWithAuthorities(String subject, String... authorities) {
        Instant now = Instant.now();
        Jwt jwt = new Jwt("token", now, now.plusSeconds(300),
                java.util.Map.of("alg", "none"), java.util.Map.of("sub", subject));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                java.util.Arrays.stream(authorities)
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList()));
    }
}
