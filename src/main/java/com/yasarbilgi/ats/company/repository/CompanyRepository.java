package com.yasarbilgi.ats.company.repository;

import com.yasarbilgi.ats.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Şirketi benzersiz şirket koduna göre getirir.
    Optional<Company> findByCode(String code);

    // Verilen şirket kodunun daha önce kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCode(String code);
}