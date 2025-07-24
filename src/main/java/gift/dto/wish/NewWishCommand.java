package gift.dto.wish;

public record NewWishCommand(
        Long memberId,
        Long productId
) {

}
