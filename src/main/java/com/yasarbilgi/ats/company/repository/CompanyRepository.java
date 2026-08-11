package com.yasarbilgi.ats.company.repository;

import com.yasarbilgi.ats.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Şirketi benzersiz şirket koduna göre getirir.
    Optional<Company> findByCode(String code);

    // Verilen şirket kodunun daha önce kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCode(String code);

    // Platform yönetimi için şirketleri adlarına göre getirir.
    List<Company> findAllByOrderByNameAsc();

    Page<Company> findAll(Pageable pageable);
}
