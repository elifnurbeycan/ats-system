package com.yasarbilgi.ats.contactlead.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import com.yasarbilgi.ats.position.entity.Position;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Getter @SuperBuilder @Entity @Table(name = "candidate_contact_leads")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactLead extends TenantBaseEntity {
    @Column(name="first_name", nullable=false, length=100) private String firstName;
    @Column(name="last_name", nullable=false, length=100) private String lastName;
    @Column(name="linkedin_url", length=500) private String linkedinUrl;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="position_id") private Position position;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="pipeline_id") private RecruitmentPipeline pipeline;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private ContactLeadStatus status;
    @Enumerated(EnumType.STRING) @Column(name="contact_channel", length=30) private InteractionChannel contactChannel;
    @Enumerated(EnumType.STRING) @Column(name="rejection_reason", length=50) private ContactRejectionReason rejectionReason;
    @Column(columnDefinition="TEXT") private String note;
    @Column(name="candidate_process_id") private Long candidateProcessId;
    @Column(name="resolved_at") private Instant resolvedAt;
    public void markWaiting(InteractionChannel channel, String note) { this.contactChannel=channel; this.note=note; }
    public void convert(InteractionChannel channel, String note, Long processId) { status=ContactLeadStatus.CONVERTED; contactChannel=channel; this.note=note; candidateProcessId=processId; rejectionReason=null; resolvedAt=Instant.now(); }
    public void reject(InteractionChannel channel, ContactRejectionReason reason, String note) { status=ContactLeadStatus.REJECTED; contactChannel=channel; rejectionReason=reason; this.note=note; resolvedAt=Instant.now(); }
}
