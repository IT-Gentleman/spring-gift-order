package gift.dto;

import gift.entity.ProductOption;

public record ProductOptionDto(
        Long id,
        String name,
        Integer quantity,
        Long productId
) {

    public static ProductOptionDto from(ProductOption productOption) {
        return new ProductOptionDto(
                productOption.getId(),
                productOption.getName(),
                productOption.getQuantity(),
                productOption.getProduct().getId()
        );
    }
}
