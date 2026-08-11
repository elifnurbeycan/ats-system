package com.yasarbilgi.ats.attachment.controller;

import com.yasarbilgi.ats.attachment.dto.CandidateCvResponseDto;
import com.yasarbilgi.ats.attachment.service.CandidateCvService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidates/{candidateId}/cv")
public class CandidateCvController {
    private final CandidateCvService service;

    @GetMapping
    public ResponseEntity<ApiResponse<CandidateCvResponseDto>> metadata(@PathVariable Long companyId,
                                                                        @PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success(service.getMetadata(companyId, candidateId)));
    }

    @GetMapping("/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long companyId,
                                                                         @PathVariable Long candidateId) {
        CandidateCvService.DownloadedCv cv = service.download(companyId, candidateId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(cv.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(cv.resource());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CandidateCvResponseDto>> upload(@PathVariable Long companyId,
                                                                      @PathVariable Long candidateId,
                                                                      @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("CV yüklendi.", service.upload(companyId, candidateId, file)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long companyId, @PathVariable Long candidateId) {
        service.delete(companyId, candidateId);
        return ResponseEntity.ok(ApiResponse.success("CV silindi.", null));
    }
}
