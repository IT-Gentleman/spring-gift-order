package gift.dto;

public record NewWishCommand(
        Long memberId,
        Long productId
) {

}
