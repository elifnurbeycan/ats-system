package com.yasarbilgi.ats.contactlead.service;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.contactlead.dto.*;
import com.yasarbilgi.ats.contactlead.entity.ContactLeadStatus;
import com.yasarbilgi.ats.contactlead.entity.ContactRejectionReason;
public interface ContactLeadService { ContactLeadResponse create(Long companyId, CreateContactLeadRequest request); PageResponse<ContactLeadResponse> getAll(Long companyId, String search, ContactLeadStatus status, ContactRejectionReason rejectionReason, int page, int size); ContactLeadResponse resolve(Long companyId, Long leadId, ResolveContactLeadRequest request); }
