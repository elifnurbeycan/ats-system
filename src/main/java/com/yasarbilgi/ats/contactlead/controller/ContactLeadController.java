package com.yasarbilgi.ats.contactlead.controller;
import com.yasarbilgi.ats.common.response.*;
import com.yasarbilgi.ats.contactlead.dto.*;
import com.yasarbilgi.ats.contactlead.entity.ContactLeadStatus;
import com.yasarbilgi.ats.contactlead.entity.ContactRejectionReason;
import com.yasarbilgi.ats.contactlead.service.ContactLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/companies/{companyId}/contact-leads")
public class ContactLeadController { private final ContactLeadService service;
 @PostMapping public ResponseEntity<ApiResponse<ContactLeadResponse>> create(@PathVariable Long companyId,@Valid @RequestBody CreateContactLeadRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Kişi iletişim havuzuna eklendi.",service.create(companyId,request)));}
 @GetMapping public ResponseEntity<ApiResponse<PageResponse<ContactLeadResponse>>> getAll(@PathVariable Long companyId,@RequestParam(required=false)String search,@RequestParam(required=false)ContactLeadStatus status,@RequestParam(required=false)ContactRejectionReason rejectionReason,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ResponseEntity.ok(ApiResponse.success(service.getAll(companyId,search,status,rejectionReason,page,size)));}
 @PatchMapping("/{leadId}/resolve") public ResponseEntity<ApiResponse<ContactLeadResponse>> resolve(@PathVariable Long companyId,@PathVariable Long leadId,@Valid @RequestBody ResolveContactLeadRequest request){return ResponseEntity.ok(ApiResponse.success("İletişim sonucu kaydedildi.",service.resolve(companyId,leadId,request)));}
}
