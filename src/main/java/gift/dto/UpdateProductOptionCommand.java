package gift.dto;

public record UpdateProductOptionCommand(
        Long id,
        String name,
        Integer quantity,
        Long productId
        //TODO : '본인 등록 상품 수정' 기능 위해 memberId 추가
) {

}
