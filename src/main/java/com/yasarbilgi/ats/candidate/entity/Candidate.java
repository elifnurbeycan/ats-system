package com.yasarbilgi.ats.candidate.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidates_company_linkedin_url",
                        columnNames = {"company_id", "linkedin_url"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate extends TenantBaseEntity {

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            name = "linkedin_url",
            length = 500
    )
    private String linkedinUrl;

    @Column(
            name = "email",
            length = 255
    )
    private String email;

    @Column(
            name = "phone",
            length = 30
    )
    private String phone;

    @Column(
            name = "city",
            length = 100
    )
    private String city;

    @Column(
            name = "current_company",
            length = 150
    )
    private String currentCompany;

    @Column(
            name = "current_job_title",
            length = 150
    )
    private String currentJobTitle;

    @Column(
            name = "current_salary",
            precision = 19,
            scale = 2
    )
    private BigDecimal currentSalary;

    @Column(
            name = "salary_currency",
            length = 3
    )
    private String salaryCurrency;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    public void updateIdentityInformation(
            String firstName,
            String lastName,
            String linkedinUrl
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.linkedinUrl = linkedinUrl;
    }

    public void updateContactInformation(
            String email,
            String phone,
            String city
    ) {
        this.email = email;
        this.phone = phone;
        this.city = city;
    }

    public void updateProfessionalInformation(
            String currentCompany,
            String currentJobTitle,
            BigDecimal currentSalary,
            String salaryCurrency,
            Integer noticePeriodDays
    ) {
        this.currentCompany = currentCompany;
        this.currentJobTitle = currentJobTitle;
        this.currentSalary = currentSalary;
        this.salaryCurrency = salaryCurrency;
        this.noticePeriodDays = noticePeriodDays;
    }
}