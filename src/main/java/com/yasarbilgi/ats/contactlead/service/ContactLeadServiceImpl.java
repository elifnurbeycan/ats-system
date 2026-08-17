package com.yasarbilgi.ats.contactlead.service;
import com.yasarbilgi.ats.candidateprocess.dto.request.CreateCandidateProcessRequestDto;
import com.yasarbilgi.ats.candidateprocess.service.CandidateProcessService;
import com.yasarbilgi.ats.common.contract.ApplicationContract;
import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.contactlead.dto.*;
import com.yasarbilgi.ats.contactlead.entity.*;
import com.yasarbilgi.ats.contactlead.repository.ContactLeadRepository;
import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import com.yasarbilgi.ats.pipeline.repository.RecruitmentPipelineRepository;
import com.yasarbilgi.ats.position.entity.*;
import com.yasarbilgi.ats.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ContactLeadServiceImpl implements ContactLeadService {
 private final CompanyRepository companyRepository; private final PositionRepository positionRepository; private final RecruitmentPipelineRepository pipelineRepository; private final ContactLeadRepository repository; private final CandidateProcessService candidateProcessService;
 @Override @Transactional public ContactLeadResponse create(Long companyId, CreateContactLeadRequest request) {
  Company company=companyRepository.findById(companyId).filter(Company::isActive).orElseThrow(()->new ResourceNotFoundException("Şirket bulunamadı."));
  Position position=positionRepository.findByCompanyIdAndId(companyId,request.positionId()).filter(Position::isActive).filter(p->p.getStatus()==PositionStatus.OPEN).orElseThrow(()->new BusinessRuleException("Yalnızca açık pozisyonlar için iletişim kaydı oluşturulabilir."));
  RecruitmentPipeline pipeline=pipelineRepository.findByCompanyIdAndId(companyId,request.pipelineId()).filter(RecruitmentPipeline::isActive).orElseThrow(()->new ResourceNotFoundException("Pipeline bulunamadı."));
  String linkedin=normalize(request.linkedinUrl());
  if(linkedin!=null&&repository.existsByCompanyIdAndLinkedinUrlAndPositionIdAndStatusAndActiveTrue(companyId,linkedin,position.getId(),ContactLeadStatus.CONTACTING)) throw new BusinessRuleException("Bu kişi aynı pozisyon için zaten iletişim havuzunda.");
  return map(repository.save(ContactLead.builder().company(company).firstName(request.firstName().trim()).lastName(request.lastName().trim()).linkedinUrl(linkedin).position(position).pipeline(pipeline).status(ContactLeadStatus.CONTACTING).build()));
 }
 @Override public PageResponse<ContactLeadResponse> getAll(Long companyId,String search,ContactLeadStatus status,ContactRejectionReason rejectionReason,int page,int size){ if(page<0||size<1||size>ApplicationContract.MAX_PAGE_SIZE)throw new BusinessRuleException("Geçersiz sayfalama bilgisi."); return PageResponse.from(repository.search(companyId,status,rejectionReason,normalize(search),PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"))),this::map); }
 @Override @Transactional public ContactLeadResponse resolve(Long companyId,Long leadId,ResolveContactLeadRequest request){
  ContactLead lead=repository.findByCompanyIdAndId(companyId,leadId).filter(ContactLead::isActive).orElseThrow(()->new ResourceNotFoundException("İletişim adayı bulunamadı."));
  if(lead.getStatus()!=ContactLeadStatus.CONTACTING)throw new BusinessRuleException("Bu iletişim kaydı daha önce sonuçlandırılmış."); String note=normalize(request.note());
  switch(request.resolution()){case WAITING->lead.markWaiting(request.channel(),note);case REJECTED->{if(request.rejectionReason()==null)throw new BusinessRuleException("Ret nedeni zorunludur.");lead.reject(request.channel(),request.rejectionReason(),note);}case POSITIVE->{var process=candidateProcessService.create(companyId,new CreateCandidateProcessRequestDto(lead.getFirstName(),lead.getLastName(),lead.getLinkedinUrl(),lead.getPosition().getId(),lead.getPipeline().getId()));lead.convert(request.channel(),note,process.id());}}
  return map(lead);
 }
 private ContactLeadResponse map(ContactLead l){return new ContactLeadResponse(l.getId(),l.getFirstName(),l.getLastName(),l.getFirstName()+" "+l.getLastName(),l.getLinkedinUrl(),l.getPosition().getId(),l.getPosition().getTitle(),l.getPosition().getDepartment().getId(),l.getPosition().getDepartment().getName(),l.getPipeline().getId(),l.getPipeline().getName(),l.getStatus(),l.getContactChannel(),l.getRejectionReason(),l.getNote(),l.getCandidateProcessId(),l.getResolvedAt(),l.getCreatedAt(),l.getUpdatedAt());}
 private String normalize(String v){return v==null||v.isBlank()?null:v.trim();}
}
