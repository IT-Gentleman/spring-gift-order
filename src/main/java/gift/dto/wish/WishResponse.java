package gift.dto.wish;

import java.time.LocalDateTime;

public record WishResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Boolean deleted,
        LocalDateTime createdAt
) {

    public static WishResponse from(WishDto wishDto) {
        return new WishResponse(
                wishDto.id(),
                wishDto.productId(),
                wishDto.productName(),
                wishDto.productImageUrl(),
                wishDto.deleted(),
                wishDto.createdAt()
        );
    }
}
