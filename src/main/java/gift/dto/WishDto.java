package gift.dto;

import gift.entity.Wish;
import java.time.LocalDateTime;

// WishService -> WishController
public record WishDto(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Boolean deleted,
        LocalDateTime addedAt
) {

    public static WishDto from(Wish wish) {
        return new WishDto(
                wish.getId(),
                wish.getProduct().getId(),
                wish.getProduct().getName(),
                wish.getProduct().getImageUrl(),
                wish.getProduct().isDeleted(),
                wish.getAddedAt()
        );
    }
}
