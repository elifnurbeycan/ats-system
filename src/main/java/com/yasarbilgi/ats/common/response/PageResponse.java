package com.yasarbilgi.ats.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    // Spring sayfa sonucunu API'nin kararlı sayfalama yanıtına dönüştürür.
    public static <S, T> PageResponse<T> from(Page<S> source, Function<S, T> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast()
        );
    }

    public static <T> PageResponse<T> fromList(List<T> source, int page, int size) {
        int from = Math.min(page * size, source.size());
        int to = Math.min(from + size, source.size());
        int totalPages = source.isEmpty() ? 0 : (int) Math.ceil((double) source.size() / size);
        return new PageResponse<>(source.subList(from, to), page, size, source.size(), totalPages,
                page == 0, page >= totalPages - 1);
    }
}
