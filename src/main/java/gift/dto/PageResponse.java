package gift.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        // 실제 데이터
        List<T> content,
        // 페이지 정보 (thymeleaf에서 사용)
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        String sort
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSort().toString()
        );
    }
}
