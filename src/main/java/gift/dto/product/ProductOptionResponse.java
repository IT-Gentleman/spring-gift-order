package gift.dto.product;

public record ProductOptionResponse(
        Long id,
        String name,
        Integer quantity,
        Long productId
) {

    public static ProductOptionResponse from(ProductOptionDto productOptionDto) {
        return new ProductOptionResponse(
                productOptionDto.id(),
                productOptionDto.name(),
                productOptionDto.quantity(),
                productOptionDto.productId()
        );
    }
}
