package com.yasarbilgi.ats.pipeline.service.impl;

import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.pipeline.dto.request.*;
import com.yasarbilgi.ats.pipeline.dto.response.*;
import com.yasarbilgi.ats.pipeline.entity.*;
import com.yasarbilgi.ats.pipeline.mapper.PipelineMapper;
import com.yasarbilgi.ats.pipeline.repository.*;
import com.yasarbilgi.ats.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

    private final CompanyRepository companyRepository;
    private final RecruitmentPipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final PipelineMapper pipelineMapper;

    // Yeni pipeline'ı aşamalarıyla birlikte tek işlemde oluşturur.
    @Override
    @Transactional
    public PipelineDetailResponseDto create(Long companyId, CreatePipelineRequestDto request) {
        Company company = getCompany(companyId);
        String code = normalizeCode(request.code());
        if (pipelineRepository.existsByCompanyIdAndCode(companyId, code)) {
            throw new BusinessRuleException("Pipeline kodu daha önce kullanılmış.");
        }
        validateNewStages(request.stages());
        if (request.defaultPipeline()) removeCurrentDefault(companyId);

        RecruitmentPipeline pipeline = pipelineRepository.save(RecruitmentPipeline.builder()
                .company(company).name(request.name().trim()).code(code)
                .description(trimToNull(request.description()))
                .defaultPipeline(request.defaultPipeline()).build());
        request.stages().forEach(stage -> stageRepository.save(toStage(company, pipeline, stage)));
        return toDetail(pipeline);
    }

    // Şirketin aktif pipeline'larını listeler.
    @Override
    public List<PipelineSummaryResponseDto> getPipelines(Long companyId) {
        getCompany(companyId);
        return pipelineRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId).stream()
                .map(pipelineMapper::toSummaryResponseDto).toList();
    }

    // Pipeline'ın aktif aşamalarını sıralı olarak getirir.
    @Override
    public List<PipelineStageResponseDto> getStages(Long companyId, Long pipelineId) {
        getPipeline(companyId, pipelineId);
        return stageRepository.findAllByCompanyIdAndPipelineIdAndActiveTrueOrderByDisplayOrderAsc(
                        companyId, pipelineId).stream().map(pipelineMapper::toStageResponseDto).toList();
    }

    // Pipeline ayrıntısını aktif ve pasif aşamalarıyla getirir.
    @Override
    public PipelineDetailResponseDto getById(Long companyId, Long pipelineId) {
        return toDetail(getPipeline(companyId, pipelineId));
    }

    // Pipeline'ın görünen bilgilerini ve varsayılan durumunu günceller.
    @Override
    @Transactional
    public PipelineDetailResponseDto update(Long companyId, Long pipelineId, UpdatePipelineRequestDto request) {
        RecruitmentPipeline pipeline = getPipeline(companyId, pipelineId);
        pipeline.updateDetails(request.name().trim(), trimToNull(request.description()));
        if (request.defaultPipeline()) {
            removeCurrentDefault(companyId);
            pipeline.markAsDefault();
        }
        return toDetail(pipeline);
    }

    // Kullanımda ve varsayılan olmayan pipeline'ı pasifleştirir.
    @Override
    @Transactional
    public PipelineDetailResponseDto deactivate(Long companyId, Long pipelineId) {
        RecruitmentPipeline pipeline = getPipeline(companyId, pipelineId);
        if (pipeline.isDefaultPipeline()) {
            throw new BusinessRuleException("Varsayılan pipeline pasifleştirilemez.");
        }
        if (candidateProcessRepository.existsByCompanyIdAndPipelineIdAndActiveTrue(companyId, pipelineId)) {
            throw new BusinessRuleException("Aktif aday süreçlerinde kullanılan pipeline pasifleştirilemez.");
        }
        pipeline.deactivate();
        return toDetail(pipeline);
    }

    // Pipeline'a benzersiz kod ve sıraya sahip yeni aşama ekler.
    @Override
    @Transactional
    public PipelineStageResponseDto addStage(Long companyId, Long pipelineId,
                                             CreatePipelineStageRequestDto request) {
        RecruitmentPipeline pipeline = getActivePipeline(companyId, pipelineId);
        String code = normalizeCode(request.code());
        validateStageUniqueness(companyId, pipelineId, code, request.displayOrder());
        PipelineStage stage = stageRepository.save(toStage(pipeline.getCompany(), pipeline, request));
        return pipelineMapper.toStageResponseDto(stage);
    }

    // Pipeline aşamasının adını, açıklamasını ve davranış türünü günceller.
    @Override
    @Transactional
    public PipelineStageResponseDto updateStage(Long companyId, Long pipelineId, Long stageId,
                                                UpdatePipelineStageRequestDto request) {
        PipelineStage stage = getStage(companyId, pipelineId, stageId);
        stage.updateDetails(request.name().trim(), trimToNull(request.description()));
        stage.changeStageType(request.stageType());
        return pipelineMapper.toStageResponseDto(stage);
    }

    // Tüm aktif aşamaların sırasını benzersiz sıra değerlerini koruyarak değiştirir.
    @Override
    @Transactional
    public List<PipelineStageResponseDto> reorderStages(Long companyId, Long pipelineId,
                                                        ReorderPipelineStagesRequestDto request) {
        getActivePipeline(companyId, pipelineId);
        List<PipelineStage> stages = stageRepository
                .findAllByCompanyIdAndPipelineIdAndActiveTrueOrderByDisplayOrderAsc(companyId, pipelineId);
        if (new HashSet<>(request.stageIds()).size() != request.stageIds().size()
                || stages.size() != request.stageIds().size()
                || !stages.stream().map(PipelineStage::getId).collect(java.util.stream.Collectors.toSet())
                .equals(new HashSet<>(request.stageIds()))) {
            throw new BusinessRuleException("Sıralama listesi pipeline'ın tüm aktif aşamalarını bir kez içermelidir.");
        }
        Map<Long, PipelineStage> byId = new HashMap<>();
        stages.forEach(stage -> byId.put(stage.getId(), stage));
        for (int index = 0; index < request.stageIds().size(); index++) {
            byId.get(request.stageIds().get(index)).changeDisplayOrder(-(index + 1));
        }
        stageRepository.flush();
        for (int index = 0; index < request.stageIds().size(); index++) {
            byId.get(request.stageIds().get(index)).changeDisplayOrder(index + 1);
        }
        stageRepository.flush();
        return request.stageIds().stream().map(byId::get)
                .map(pipelineMapper::toStageResponseDto).toList();
    }

    // Aktif süreçte kullanılmayan pipeline aşamasını pasifleştirir.
    @Override
    @Transactional
    public PipelineStageResponseDto deactivateStage(Long companyId, Long pipelineId, Long stageId) {
        PipelineStage stage = getStage(companyId, pipelineId, stageId);
        if (candidateProcessRepository.existsByCompanyIdAndCurrentStageIdAndActiveTrue(companyId, stageId)) {
            throw new BusinessRuleException("Aktif adayların bulunduğu aşama pasifleştirilemez.");
        }
        stage.deactivate();
        return pipelineMapper.toStageResponseDto(stage);
    }

    // Yeni pipeline içindeki aşama kodlarının ve sıra değerlerinin benzersizliğini doğrular.
    private void validateNewStages(List<CreatePipelineStageRequestDto> stages) {
        Set<String> codes = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        for (CreatePipelineStageRequestDto stage : stages) {
            if (!codes.add(normalizeCode(stage.code())) || !orders.add(stage.displayOrder())) {
                throw new BusinessRuleException("Aşama kodları ve sıra değerleri pipeline içinde benzersiz olmalıdır.");
            }
        }
        if (stages.stream().noneMatch(stage -> stage.stageType() == PipelineStageType.ACTIVE)) {
            throw new BusinessRuleException("Pipeline en az bir aktif süreç aşaması içermelidir.");
        }
    }

    // Eklenecek aşamanın kod ve sıra değerinin kullanılmadığını doğrular.
    private void validateStageUniqueness(Long companyId, Long pipelineId, String code, Integer order) {
        if (stageRepository.existsByCompanyIdAndPipelineIdAndCode(companyId, pipelineId, code))
            throw new BusinessRuleException("Aşama kodu daha önce kullanılmış.");
        if (stageRepository.existsByCompanyIdAndPipelineIdAndDisplayOrder(companyId, pipelineId, order))
            throw new BusinessRuleException("Aşama sıra değeri daha önce kullanılmış.");
    }

    // Mevcut varsayılan pipeline'ın işaretini kaldırır.
    private void removeCurrentDefault(Long companyId) {
        pipelineRepository.findByCompanyIdAndDefaultPipelineTrueAndActiveTrue(companyId)
                .ifPresent(RecruitmentPipeline::removeDefault);
    }

    // İstek verisinden yeni pipeline aşaması oluşturur.
    private PipelineStage toStage(Company company, RecruitmentPipeline pipeline,
                                  CreatePipelineStageRequestDto request) {
        return PipelineStage.builder().company(company).pipeline(pipeline).name(request.name().trim())
                .code(normalizeCode(request.code())).description(trimToNull(request.description()))
                .displayOrder(request.displayOrder()).stageType(request.stageType()).build();
    }

    // Pipeline entity'sini aşamalarıyla birlikte ayrıntılı yanıta dönüştürür.
    private PipelineDetailResponseDto toDetail(RecruitmentPipeline pipeline) {
        List<PipelineStageResponseDto> stages = stageRepository
                .findAllByCompanyIdAndPipelineIdOrderByDisplayOrderAsc(
                        pipeline.getCompany().getId(), pipeline.getId()).stream()
                .map(pipelineMapper::toStageResponseDto).toList();
        return new PipelineDetailResponseDto(pipeline.getId(), pipeline.getName(), pipeline.getCode(),
                pipeline.getDescription(), pipeline.isDefaultPipeline(), pipeline.isActive(), stages);
    }

    // Şirket kaydını getirir.
    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirket bulunamadı: " + companyId));
    }

    // Şirkete ait pipeline kaydını getirir.
    private RecruitmentPipeline getPipeline(Long companyId, Long pipelineId) {
        return pipelineRepository.findByCompanyIdAndId(companyId, pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline bulunamadı: " + pipelineId));
    }

    // Şirkete ait aktif pipeline kaydını getirir.
    private RecruitmentPipeline getActivePipeline(Long companyId, Long pipelineId) {
        RecruitmentPipeline pipeline = getPipeline(companyId, pipelineId);
        if (!pipeline.isActive()) throw new BusinessRuleException("Pasif pipeline güncellenemez.");
        return pipeline;
    }

    // Pipeline'a ait aşama kaydını getirir.
    private PipelineStage getStage(Long companyId, Long pipelineId, Long stageId) {
        return stageRepository.findByCompanyIdAndPipelineIdAndId(companyId, pipelineId, stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline aşaması bulunamadı: " + stageId));
    }

    // Sistem kodunu büyük harfli standart biçime dönüştürür.
    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    // Boş açıklamaları null, dolu açıklamaları kırpılmış biçime dönüştürür.
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
