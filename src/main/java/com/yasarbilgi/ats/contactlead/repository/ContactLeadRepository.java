package com.yasarbilgi.ats.contactlead.repository;
import com.yasarbilgi.ats.contactlead.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ContactLeadRepository extends JpaRepository<ContactLead, Long> {
 @Query(value="SELECT lead FROM ContactLead lead JOIN FETCH lead.position position JOIN FETCH position.department JOIN FETCH lead.pipeline WHERE lead.company.id=:companyId AND lead.active=true AND (:status IS NULL OR lead.status=:status) AND (:rejectionReason IS NULL OR lead.rejectionReason=:rejectionReason) AND (CAST(:search AS string) IS NULL OR LOWER(CONCAT(lead.firstName, ' ', lead.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(lead.linkedinUrl) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(position.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))", countQuery="SELECT COUNT(lead) FROM ContactLead lead JOIN lead.position position WHERE lead.company.id=:companyId AND lead.active=true AND (:status IS NULL OR lead.status=:status) AND (:rejectionReason IS NULL OR lead.rejectionReason=:rejectionReason) AND (CAST(:search AS string) IS NULL OR LOWER(CONCAT(lead.firstName, ' ', lead.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(lead.linkedinUrl) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(position.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
 Page<ContactLead> search(@Param("companyId") Long companyId, @Param("status") ContactLeadStatus status, @Param("rejectionReason") ContactRejectionReason rejectionReason, @Param("search") String search, Pageable pageable);
 Optional<ContactLead> findByCompanyIdAndId(Long companyId, Long id);
 boolean existsByCompanyIdAndLinkedinUrlAndPositionIdAndStatusAndActiveTrue(Long companyId, String linkedinUrl, Long positionId, ContactLeadStatus status);
}
