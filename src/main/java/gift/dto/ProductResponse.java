package gift.dto;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        Integer price,
        String imageUrl,
        List<ProductOptionResponse> productOptions,
        Boolean validated
) {

    public static ProductResponse from(ProductDto productDto) {
        return new ProductResponse(
                productDto.id(),
                productDto.name(),
                productDto.price(),
                productDto.imageUrl(),
                productDto.options().stream().map(ProductOptionResponse::from).toList(),
                productDto.validated()
        );
    }
}