package com.yasarbilgi.ats.interview.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.interview.dto.request.*;
import com.yasarbilgi.ats.interview.dto.response.*;
import com.yasarbilgi.ats.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidate-processes/{candidateProcessId}/interviews")
public class InterviewController {
    private final InterviewService service;
    // Aday süreci için görüşme planlar.
    @PostMapping public ResponseEntity<ApiResponse<InterviewResponseDto>> create(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId,
            @Valid @RequestBody CreateInterviewRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Görüşme planlandı.",
                service.create(companyId, candidateProcessId, request)));
    }
    // Sürece ait görüşmeleri listeler.
    @GetMapping public ResponseEntity<ApiResponse<PageResponse<InterviewResponseDto>>> getAll(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(companyId, candidateProcessId, page, size)));
    }
    // Planlanmış görüşmenin ayrıntılarını günceller.
    @PutMapping("/{interviewId}") public ResponseEntity<ApiResponse<InterviewResponseDto>> update(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId, @PathVariable Long interviewId,
            @Valid @RequestBody UpdateInterviewRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Görüşme güncellendi.",
                service.update(companyId, candidateProcessId, interviewId, request)));
    }
    // Görüşmeyi tamamlar veya iptal eder.
    @PatchMapping("/{interviewId}/status") public ResponseEntity<ApiResponse<InterviewResponseDto>> changeStatus(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId, @PathVariable Long interviewId,
            @Valid @RequestBody ChangeInterviewStatusRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Görüşme durumu güncellendi.",
                service.changeStatus(companyId, candidateProcessId, interviewId, request)));
    }
    // Atanmış görüşmecinin değerlendirmesini oluşturur veya günceller.
    @PutMapping("/{interviewId}/evaluations") public ResponseEntity<ApiResponse<InterviewEvaluationResponseDto>> saveEvaluation(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId, @PathVariable Long interviewId,
            @Valid @RequestBody SaveInterviewEvaluationRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Görüşme değerlendirmesi kaydedildi.",
                service.saveEvaluation(companyId, candidateProcessId, interviewId, request)));
    }
    // Görüşmeye ait değerlendirmeleri listeler.
    @GetMapping("/{interviewId}/evaluations") public ResponseEntity<ApiResponse<List<InterviewEvaluationResponseDto>>> getEvaluations(
            @PathVariable Long companyId, @PathVariable Long candidateProcessId, @PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(service.getEvaluations(companyId, candidateProcessId, interviewId)));
    }
}
