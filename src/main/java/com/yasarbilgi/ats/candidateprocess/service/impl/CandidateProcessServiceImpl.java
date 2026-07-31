package com.yasarbilgi.ats.candidateprocess.service.impl;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.dto.request.ChangeCandidateStageRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.request.CreateCandidateProcessRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.PipelineBoardResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.PipelineBoardStageResponseDto;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcessStageHistory;
import com.yasarbilgi.ats.candidateprocess.mapper.CandidateProcessMapper;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessStageHistoryRepository;
import com.yasarbilgi.ats.candidateprocess.service.CandidateProcessService;
import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import com.yasarbilgi.ats.pipeline.repository.PipelineStageRepository;
import com.yasarbilgi.ats.pipeline.repository.RecruitmentPipelineRepository;
import com.yasarbilgi.ats.position.entity.Position;
import com.yasarbilgi.ats.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateProcessServiceImpl implements CandidateProcessService {

    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final PositionRepository positionRepository;
    private final RecruitmentPipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final CandidateProcessStageHistoryRepository stageHistoryRepository;
    private final CandidateProcessMapper candidateProcessMapper;

    @Override
    @Transactional
    public CandidateProcessResponseDto create(
            Long companyId,
            CreateCandidateProcessRequestDto request
    ) {
        log.info("Creating candidate process for company: {}, position: {}",
                companyId, request.positionId());

        Company company = getCompany(companyId);
        Position position = getPosition(companyId, request.positionId());
        RecruitmentPipeline pipeline = getPipeline(companyId, request.pipelineId());
        PipelineStage initialStage = stageRepository
                .findFirstByCompanyIdAndPipelineIdAndStageTypeAndActiveTrueOrderByDisplayOrderAsc(
                        companyId,
                        pipeline.getId(),
                        PipelineStageType.ACTIVE
                )
                .orElseThrow(() -> new BusinessRuleException(
                        "Pipeline içerisinde aktif bir başlangıç aşaması bulunamadı."
                ));

        Candidate candidate = findOrCreateCandidate(company, request);

        CandidateProcess process = CandidateProcess.builder()
                .company(company)
                .candidate(candidate)
                .position(position)
                .pipeline(pipeline)
                .currentStage(initialStage)
                .build();

        CandidateProcess savedProcess = candidateProcessRepository.save(process);

        stageHistoryRepository.save(CandidateProcessStageHistory.builder()
                .company(company)
                .candidateProcess(savedProcess)
                .toStage(initialStage)
                .reason("Aday sürece eklendi.")
                .build());

        log.info("Candidate process created with id: {}", savedProcess.getId());
        return candidateProcessMapper.toResponseDto(savedProcess);
    }

    @Override
    @Transactional
    public CandidateProcessResponseDto changeStage(
            Long companyId,
            Long candidateProcessId,
            ChangeCandidateStageRequestDto request
    ) {
        log.info("Changing candidate process stage: {} to stage: {}",
                candidateProcessId, request.stageId());

        CandidateProcess process = candidateProcessRepository
                .findByCompanyIdAndId(companyId, candidateProcessId)
                .orElseThrow(() -> new ResourceNotFoundException("Aday süreci bulunamadı."));

        PipelineStage newStage = stageRepository
                .findByCompanyIdAndPipelineIdAndId(
                        companyId,
                        process.getPipeline().getId(),
                        request.stageId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seçilen aşama bu pipeline içerisinde bulunamadı."
                ));

        PipelineStage previousStage = process.getCurrentStage();

        if (previousStage.getId().equals(newStage.getId())) {
            throw new BusinessRuleException("Aday zaten seçilen aşamada bulunuyor.");
        }

        stageHistoryRepository.save(CandidateProcessStageHistory.builder()
                .company(process.getCompany())
                .candidateProcess(process)
                .fromStage(previousStage)
                .toStage(newStage)
                .reason(request.reason())
                .build());

        process.changeStage(newStage);

        return candidateProcessMapper.toResponseDto(process);
    }

    @Override
    public PipelineBoardResponseDto getBoard(
            Long companyId,
            Long pipelineId,
            Long positionId
    ) {
        log.debug("Fetching board for company: {}, pipeline: {}, position: {}",
                companyId, pipelineId, positionId);

        RecruitmentPipeline pipeline = getPipeline(companyId, pipelineId);
        Position position = getPosition(companyId, positionId);

        List<PipelineStage> stages = stageRepository
                .findAllByCompanyIdAndPipelineIdAndActiveTrueOrderByDisplayOrderAsc(
                        companyId,
                        pipelineId
                );

        Map<Long, List<CandidateProcess>> processesByStage = candidateProcessRepository
                .findAllByCompanyIdAndPositionIdAndPipelineIdAndActiveTrue(
                        companyId,
                        positionId,
                        pipelineId
                )
                .stream()
                .collect(Collectors.groupingBy(process -> process.getCurrentStage().getId()));

        List<PipelineBoardStageResponseDto> stageResponses = stages.stream()
                .map(stage -> new PipelineBoardStageResponseDto(
                        stage.getId(),
                        stage.getName(),
                        stage.getCode(),
                        stage.getDisplayOrder(),
                        stage.getStageType(),
                        processesByStage.getOrDefault(stage.getId(), List.of())
                                .stream()
                                .map(candidateProcessMapper::toCardResponseDto)
                                .toList()
                ))
                .toList();

        return new PipelineBoardResponseDto(
                pipeline.getId(),
                pipeline.getName(),
                position.getId(),
                position.getTitle(),
                stageResponses
        );
    }

    private Candidate findOrCreateCandidate(
            Company company,
            CreateCandidateProcessRequestDto request
    ) {
        String linkedinUrl = normalizeNullable(request.linkedinUrl());

        if (linkedinUrl != null) {
            return candidateRepository
                    .findByCompanyIdAndLinkedinUrl(company.getId(), linkedinUrl)
                    .orElseGet(() -> saveCandidate(company, request, linkedinUrl));
        }

        return saveCandidate(company, request, null);
    }

    private Candidate saveCandidate(
            Company company,
            CreateCandidateProcessRequestDto request,
            String linkedinUrl
    ) {
        return candidateRepository.save(Candidate.builder()
                .company(company)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .linkedinUrl(linkedinUrl)
                .build());
    }

    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .filter(Company::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Şirket bulunamadı."));
    }

    private Position getPosition(Long companyId, Long positionId) {
        return positionRepository.findByCompanyIdAndId(companyId, positionId)
                .filter(Position::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pozisyon bulunamadı."));
    }

    private RecruitmentPipeline getPipeline(Long companyId, Long pipelineId) {
        return pipelineRepository.findByCompanyIdAndId(companyId, pipelineId)
                .filter(RecruitmentPipeline::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline bulunamadı."));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
