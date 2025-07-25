package gift.dto.product;

public record UpdateProductCommand(
        Long id,
        String name,
        Integer price,
        String imageUrl
) {

}
