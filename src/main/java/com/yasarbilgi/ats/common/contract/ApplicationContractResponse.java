package com.yasarbilgi.ats.common.contract;
import java.util.List;

public record ApplicationContractResponse(String version, FileUploadContract candidateCv,
        PaginationContract pagination, List<String> pipelineStageTypes, List<String> salaryCurrencies) {
    public record FileUploadContract(long maxFileSizeBytes, List<String> allowedContentTypes, List<String> allowedExtensions) {}
    public record PaginationContract(int defaultPageSize, int maxPageSize, List<String> candidateSortFields, List<String> sortDirections) {}
    public static ApplicationContractResponse current() {
        return new ApplicationContractResponse(ApplicationContract.VERSION,
                new FileUploadContract(ApplicationContract.CV_MAX_FILE_SIZE_BYTES, ApplicationContract.CV_ALLOWED_CONTENT_TYPES, ApplicationContract.CV_ALLOWED_EXTENSIONS),
                new PaginationContract(ApplicationContract.DEFAULT_PAGE_SIZE, ApplicationContract.MAX_PAGE_SIZE, ApplicationContract.CANDIDATE_SORT_FIELDS, ApplicationContract.SORT_DIRECTIONS),
                ApplicationContract.pipelineStageTypes(), ApplicationContract.SALARY_CURRENCIES);
    }
}
