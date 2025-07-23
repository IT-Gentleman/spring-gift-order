package gift.dto;

import gift.entity.Product;
import java.util.List;

public record ProductDto(
        Long id,
        String name,
        Integer price,
        String imageUrl,
        List<ProductOptionDto> options,
        Boolean validated,
        Boolean deleted
) {

    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getOptionList().stream()
                        .map(ProductOptionDto::from)
                        .toList(),
                product.isValidated(),
                product.isDeleted()
        );
    }
}
