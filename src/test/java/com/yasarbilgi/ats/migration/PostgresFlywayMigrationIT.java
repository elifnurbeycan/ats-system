package com.yasarbilgi.ats.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.open-in-view=false",
        "security.rate-limit.enabled=false"
})
class PostgresFlywayMigrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ats_migration_test")
            .withUsername("ats_test")
            .withPassword("ats_test_password");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allMigrationsAreAppliedAndHibernateSchemaValidationPasses() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("27");
        assertThat(flyway.info().pending()).isEmpty();

        Integer appliedMigrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success", Integer.class);
        assertThat(appliedMigrationCount).isEqualTo(27);
    }

    @Test
    void finalSchemaContainsExpectedTablesColumnsAndConstraints() {
        assertThat(existingTables("companies", "users", "candidates", "candidate_processes",
                "candidate_cvs", "candidate_contact_leads", "audit_logs", "platform_admins"))
                .containsExactlyInAnyOrder("companies", "users", "candidates", "candidate_processes",
                        "candidate_cvs", "candidate_contact_leads", "audit_logs", "platform_admins");

        assertThat(columnExists("candidate_processes", "current_salary")).isTrue();
        assertThat(columnExists("candidate_processes", "expected_salary")).isTrue();
        assertThat(columnExists("candidate_processes", "offered_salary")).isTrue();
        assertThat(columnExists("candidates", "current_salary")).isFalse();
        assertThat(columnExists("candidates", "salary_currency")).isFalse();
        assertThat(columnExists("users", "password_hash")).isFalse();
        assertThat(columnExists("platform_admins", "password_hash")).isFalse();

        assertThat(constraintExists("uk_companies_code")).isTrue();
        assertThat(constraintExists("uk_candidate_cvs_candidate")).isTrue();
        assertThat(constraintExists("ck_candidate_cvs_file_size")).isTrue();
    }

    @Test
    void dataMigrationsAndSeedDataReachExpectedFinalState() {
        Boolean candidateCreateActive = jdbcTemplate.queryForObject(
                "SELECT active FROM permissions WHERE code = 'CANDIDATE_CREATE'", Boolean.class);
        Boolean contactLeadUpdateActive = jdbcTemplate.queryForObject(
                "SELECT active FROM permissions WHERE code = 'CONTACT_LEAD_UPDATE'", Boolean.class);
        assertThat(candidateCreateActive).isFalse();
        assertThat(contactLeadUpdateActive).isFalse();

        Boolean initialContactStageActive = jdbcTemplate.queryForObject(
                "SELECT active FROM pipeline_stages WHERE code = 'WAITING_RESPONSE'", Boolean.class);
        Integer firstActiveStageOrder = jdbcTemplate.queryForObject(
                "SELECT display_order FROM pipeline_stages " +
                        "WHERE code = 'INFORMATION_COLLECTION' AND active = TRUE", Integer.class);
        assertThat(initialContactStageActive).isFalse();
        assertThat(firstActiveStageOrder).isEqualTo(1);

        Integer companyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE code = 'YASAR_BILGI' AND active = TRUE", Integer.class);
        assertThat(companyCount).isEqualTo(1);
    }

    private List<String> existingTables(String... tableNames) {
        String placeholders = String.join(",", java.util.Collections.nCopies(tableNames.length, "?"));
        return jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name IN (" + placeholders + ")",
                String.class, (Object[]) tableNames);
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
                Integer.class, tableName, columnName);
        return count != null && count == 1;
    }

    private boolean constraintExists(String constraintName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_constraint WHERE conname = ?", Integer.class, constraintName);
        return count != null && count == 1;
    }
}
