package com.yasarbilgi.ats.common.contract;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import java.util.Arrays;
import java.util.List;

/** Frontend istemcilerinin doğrulama ve seçenekler için kullandığı sürümlü sözleşme. */
public final class ApplicationContract {
    public static final String VERSION = "1.0";
    public static final long CV_MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    public static final List<String> CV_ALLOWED_CONTENT_TYPES = List.of("application/pdf");
    public static final List<String> CV_ALLOWED_EXTENSIONS = List.of(".pdf");
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final List<String> CANDIDATE_SORT_FIELDS = List.of("name", "createdAt");
    public static final List<String> SORT_DIRECTIONS = List.of("asc", "desc");
    public static final List<String> SALARY_CURRENCIES = List.of("TRY", "USD", "EUR", "GBP");
    private ApplicationContract() {}
    public static List<String> pipelineStageTypes() {
        return Arrays.stream(PipelineStageType.values()).map(Enum::name).toList();
    }
}
