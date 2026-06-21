package com.commutecarpool.util;

import com.commutecarpool.dto.PageResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class PageUtils {

    public static <T, V> PageResponse<V> toPageResponse(Page<T> page, Class<V> targetType) {
        List<V> content = page.getContent().stream()
                .map(entity -> {
                    V dto = BeanUtils.instantiateClass(targetType);
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return buildPageResponse(page, content);
    }

    public static <T, V> PageResponse<V> toPageResponse(Page<T> page, List<V> content) {
        return buildPageResponse(page, content);
    }

    private static <T, V> PageResponse<V> buildPageResponse(Page<T> page, List<V> content) {
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
