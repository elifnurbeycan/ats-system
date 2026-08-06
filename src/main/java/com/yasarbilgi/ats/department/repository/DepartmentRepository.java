package com.yasarbilgi.ats.department.repository;

import com.yasarbilgi.ats.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Şirkete ait departmanı benzersiz departman koduna göre getirir.
    Optional<Department> findByCompanyIdAndCode(Long companyId, String code);

    // Şirkete ait departmanı kimliğine göre getirir.
    Optional<Department> findByCompanyIdAndId(Long companyId, Long departmentId);

    // Şirket içerisinde verilen departman kodunun kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCompanyIdAndCode(Long companyId, String code);

    // Şirkete ait aktif departmanları ada göre sıralayarak getirir.
    List<Department> findAllByCompanyIdAndActiveTrueOrderByNameAsc(Long companyId);

    // Şirkete ait aktif ve pasif tüm departmanları ada göre sıralayarak getirir.
    List<Department> findAllByCompanyIdOrderByNameAsc(Long companyId);
}
